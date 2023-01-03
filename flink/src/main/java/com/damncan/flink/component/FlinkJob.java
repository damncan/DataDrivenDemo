package com.damncan.flink.component;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.flink.api.common.eventtime.WatermarkStrategy;
import org.apache.flink.api.java.tuple.Tuple5;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.windowing.assigners.GlobalWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.triggers.ContinuousProcessingTimeTrigger;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.Serializable;
import java.time.Duration;
import java.util.HashMap;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This component starts a flink job, and its duties include:
 * <ul>
 * <li> Source: Polling trading result from kafka (topic name: tradingResult) and assigning watermark for each record when each record has been pooled.
 * <li> Transformation: Dividing the original stream into several sub-stream according to it's base-currency. Then use a time window to aggregate the summary of trading, such as the total turnover and volume of every currency, every 5 seconds.
 * <li> Sink: Storing the analysing result into MongoDB.
 * </ul>
 *
 * @author Ian Zhong (damncan)
 * @since 14 January 2023
 */
@Component
@ConditionalOnProperty(name = "flink.job.summary", havingValue = "true", matchIfMissing = false)
public class FlinkJob implements Serializable {
    @Value(value = "${kafka.bootstrapAddress}")
    private String kafkaBootstrapAddress;
    @Value(value = "${kafka.targetTopic}")
    private String targetTopic;

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void execute() {
        // Initialization
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // Source
        Properties properties = new Properties();
        properties.setProperty("bootstrap.servers", kafkaBootstrapAddress);
        properties.setProperty("group.id", "1");

        FlinkKafkaConsumer<ObjectNode> consumer = new FlinkKafkaConsumer<>(targetTopic, new KafkaDeserializer(), properties);
        consumer.setStartFromEarliest();

        consumer.assignTimestampsAndWatermarks(WatermarkStrategy.<ObjectNode>forMonotonousTimestamps().withIdleness(Duration.ofSeconds(5)));
        // consumer.assignTimestampsAndWatermarks(WatermarkStrategy.<ObjectNode>forBoundedOutOfOrderness(Duration.ofSeconds(3)).withIdleness(Duration.ofSeconds(5)));

        DataStream<ObjectNode> sourceStream = env.addSource(consumer);

        // Transformation
        SingleOutputStreamOperator<Tuple5<String, Double, Double, Double, ConcurrentHashMap<String, HashMap<String, Double>>>> process = sourceStream
                .map(k -> k.get("value"))
                .keyBy(k -> k.get("base_currency").asText())
                .window(GlobalWindows.create())
                // .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                .trigger(ContinuousProcessingTimeTrigger.of(Time.seconds(5)))
                .aggregate((SummaryAggregator) applicationContext.getBean("summaryAggregator"))
                .setParallelism(2);

        // Sink
        process.addSink((SummarySink) applicationContext.getBean("summarySink"));

        // Execute Job
        new Thread(() -> {
            try {
                env.execute("Spring & Flink Integration");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }
}
