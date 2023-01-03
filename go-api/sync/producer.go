package sync

import (
	"github.com/Shopify/sarama"
	"github.com/damncan/data-driven-demo/conf"
	"log"
)

func Producer(topic string, jsonData string) {
	// prepare kfk connection config
	config := sarama.NewConfig()
	config.Producer.Return.Successes = true
	config.Producer.Return.Errors = true

	// build up a kafka producer
	producer, err := sarama.NewSyncProducer([]string{conf.KFK_HOST}, config)
	if err != nil {
		log.Fatal("NewSyncProducer err:", err)
	}
	defer func(producer sarama.SyncProducer) {
		err := producer.Close()
		if err != nil {
			log.Fatal(err)
		}
	}(producer)

	// produce message into kafka
	var successes, errors int
	msg := &sarama.ProducerMessage{Topic: topic, Key: nil, Value: sarama.StringEncoder(jsonData)}
	partition, offset, err := producer.SendMessage(msg)
	if err != nil {
		log.Printf("SendMessage err:%v\n ", err)
		errors++
	}
	successes++
	log.Printf("[Producer] partitionid: %d; offset:%d, value: %s\n", partition, offset, msg)
}
