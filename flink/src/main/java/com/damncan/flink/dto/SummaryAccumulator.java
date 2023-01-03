package com.damncan.flink.dto;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class SummaryAccumulator {
    public String key;
    public double turnover;
    public double volume;
    public double count;
    public ConcurrentHashMap<String, HashMap<String, Double>> currencyMap = new ConcurrentHashMap<>();
}