package main

import (
	"context"
	"encoding/json"
	"fmt"
	"strings"

	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
)

type text struct {
	simpletext string `json:"text"`
}

//Function
func handleRequest(ctx context.Context, request events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	fmt.Printf("Processing request data for request %s.\n", request.RequestContext.RequestID)
	fmt.Printf("Body size = %d.\n", len(request.Body))
	var Text text
	err := json.Unmarshal([]byte(request.Body), &Text)
	responseString := string(Text.simpletext)
	if err != nil {
		return events.APIGatewayProxyResponse{
			StatusCode: 400,
			Body:       "Invalid payload",
		}, nil
	}
	//responseString := string(request.Body)
	responseString = strings.ReplaceAll(responseString, "Google", "Google©")
	responseString = strings.Replace(responseString, "Oracle", "Oracle©", -1)
	responseString = strings.Replace(responseString, "Microsoft", "Microsoft©", -1)
	responseString = strings.Replace(responseString, "Amazon", "Amazon©", -1)
	responseString = strings.Replace(responseString, "Deloitte", "Deloitte©", -1)
	response, err := json.Marshal(responseString)
	if err != nil {
		return events.APIGatewayProxyResponse{
			StatusCode: 500,
			Body:       err.Error(),
		}, nil
	}
	fmt.Println("Headers:")
	for key, value := range request.Headers {
		fmt.Printf("    %s: %s\n", key, value)
	}

	return events.APIGatewayProxyResponse{
		Body:       string(response),
		StatusCode: 200,
	}, nil
}

func main() {
	lambda.Start(handleRequest)
}
