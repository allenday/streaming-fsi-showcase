package com.google.allenday;

import com.google.allenday.calculation.Candlestick;
import com.google.allenday.calculation.CombineCandlestickFn;
import com.google.allenday.firestore.DataPoint;
import com.google.allenday.firestore.WriteDataToFirestoreDbFn;
import com.google.allenday.input.*;
import com.google.allenday.transaction.StockTrade;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubIO;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import org.apache.beam.sdk.transforms.*;
import org.apache.beam.sdk.transforms.windowing.FixedWindows;
import org.apache.beam.sdk.transforms.windowing.Window;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.Row;
import org.joda.time.Duration;

public class TransactionMetricsPipeline {

    private static final int WINDOW_SIZE_SECONDS = 60;

    public static void main(String[] args) {
        TransactionMetricsPipelineOptions options =
                PipelineOptionsFactory.fromArgs(args).withValidation().as(TransactionMetricsPipelineOptions.class);

        Pipeline pipeline = Pipeline.create(options);

        PubsubIO.Read<PubsubMessage> readFromPubSub;
        if (options.getInputDataTopicOrSubscription().contains("/topics/")) {
            readFromPubSub = PubsubIO
                    .readMessagesWithAttributes()
                    .fromTopic(options.getInputDataTopicOrSubscription());
        } else {
            readFromPubSub = PubsubIO
                    .readMessagesWithAttributes()
                    .fromSubscription(options.getInputDataTopicOrSubscription());
        }

        PCollection<PubsubMessage> messages = pipeline.apply("Reading PubSub", readFromPubSub);

        PCollection<Row> rows;
        if (options.getInputType().equals("ethereum")) {
            rows = messages
                    .apply("Deserialize JSON", ParDo.of(new DeserializeTransaction()))
                    .apply(ParDo.of(new TransactionToRow()));
        } else {
            // TODO: extract
            rows = messages
                    .apply("Deserialize JSON", ParDo.of(new DeserializeStockTrade()))
                    .apply(ParDo.of(new TradeToRow(options.getStockSymbol())));
        }
        rows.setRowSchema(InputSchema.schema)
                .apply(
                        "Fixed windows",
                        Window.into(FixedWindows.of(Duration.standardSeconds(WINDOW_SIZE_SECONDS)))
                )
                .apply("Calculate statistic", Combine.globally(new CombineCandlestickFn()).withoutDefaults())
                .apply("Prepare data points", ParDo.of(new DoFn<Candlestick, DataPoint>() {
                    @ProcessElement
                    public void processElement(ProcessContext c) {
                        Candlestick candlestick = c.element();

                        System.err.format("timestamp: %d, object: %s\n",
                                c.timestamp().getMillis(), candlestick.toString()
                        );

                        DataPoint dataPoint = new DataPoint();
                        dataPoint.setTimestamp(c.timestamp().getMillis());
                        dataPoint.setCandlestick(candlestick);

                        c.output(dataPoint);
                    }
                }))
                .apply("Write to FireStore", ParDo.of(
                        new WriteDataToFirestoreDbFn(options.getProject(), options.getFirestoreCollection())
                ))
        ;
        pipeline.run();
    }
}
