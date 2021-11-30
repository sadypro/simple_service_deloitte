package main

import (
	"context"
	"errors"
	"fmt"
	"strings"

	"github.com/aws/aws-lambda-go/events"
	"github.com/aws/aws-lambda-go/lambda"
)

// ErrNameNotProvided is thrown when a name is not provided
var (
	HTTPMethodNotSupported = errors.New("No supported method name was provided in the HTTP body")
)

//Fuction to construct response from string request
func HandleRequest(ctx context.Context, request events.APIGatewayProxyRequest) (events.APIGatewayProxyResponse, error) {
	fmt.Printf("Body size = %d. \n", len(request.Body))
	fmt.Println("Headers:")
	for key, value := range request.Headers {
		fmt.Printf("  %s: %s\n", key, value)
	}
	//Get method
	if request.HTTPMethod == "GET" {
		fmt.Printf("GET METHOD\n")
		return events.APIGatewayProxyResponse{Body: "Welcome to the replacement api", StatusCode: 200}, nil

	} else if request.HTTPMethod == "POST" { //Post method

		fmt.Printf("POST METHOD\n")
		responseString := string(request.Body)
		responseString = strings.ReplaceAll(responseString, "Google", "Google©")
		responseString = strings.Replace(responseString, "Oracle", "Oracle©", -1)
		responseString = strings.Replace(responseString, "Microsoft", "Microsoft©", -1)
		responseString = strings.Replace(responseString, "Amazon", "Amazon©", -1)
		responseString = strings.Replace(responseString, "Deloitte", "Deloitte©", -1)

		return events.APIGatewayProxyResponse{
			StatusCode: 200,
			Body:       string(responseString),
		}, nil

	} else { //Unsupported method
		fmt.Printf("NEITHER\n")
		return events.APIGatewayProxyResponse{}, HTTPMethodNotSupported
	}
}

func main() {
	lambda.Start(HandleRequest)
}
