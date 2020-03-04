package com.google.allenday.calculation;

import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.values.Row;

public class CombineCandlestickFn extends Combine.CombineFn<Row, CombineCandlestickFn.Accum, Candlestick> {
    @DefaultCoder(AvroCoder.class)
    static class Accum {
        @Nullable
        String openKey;
        @Nullable
        String closeKey;
        @Nullable
        Long open;
        @Nullable
        Long close;
        @Nullable
        Long low;
        @Nullable
        Long high;

        Accum() {
        }

        Accum(String openKey, String closeKey, long open, long close, long low, long high) {
            this.openKey = openKey;
            this.closeKey = closeKey;
            this.open = open;
            this.close = close;
            this.low = low;
            this.high = high;
        }

        Accum merge(Accum input) {
            if (input.low != null) {
                if (low == null) {
                    low = input.low;
                } else {
                    low = Math.min(low, input.low);
                }
            }

            if (input.high != null) {
                if (high == null) {
                    high = input.high;
                } else {
                    high = Math.max(high, input.high);
                }
            }

            if (input.openKey != null) {
                if (openKey == null) {
                    openKey = input.openKey;
                    open = input.open;
                } else {
                    if (input.openKey.compareTo(openKey) < 0) {
                        openKey = input.openKey;
                        open = input.open;
                    }
                }
            }

            if (input.closeKey != null) {
                if (closeKey == null) {
                    closeKey = input.closeKey;
                    close = input.close;
                } else {
                    if (input.closeKey.compareTo(closeKey) > 0) {
                        closeKey = input.closeKey;
                        close = input.close;
                    }
                }
            }

            return this;
        }
    }

    @Override
    public Accum createAccumulator() {
        return new Accum();
    }

    @Override
    public Accum addInput(Accum accum, Row input) {
        String sortKey = input.getString("sorting_key");
        Long price = input.getInt64("price");

        Accum inputAccum = new Accum(
                sortKey, sortKey, price, price, price, price
        );

        return accum.merge(inputAccum);
    }

    @Override
    public Accum mergeAccumulators(Iterable<Accum> accums) {
        Accum merged = createAccumulator();
        for (Accum accum : accums) {
            merged.merge(accum);
        }

        return merged;
    }

    @Override
    public Candlestick extractOutput(Accum accum) {
        return new Candlestick(accum.open, accum.close, accum.low, accum.high);
    }
}
