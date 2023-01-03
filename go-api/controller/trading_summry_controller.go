package controller

import (
	"context"
	"encoding/json"
	"fmt"
	"github.com/damncan/data-driven-demo/conf"
	database "github.com/damncan/data-driven-demo/database_connection"
	"github.com/damncan/data-driven-demo/dto"
	"github.com/damncan/data-driven-demo/sync"
	"github.com/gin-gonic/gin"
	"go.mongodb.org/mongo-driver/bson"
	"log"
	"net/http"
	"time"
)

var tsCollection = database.OpenCollection()

func PostDeals(c *gin.Context) {
	// prepare data which is going to write into kafka
	var newDeal dto.Deal
	if err := c.BindJSON(&newDeal); err != nil {
		fmt.Println(err.Error())
		return
	}
	jsondata, err := json.Marshal(newDeal)
	if err != nil {
		log.Fatal(err)
	}

	// kafka producer: write data into kafka
	sync.Producer(conf.KFK_TOPIC, string(jsondata))

	c.IndentedJSON(http.StatusCreated, newDeal)
}

func GetTradingSummaries(c *gin.Context) {
	// build up connection with mongoDB
	var ctx, cancel = context.WithTimeout(context.Background(), 100*time.Second)
	defer cancel()

	// execute mongoDB query to get cursor
	result, err := tsCollection.Find(context.TODO(), bson.M{})
	if err != nil {
		log.Fatal(err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error occurred while listing the trading summaries"})
		return
	}

	// transfer the cursor and decode it, then push into the variable tradingSummaries
	var tradingSummaries []bson.M
	if err = result.All(ctx, &tradingSummaries); err != nil {
		log.Fatal(err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error occurred while get data from mongoDB"})
		return
	}

	c.IndentedJSON(http.StatusOK, tradingSummaries)
}

func GetTradingSummary(c *gin.Context) {
	// build up connection with mongoDB
	var ctx, cancel = context.WithTimeout(context.Background(), 100*time.Second)
	defer cancel()

	// execute mongoDB query to get cursor
	// transfer the cursor and decode it, then push into the variable tradingSummaries
	baseCurrency := c.Param("baseCurrency")
	var tradingSummary dto.TradingSummary
	err := tsCollection.FindOne(ctx, bson.M{"baseCurrency": baseCurrency}).Decode(&tradingSummary)
	if err != nil {
		log.Fatal(err)
		c.JSON(http.StatusInternalServerError, gin.H{"error": "error occurred while fetching the trading summary by base-currency"})
		return
	}

	c.JSON(http.StatusOK, tradingSummary)
}
