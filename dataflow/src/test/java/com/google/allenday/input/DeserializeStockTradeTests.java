package com.google.allenday.input;


import com.google.allenday.transaction.StockTrade;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.testing.PAssert;
import org.apache.beam.sdk.testing.TestPipeline;
import org.apache.beam.sdk.transforms.Create;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.junit.Rule;
import org.junit.jupiter.api.Test;

import java.util.Collections;

class DeserializeStockTradeTests {

    @Rule
    public final transient TestPipeline testPipeline = TestPipeline.create().enableAbandonedNodeEnforcement(true);

    @Test
    void deserialize() {
        PubsubMessage message = new PubsubMessage(getTransactionJson().getBytes(), Collections.emptyMap());

        PCollection<StockTrade> transactions = testPipeline
                .apply(Create.of(message))
                .apply("Deserialize JSON", ParDo.of(new DeserializeStockTrade()));

        PAssert
                .that(transactions)
                .containsInAnyOrder(getTrade());

        testPipeline.run();
    }

    String getTransactionJson() {
        return "{" +
                " \"ts\": \"2018-12-31 18:13:20.025072 UTC\"," +
                " \"trade_id\": \"62879164621906\"," +
                " \"trade_size\": \"100\"," +
                " \"sequence_num\": \"93889601\"," +
                " \"symbol\": \"ENIA\"," +
                " \"price\": \"893\"," +
                " \"_offset\": 800.025072," +
                " \"_replay_timestamp\": \"2020-02-25 02:37:57.002507 UTC\"," +
                " \"_publish_timestamp\": \"2020-02-25 02:37:57.048754 UTC\"" +
                "}";
    }

    StockTrade getTrade() {
        StockTrade st = new StockTrade();
        st.setTimestamp(1546280000025L);
        st.setTradeId(62879164621906L);
        st.setTradeSize(100L);
        st.setSequenceNum(93889601L);
        st.setSymbol("ENIA");
        st.setPrice(893L);

        return st;
    }
}
