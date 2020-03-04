package com.google.allenday.input;

import org.apache.beam.sdk.schemas.Schema;

public class InputSchema {
    public static Schema schema = Schema.builder()
            .addStringField("sorting_key")
            .addInt64Field("price")
            .build();
}
