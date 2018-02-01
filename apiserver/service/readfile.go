package service

import (
    "io/ioutil"
    "fmt"
    "os"
    "encoding/json"
)

func getSeeds() []Seed {
    raw, err := ioutil.ReadFile("../bifrost-java/java/src/main/resources/db.json")
    if err != nil {
        fmt.Println(err.Error())
        os.Exit(1)
    }

    var c []Seed
    json.Unmarshal(raw, &c)
    return c
}
