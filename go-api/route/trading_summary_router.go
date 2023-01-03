package route

import (
	"github.com/damncan/data-driven-demo/controller"
	"github.com/gin-gonic/gin"
)

func TradingSummaryRoutes(incomingRoutes *gin.Engine) {
	incomingRoutes.POST("/deals", controller.PostDeals)
	incomingRoutes.GET("/tradingSummaries", controller.GetTradingSummaries)
	incomingRoutes.GET("/tradingSummaries/:baseCurrency", controller.GetTradingSummary)
}
