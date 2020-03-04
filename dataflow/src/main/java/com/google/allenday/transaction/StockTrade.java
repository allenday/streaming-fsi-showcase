package com.google.allenday.transaction;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.allenday.input.DeserializeTimestamp;
import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.apache.avro.reflect.Nullable;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;

@DefaultCoder(AvroCoder.class)
@JsonIgnoreProperties(ignoreUnknown = true)
public class StockTrade {

    @Nullable
    @JsonProperty("ts")
    @JsonDeserialize(using = DeserializeTimestamp.class)
    private Long timestamp;

    @Nullable
    @JsonProperty("trade_id")
    private Long tradeId;

    @Nullable
    @JsonProperty("trade_size")
    private Long tradeSize;

    @Nullable
    @JsonProperty("sequence_num")
    private Long sequenceNum;

    @Nullable
    private String symbol;

    @Nullable
    private Long price;

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTradeId() {
        return tradeId;
    }

    public void setTradeId(Long tradeId) {
        this.tradeId = tradeId;
    }

    public Long getTradeSize() {
        return tradeSize;
    }

    public void setTradeSize(Long tradeSize) {
        this.tradeSize = tradeSize;
    }

    public Long getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(Long sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Long getPrice() {
        return price;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        StockTrade that = (StockTrade) o;
        return Objects.equal(timestamp, that.timestamp) &&
                Objects.equal(tradeId, that.tradeId) &&
                Objects.equal(tradeSize, that.tradeSize) &&
                Objects.equal(sequenceNum, that.sequenceNum) &&
                Objects.equal(symbol, that.symbol) &&
                Objects.equal(price, that.price);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(timestamp, tradeId, tradeSize, sequenceNum, symbol, price);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("timestamp", timestamp)
                .add("tradeId", tradeId)
                .add("tradeSize", tradeSize)
                .add("sequenceNum", sequenceNum)
                .add("symbol", symbol)
                .add("price", price)
                .toString();
    }
}
