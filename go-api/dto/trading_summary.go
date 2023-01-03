package dto

type TradingSummary struct {
	BaseCurrency string             `json:"base_currency"`
	Turnover     float64            `json:"turnover"`
	Volume       float64            `json:"volume"`
	CurrencyMap  map[string]float64 `json:"currencyMap"`
}
