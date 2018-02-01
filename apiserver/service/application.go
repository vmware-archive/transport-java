package service

import (
    "sync"
    "math/rand"
    "time"
)

type ApplicationState struct {
    Seeds []Seed
}

var appInstance *ApplicationState
var once sync.Once

func GetAppInstance() *ApplicationState {
    once.Do(func() {
        appInstance = &ApplicationState{}
    })
    return appInstance
}

func (n *ApplicationState) LoadSeeds() {
    n.Seeds = getSeeds()
}

func (n *ApplicationState) AddSeed(seedType string) Seed {
    seed := Seed{rand.Intn(9999), seedType, time.Now().Unix()}
    n.Seeds = append(n.Seeds, seed)
    return seed
}

func (n *ApplicationState) KillPlant(seed Seed) bool {
    x := 0
    for _, v := range n.Seeds {
        if v.Id == seed.Id {
            n.Seeds = append(n.Seeds[:x], n.Seeds[x+1:]...)
            return true
        }
        x++;
    }
    return false
}
