package main

import (
	"fmt"
	"io/ioutil"
	"log"
	"net/http"
	"strings"

	"github.com/gorilla/mux"
)

func homePage(w http.ResponseWriter, r *http.Request) {
	fmt.Fprintf(w, "Welcome to the HomePage!, Service is Live")
	fmt.Println("Endpoint Hit: homePage")
}

func replacedfaang(w http.ResponseWriter, r *http.Request) {
	// get the body of our POST request
	// replace the keywords
	reqBody, _ := ioutil.ReadAll(r.Body)
	responseString := string(reqBody)
	responseString = strings.ReplaceAll(responseString, "Google", "Google©")
	responseString = strings.Replace(responseString, "Oracle", "Oracle©", -1)
	responseString = strings.Replace(responseString, "Microsoft", "Microsoft©", -1)
	responseString = strings.Replace(responseString, "Amazon", "Amazon©", -1)
	responseString = strings.Replace(responseString, "Deloitte", "Deloitte©", -1)
	fmt.Println(responseString)
	fmt.Fprint(w, responseString)
}

func handleRequests() {
	// creates a new instance of a mux router
	myRouter := mux.NewRouter().StrictSlash(true)
	// replace http.HandleFunc with myRouter.HandleFunc
	myRouter.HandleFunc("/live", homePage)
	myRouter.HandleFunc("/replace", replacedfaang).Methods("POST")
	// finally, stating router
	log.Fatal(http.ListenAndServe(":8080", myRouter))
}

func main() {
	fmt.Println("Rest API v0.1 - Mux Routers")
	handleRequests()
}
