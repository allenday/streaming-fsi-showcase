package com.google.allenday.calculation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

@DefaultCoder(AvroCoder.class)
public class Candlestick {
    Candlestick() {
    }

    Candlestick(long open, long close, long low, long high) {
        this.open = open;
        this.close = close;
        this.low = low;
        this.high = high;
    }

    private long open;

    private long close;

    private long low;

    private long high;

    public long getOpen() {
        return open;
    }

    public void setOpen(long open) {
        this.open = open;
    }

    public long getClose() {
        return close;
    }

    public void setClose(long close) {
        this.close = close;
    }

    public long getLow() {
        return low;
    }

    public void setLow(long low) {
        this.low = low;
    }

    public long getHigh() {
        return high;
    }

    public void setHigh(long high) {
        this.high = high;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Candlestick that = (Candlestick) o;
        return Objects.equal(open, that.open) &&
                Objects.equal(close, that.close) &&
                Objects.equal(low, that.low) &&
                Objects.equal(high, that.high);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(open, close, low, high);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("open", open)
                .add("close", close)
                .add("low", low)
                .add("high", high)
                .toString();
    }
}
