# Replay tool usage

[replay tool repository](https://github.com/blockchain-etl/bigquery-to-pubsub)

A tool for streaming time series data from a BigQuery table to Pub/Sub

1. Create a temporary GCS bucket and a temporary BigQuery dataset:

```bash
> bash create_temp_resources.sh
```
 
2. Run replay for transactions:

```bash
> project=$(gcloud config get-value project 2> /dev/null)
> temp_resource_name=$(./get_temp_resource_name.sh)
> echo "Replaying Ethereum transactions"
> docker run \
    blockchainetl/bigquery-to-pubsub:latest \
    --bigquery-table bigquery-public-data.crypto_ethereum.transactions \
    --timestamp-field block_timestamp \
    --start-timestamp 2019-10-23T00:00:00 \
    --end-timestamp 2019-10-23T01:00:00 \
    --batch-size-in-seconds 1800 \
    --replay-rate 0.1 \
    --pubsub-topic projects/${project}/topics/bigquery-to-pubsub-test0 \
    --temp-bigquery-dataset ${temp_resource_name} \
    --temp-bucket ${temp_resource_name}
```
