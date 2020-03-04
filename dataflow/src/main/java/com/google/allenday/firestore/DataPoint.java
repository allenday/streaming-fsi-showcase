package com.google.allenday.firestore;

import com.google.allenday.calculation.Candlestick;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

@DefaultCoder(AvroCoder.class)
public class DataPoint {
    private Long timestamp;
    private Candlestick candlestick;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Candlestick getCandlestick() {
        return candlestick;
    }

    public void setCandlestick(Candlestick candlestick) {
        this.candlestick = candlestick;
    }
}
