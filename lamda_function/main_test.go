package main

import (
	"testing"

	//main "github.com/saadgen/simple_service_deloitte"

	"github.com/aws/aws-lambda-go/events"
	"github.com/stretchr/testify/assert"
)

func TestHandler(t *testing.T) {

	tests := []struct {
		request events.APIGatewayProxyRequest
		expect  string
		err     error
	}{
		{
			// Test that the handler responds with the correct response
			// when a valid name is provided in the HTTP body
			request: events.APIGatewayProxyRequest{HTTPMethod: "GET"},
			expect:  "Welcome to the replacement api",
			err:     nil,
		},
	}

	for _, test := range tests {
		response, err := main.HandleRequest(nil, test.request)
		assert.IsType(t, test.err, err)
		assert.Equal(t, test.expect, response.Body)
	}

}
