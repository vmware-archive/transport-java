package main

import (
    serv "./service"
    "fmt"
    "log"
    "net/http"
)

func main() {
    fmt.Println("Bifröst Micro API Server")
    router := serv.NewRouter()
    serv.GetAppInstance().LoadSeeds();
    log.Fatal(http.ListenAndServe(":3000", router))
}