package com.google.allenday.input;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.google.allenday.transaction.StockTrade;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.transforms.DoFn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class DeserializeStockTrade extends DoFn<PubsubMessage, StockTrade> {

    private Logger LOG = LoggerFactory.getLogger(DeserializeStockTrade.class);

    @ProcessElement
    public void processElement(ProcessContext c) {
        PubsubMessage msg = c.element();
        String jsonString = new String(msg.getPayload());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        try {
            StockTrade st = mapper.readValue(jsonString, StockTrade.class);
            c.output(st);
        } catch (IOException e) {
            LOG.error("Error parsing message: " + e.getMessage());
        }
    }
}
