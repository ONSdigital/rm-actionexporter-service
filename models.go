package main

import (
	"github.com/jinzhu/gorm"
	"time"
)

type Address struct {
	gorm.Model
}
type Contact struct {
	gorm.Model
}

type ActionRequestModel struct {
	gorm.Model
	ID               string `gorm:"column:actionrequestpk"`
	SampleUnitRef    string
	ExportJobId      string
	ReturnByDate     string
	ExerciseRef      string
	SurveyRef        string
	DateStored       time.Time
	Iac              string
	CaseRef          string
	Priority         string
	CaseId           string
	Address          Address
	Contact          Contact
	CaseGroupStatus  string
	EnrolmentStatus  string
	RespondentStatus string
	Region           string
	LegalBasis       string
	QuestionSet      string
	ActionType       string `gorm:"column:actiontypename"`
	ActionPlan       string `gorm:"column:actionplanname"`
	ResponseRequired bool
	ActionId         string
}



