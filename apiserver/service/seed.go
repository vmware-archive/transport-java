package service

import (
    "net/http"
    "encoding/json"
)

type ModelData struct {
    Seeds []Seed `json:"seed"`
}

type Seed struct {
    Id      int    `json:"id"`
    Type    string `json:"type"`
    Planted int64  `json:"planted"`
}

func GetSeeds(w http.ResponseWriter, r *http.Request) {
    w.Header().Set("Content-Type", "application/json; charset=UTF-8")
    w.WriteHeader(http.StatusOK)
    json.NewEncoder(w).Encode(GetAppInstance().Seeds)
}

func PlantSeed(w http.ResponseWriter, req *http.Request) {
    w.Header().Set("Content-Type", "application/json; charset=UTF-8")
    var seed Seed
    json.NewDecoder(req.Body).Decode(&seed)
    newSeed := GetAppInstance().AddSeed(seed.Type)
    w.WriteHeader(http.StatusOK)
    json.NewEncoder(w).Encode(newSeed)
}

func KillPlant(w http.ResponseWriter, req *http.Request) {
    w.Header().Set("Content-Type", "application/json; charset=UTF-8")
    var seed Seed
    json.NewDecoder(req.Body).Decode(&seed)
    if GetAppInstance().KillPlant(seed) {
        w.WriteHeader(http.StatusOK)
    } else {
        w.WriteHeader(http.StatusNotFound)
    }

}
