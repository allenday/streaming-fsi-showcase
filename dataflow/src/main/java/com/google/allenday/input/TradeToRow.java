package com.google.allenday.input;

import com.google.allenday.transaction.StockTrade;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.Row;

public class TradeToRow extends DoFn<StockTrade, Row> {
    @ProcessElement
    public void processElement(ProcessContext c) {
        String key = c.element().getTimestamp() + "_" + c.element().getSequenceNum();
        Row row = Row
                .withSchema(InputSchema.schema)
                .addValue(key)
                .addValue(c.element().getPrice())
                .build();

        // TODO: Parameterize
        if (c.element().getSymbol().equalsIgnoreCase("SPY")) {
            c.output(row);
        }
    }
}
