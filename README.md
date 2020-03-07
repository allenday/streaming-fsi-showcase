# Streaming FSI Showcase

![Architecture](images/architecture.png "Architecture")

## Prepare stock data

Copy stock trades historical data
```shell script
export PROJECT=$(gcloud config get-value project 2> /dev/null)
bq cp ethereum-streaming-dev:polygon.trades $PROJECT:polygon.trades
```

## AI Notebook

Clone repository in AI notebooks environment
Run bq.ipynb notebook

## Replay tool

Create topic
```shell script
gcloud pubsub topics create polygon.trades --project=$PROJECT
```

Create temp resources and start a VM running docker container
```shell script
cd ./replay
bash ./create_temp_resources.sh
export TEMP_RESOURCE_NAME=$(./get_temp_resource_name.sh)
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
  --container-arg=0.1 \
  --container-arg=--pubsub-topic \
  --container-arg=projects/$PROJECT/topics/polygon.trades \
  --container-arg=--temp-bigquery-dataset \
  --container-arg=$TEMP_RESOURCE_NAME \
  --container-arg=--temp-bucket \
  --container-arg=$TEMP_RESOURCE_NAME
```

## Dataflow
