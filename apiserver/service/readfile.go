package service

import (
    "io/ioutil"
    "fmt"
    "os"
    "encoding/json"
)

func getSeeds() []Seed {
    raw, err := ioutil.ReadFile("../java/src/main/resources/db.json")
    if err != nil {
        fmt.Println(err.Error())
        os.Exit(1)
    }

    var c ModelData
    json.Unmarshal(raw, &c)
    return c.Seeds
}
