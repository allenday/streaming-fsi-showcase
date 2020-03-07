package com.google.allenday.input;

import com.google.allenday.transaction.StockTrade;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.Row;

public class TradeToRow extends DoFn<StockTrade, Row> {
    String symbol;

    TradeToRow(String symbol) {
        this.symbol = symbol;
    }

    @ProcessElement
    public void processElement(ProcessContext c) {
        String key = c.element().getTimestamp() + "_" + c.element().getSequenceNum();
        Row row = Row
                .withSchema(InputSchema.schema)
                .addValue(key)
                .addValue(c.element().getPrice())
                .build();

        if (c.element().getSymbol().equalsIgnoreCase(symbol)) {
            c.output(row);
        }
    }
}
