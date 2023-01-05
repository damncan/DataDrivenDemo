package conf

// KAFKA
const (
	KFK_HOST = "host.docker.internal:9092"
	//KFK_HOST  = "localhost:9092"
	KFK_TOPIC = "tradingResult"
)

// MONGODB
const (
	MONGO_HOST = "host.docker.internal:27017"
	//MONGO_HOST       = "localhost:27017"
	MONGO_DADABASE   = "trading"
	MONGO_COLLECTION = "summary"
)
