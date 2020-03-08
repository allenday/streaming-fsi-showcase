# Streaming FSI showcase

This document is designed as a tutorial of copy-paste-able commands to see how to use streaming data in Google Cloud Platform's Data and Analytics tools. Here's an overview of the application we'll be showcasing in this tutorial:

![Architecture](images/architecture.png "Architecture")

As you can see, we'll be using a variety of GCP services:
- BigQuery - some of the data come from here, and are replayed into...
- PubSub - we'll be showing how to consume replayed historical financial data, as well as live cryptocurrency data
- Dataflow - we're using dataflow to perform a simple aggregation of the pubsub stream into fixed-size time windows so that they can be visualized as candlesticks, a common type of data visualization for financial data.
- Firestore - we're putting data into Firestore for web visualization with D3.js

Here's a table of contents for the tutorial. Clicking any item will link down to that section later in this doc:

- [Preparation](#preparation)
  - [Enable PubSub API](#enable-pubsub-api)
  - [Enable Dataflow API](#enable-dataflow-api)
  - [Enable Firestore](#enable-firestore)
- [Stock trades](#stock-trades)
  - [Prepare stock data](#prepare-stock-data)
  - [AI notebook](#ai-notbook)
  - [Replay tool](#replay-tool)
  - [Stock Dataflow](#stock-dataflow)
  - [Charts](#charts)
    - [Configure Firestore](#configure-firestore)

## Prepartation

### Enable PubSub API
Go to [APIs page](https://console.developers.google.com/apis/api/pubsub.googleapis.com/overview). Search for PubSub and click - "Enable"

### Enable Dataflow API
Go to [APIs page](https://console.developers.google.com/apis/api/dataflow.googleapis.com/overview). Search for Dataflow and click - "Enable"

### Enable Firestore
Go to [Firestore Page](https://console.cloud.google.com/firestore/welcome). Select Firestore in Native mode.

## Stock trades

### Prepare stock data

Copy stock trades historical data
```shell script
export PROJECT=$(gcloud config get-value project 2> /dev/null)
bq mk polygon
bq cp ethereum-streaming-dev:polygon.trades $PROJECT:polygon.trades
```

### AI Notebook

- clone repository in AI notebooks environment. Use the `https` URL, i.e. `https://github.com/allenday/streaming-fsi-showcase.git`
- run `jupyter/_jupyter-extensions.ipynb` notebook to install extensions (takes ~10 minutes)
- reboot jupyter notebook VM
- run `jupyter/bq.ipynb` notebook

### Replay tool

Clone this repository in Cloud Shell:

```shell script
git clone https://github.com/allenday/streaming-fsi-showcase
cd streaming-fsi-showcase
export $REPO=PWD
```

Create a PubSub topic. We'll be publishing data to this topic from a VM that retrieves historical data from BigQuery and replays it as if it's live data.

```shell script
gcloud pubsub topics create polygon.trades --project=$PROJECT
```

Create temp resources and start a GCE instance running a Docker container based on the [blockchain-etl/bigquery-to-pubsub](https://github.com/blockchain-etl/bigquery-to-pubsub) repo.

Note that in this step we define a variable `$REPLAY_RATE` to replay data at 10x speed. It makes this demo more dynamic and more interesting than watching paint dry.

```shell script
# we'll replay the trade data faster than real-time to make for a more dynamic demo
$REPLAY_RATE=0.1
cd $REPO/replay
# we'll create a temporary GCS bucket with this name:
export TEMP_RESOURCE_NAME=$(./get_temp_resource_name.sh)
bash ./create_temp_resources.sh
gcloud compute instances create-with-container replay-tool \
  --zone=us-central1-a \
  --machine-type=n1-standard-1 \
  --scopes=https://www.googleapis.com/auth/bigquery,https://www.googleapis.com/auth/pubsub,https://www.googleapis.com/auth/servicecontrol,https://www.googleapis.com/auth/service.management.readonly,https://www.googleapis.com/auth/logging.write,https://www.googleapis.com/auth/monitoring.write,https://www.googleapis.com/auth/trace.append,https://www.googleapis.com/auth/devstorage.read_write \
  --container-image=blockchainetl/bigquery-to-pubsub:0.0.1 \
  --container-restart-policy=always \
  --container-arg=--bigquery-table \
  --container-arg=$PROJECT.polygon.trades \
  --container-arg=--timestamp-field \
  --container-arg=ts \
  --container-arg=--start-timestamp \
  --container-arg=2018-12-31T18:00:00 \
  --container-arg=--end-timestamp \
  --container-arg=2019-01-01T00:00:00 \
  --container-arg=--batch-size-in-seconds \
  --container-arg=1800 \
  --container-arg=--replay-rate \
  --container-arg=$REPLAY_RATE \
  --container-arg=--pubsub-topic \
  --container-arg=projects/$PROJECT/topics/polygon.trades \
  --container-arg=--temp-bigquery-dataset \
  --container-arg=$TEMP_RESOURCE_NAME \
  --container-arg=--temp-bucket \
  --container-arg=$TEMP_RESOURCE_NAME
```

Before moving on, go to the PubSub page of Cloud Console and perform a sanity check. Create a test subscription to make sure data are being published to PubSub from the replay tool.

### Stock dataflow

Create a subscription:
```shell script
gcloud pubsub subscriptions create polygon.trades --topic=polygon.trades --ack-deadline=60
```

And start a dataflow pipeline:

```shell script
cd $REPO/dataflow
mvn clean package
java -cp target/ethereum-streaming-analytics-bundled-1.0-SNAPSHOT.jar com.google.allenday.TransactionMetricsPipeline \
--runner=org.apache.beam.runners.dataflow.DataflowRunner \
--project=$PROJECT \
--inputDataTopicOrSubscription=projects/$PROJECT/topics/polygon.trades \
--firestoreCollection=polygon_trades \
--streaming=true \
--jobName=polygon-candlestick-demo \
--inputType=polygon
```

As a sanity check, go to the dataflow page in Cloud Console to confirm that the dataflow job was created and that is is successfully retrieving data from PubSub.

### Charts

#### Configure Firestore
Go to [Firestore console](https://console.firebase.google.com/)
- add your project to Firebase Console
- add new application named "charts"
- under `database > rules` set up permissions on the fireestore database
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /polygon_trades/{documentId} {
     allow read;
    }
    match /ethereum_transactions/{documentId} {
     allow read;
    }
    match /{document=**} {
      allow read, write: if false;
    }
  }
}
```

#### Upload the Javascript application
- modify `charts/csmain.js` by putting your Firebase app config inside
- create public bucket
```shell script
export PUBLIC_BUCKET_NAME=${PROJECT}_public
gsutil mb gs://$PUBLIC_BUCKET_NAME
gsutil iam ch allUsers:objectViewer gs://$PUBLIC_BUCKET_NAME
```
- upload files from `charts` directory to public bucket: `gsutil -m cp charts/* gs://$PUBLIC_BUCKET_NAME`
- check real-time chart (here: `echo https://storage.googleapis.com/$PUBLIC_BUCKET_NAME/trade.html`)

## Ethereum transactions

#### Create subscription to public Ethereum topic

```shell script
gcloud pubsub subscriptions create crypto_ethereum.transactions \
  --topic=crypto_ethereum.transactions \
  --topic-project=crypto-public-data \
  --ack-deadline=60
```

### AI Notebook

Run jupyter/pub-sub.ipynb notebook to inspect data in PubSub

#### Start Dataflow pipeline

```shell script
cd $REPO/dataflow
java -cp target/ethereum-streaming-analytics-bundled-1.0-SNAPSHOT.jar com.google.allenday.TransactionMetricsPipeline \
--runner=org.apache.beam.runners.dataflow.DataflowRunner \
--project=$PROJECT \
--inputDataTopicOrSubscription=projects/$PROJECT/subscriptions/crypto_ethereum.transactions \
--firestoreCollection=ethereum_transactions \
--streaming=true \
--jobName=ethereum-candlestick-demo \
--inputType=ethereum
```

#### JS Application
- check real-time chart in ehtereum.html file on public bucket
