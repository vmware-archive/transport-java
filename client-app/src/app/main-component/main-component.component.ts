import { Component, OnInit } from '@angular/core';
import { MessagebusService, StompParser } from '@vmw/bifrost';
import { AbstractComponent } from '../abstract.component';
import { GalacticRequest } from '@vmw/bifrost/bus/model/request.model';
import { GalacticResponse } from '@vmw/bifrost/bus/model/response.model';
import { Seed } from '../service.model';

@Component({
    selector: 'app-main-component',
    templateUrl: './main-component.component.html',
    styleUrls: ['./main-component.component.css']
})
export class MainComponentComponent extends AbstractComponent implements OnInit {

    constructor() {
        super();
    }

    ngOnInit() {
        super.ngOnInit();
    }

    public plantNewSeed(type: string) {
         this.plantSeed(
            new Seed(type),
            (seeds: Seed[]) => {
                console.log('seed planted!', seeds);
            }
        );
    }

    public killAPlant(seed: Seed) {
        this.killPlant(
           seed,
           (seeds: Seed[]) => {
               console.log('we killed a plant', seeds);
           }
       );
   }

    public getSeedList(): void {
        this.getSeeds(
            (seeds: Seed[]) => {
                console.log('hey hey we got some seeds', seeds);
            }
        );
    }
}

