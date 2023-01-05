package main

import (
	"github.com/damncan/data-driven-demo/route"
	"github.com/gin-gonic/gin"
)

func main() {
	router := gin.Default()
	route.TradingSummaryRoutes(router)
	router.Run(":8081")
}
