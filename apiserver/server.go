package main

import (
    serv "./service"
    "fmt"
    "log"
    "net/http"
)

func main() {
    fmt.Println("Bifröst Micro App")
    router := serv.NewRouter()
    serv.GetAppInstance().LoadSeeds();
    log.Fatal(http.ListenAndServe(":3000", router))
}