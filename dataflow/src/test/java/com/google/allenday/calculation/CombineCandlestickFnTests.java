package com.google.allenday.calculation;


import com.google.allenday.input.TransactionToRow;
import com.google.allenday.transaction.EthereumTransaction;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.jupiter.api.Test;


class CombineCandlestickFnTests {

    @Rule
    public final transient TestPipeline testPipeline = TestPipeline.create().enableAbandonedNodeEnforcement(true);

    @Test
    void combineSameBlock() {
        PCollection<Candlestick> candlesticks = testPipeline
                .apply(Create.of(
                        // open
                        getTransaction(10, 1571789348000L, 1),
                        // high
                        getTransaction(100, 1571789348000L, 2),
                        // low
                        getTransaction(1, 1571789348000L, 3),
                        // close
                        getTransaction(10, 1571789348000L, 4)
                ))
                .apply(ParDo.of(new TransactionToRow()))
                .apply("Combine", Combine.globally(new CombineCandlestickFn()).withoutDefaults());

        PAssert
                .that(candlesticks)
                .containsInAnyOrder(new Candlestick(10, 10, 1, 100));

        testPipeline.run();
    }

    @Test
    void combineDifferentBlocks() {
        PCollection<Candlestick> candlesticks = testPipeline
                .apply(Create.of(
                        // open & high
                        getTransaction(200, 1571789344000L, 1),
                        getTransaction(100, 1571789348000L, 1),
                        // low
                        getTransaction(1, 1571789348000L, 2),
                        getTransaction(10, 1571789348000L, 3),
                        // close
                        getTransaction(5, 1571789349000L, 1)
                ))
                .apply(ParDo.of(new TransactionToRow()))
                .apply("Combine", Combine.globally(new CombineCandlestickFn()).withoutDefaults());

        PAssert
                .that(candlesticks)
                .containsInAnyOrder(new Candlestick(200, 5, 1, 200));

        testPipeline.run();
    }

    @Test
    void combineSingleTransaction() {
        PCollection<Candlestick> candlesticks = testPipeline
                .apply(Create.of(
                        // open & close & low & high
                        getTransaction(1, 1571789348000L, 2)
                ))
                .apply(ParDo.of(new TransactionToRow()))
                .apply("Combine", Combine.globally(new CombineCandlestickFn()).withoutDefaults());

        PAssert
                .that(candlesticks)
                .containsInAnyOrder(new Candlestick(1, 1, 1, 1));

        testPipeline.run();
    }

    EthereumTransaction getTransaction(long price, long blockTimestamp, long index) {
        EthereumTransaction tx = new EthereumTransaction();
        tx.setTransactionIndex(index);
        tx.setGasPrice(price);
        tx.setBlockTimestamp(blockTimestamp);

        return tx;
    }
}
