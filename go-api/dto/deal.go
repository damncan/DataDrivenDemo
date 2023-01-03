package dto

type Deal struct {
	BaseCurrency   string  `json:"base_currency"`
	QuotedCurrency string  `json:"quoted_currency"`
	Price          float64 `json:"price"`
	Volume         float64 `json:"volume"`
}
