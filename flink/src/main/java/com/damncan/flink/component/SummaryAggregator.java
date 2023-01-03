package com.damncan.flink.component;

import com.damncan.flink.dto.SummaryAccumulator;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.flink.api.common.functions.AggregateFunction;
import org.apache.flink.api.java.tuple.Tuple5;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This component defines the logic of aggregating each element in the stream.
 *
 * @author Ian Zhong (damncan)
 * @since 14 January 2023
 */
@Component
@ConditionalOnProperty(name = "flink.job.summary", havingValue = "true", matchIfMissing = false)
public class SummaryAggregator implements AggregateFunction<JsonNode, SummaryAccumulator, Tuple5<String, Double, Double, Double, ConcurrentHashMap<String, HashMap<String, Double>>>> {
    @Override
    public SummaryAccumulator createAccumulator() {
        return new SummaryAccumulator();
    }

    @Override
    public SummaryAccumulator add(JsonNode value, SummaryAccumulator accumulator) {
        // prepare total summary data
        accumulator.key = value.get("base_currency").asText();
        accumulator.turnover += value.get("price").asDouble();
        accumulator.volume += value.get("volume").asDouble();
        accumulator.count++;

        // calculate each quoted currency's summary data, and store them in a concurrencyMap
        String quotedCurrency = value.get("quoted_currency").asText();
        HashMap<String, Double> eachSum = accumulator.currencyMap.getOrDefault(quotedCurrency, new HashMap<>());
        if (eachSum.size() == 0) {
            eachSum = new HashMap<>();
            eachSum.put("turnover", value.get("price").asDouble());
            eachSum.put("volume", value.get("volume").asDouble());
            eachSum.put("count", 1.0);
        } else {
            eachSum.put("turnover", eachSum.get("turnover") + value.get("price").asDouble());
            eachSum.put("volume", eachSum.get("volume") + value.get("volume").asDouble());
            eachSum.put("count", eachSum.get("count") + 1.0);
        }
        accumulator.currencyMap.put(quotedCurrency, eachSum);

        return accumulator;
    }

    @Override
    public Tuple5<String, Double, Double, Double, ConcurrentHashMap<String, HashMap<String, Double>>> getResult(SummaryAccumulator accumulator) {
        return new Tuple5<>(accumulator.key, accumulator.turnover, accumulator.volume, accumulator.count, accumulator.currencyMap);
    }

    @Override
    @Deprecated
    public SummaryAccumulator merge(SummaryAccumulator a, SummaryAccumulator b) {
        a.turnover += b.turnover;
        a.volume += b.volume;
        a.count += b.count;
        // TODO - merge b.currencyMap into a.currencyMap
        return a;
    }
}
