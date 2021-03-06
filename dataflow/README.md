# ethereum-streaming-analytics-demo
Process Ethereum data with Pubsub and Dataflow

## Build
```bash
mvn clean package
```

## Run
```bash
export RUNNER=org.apache.beam.runners.dataflow.DataflowRunner
export PROJECT=ethereum-streaming-dev
export TOPIC=projects/ethereum-streaming-dev/topics/crypto_ethereum.transactions
export FIRESTORE_COLLECTION=demo_gas_price
java -cp target/ethereum-streaming-analytics-bundled-1.0-SNAPSHOT.jar com.google.allenday.TransactionMetricsPipeline \
  --runner=$RUNNER \
  --project=$PROJECT \
  --inputDataTopic=$TOPIC \
  --firestoreCollection=$FIRESTORE_COLLECTION \
  --streaming=true
```

## Replay

### Replaying Ethereum transactions in loop
```bash
project=$(gcloud config get-value project 2> /dev/null)
temp_resource_name=$(./get_temp_resource_name.sh)
docker run -rm -d --restart unless-stopped bigquery-to-pubsub:latest \
  --bigquery-table bigquery-public-data.crypto_ethereum.transactions \
  --timestamp-field block_timestamp \
  --start-timestamp 2019-10-23T00:00:00 \
  --end-timestamp 2019-10-23T01:00:00 \
  --batch-size-in-seconds 1800 \
  --replay-rate 0.1 \
  --pubsub-topic projects/${project}/topics/crypto_ethereum.transactions \
  --temp-bigquery-dataset ${temp_resource_name} \
  --temp-bucket ${temp_resource_name}
```
