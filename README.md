# Streaming FSI Showcase


![Architecture](images/architecture.png "Architecture")


### Copy stock trades historical data
```shell script
export PROJECT=$(gcloud config get-value project 2> /dev/null)
bq cp ethereum-streaming-dev:polygon.trades $PROJECT:polygon.trades
```

### AI Notebook

Install jupyter extensions for plotting
