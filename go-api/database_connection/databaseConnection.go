package database

import (
	"context"
	"fmt"
	"github.com/damncan/data-driven-demo/conf"
	"go.mongodb.org/mongo-driver/mongo"
	"go.mongodb.org/mongo-driver/mongo/options"
	"log"
	"time"
)

var client = DBinstance()

func DBinstance() *mongo.Client {
	client, err := mongo.NewClient(options.Client().ApplyURI("mongodb://" + conf.MONGO_HOST))
	if err != nil {
		log.Fatal(err)
	}

	ctx, cancel := context.WithTimeout(context.Background(), 10*time.Second)
	defer cancel()

	err = client.Connect(ctx)
	if err != nil {
		log.Fatal(err)
	}
	fmt.Println("connected to mongodb")
	return client
}

func OpenCollection() *mongo.Collection {
	var collection = client.Database(conf.MONGO_DADABASE).Collection(conf.MONGO_COLLECTION)
	return collection
}
