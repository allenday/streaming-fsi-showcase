package com.google.allenday.input;

import com.google.allenday.transaction.EthereumTransaction;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.Row;

public class TransactionToRow extends DoFn<EthereumTransaction, Row> {
    @ProcessElement
    public void processElement(ProcessContext c) {
        String key = c.element().getBlockTimestamp() + "_" + c.element().getTransactionIndex();
        Row row = Row
                .withSchema(InputSchema.schema)
                .addValue(key)
                .addValue(c.element().getGasPrice())
                .build();

        c.output(row);
    }
}
