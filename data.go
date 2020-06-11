package main

type ActionInstruction struct {
	ActionRequest
	ActionAddress
	ActionContact
}

type ActionRequest struct {

}


type ActionContact struct {
	Title string `xml:"title"`
	Forename string `xml:"forename"`
	Surname string `xml:"surname"`
	PhoneNumber string `xml:"phoneNumber"`
	EmailAddress string `xml:"emailAddress"`
	RuName string `xml:ruName`
	TradingStyle string `xml:ruName`
}

type ActionAddress struct {

}