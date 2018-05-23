import { Component, OnInit } from '@angular/core';
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
         this.SeedService.plantSeed(
            new Seed(type),
            (seeds: Seed[]) => {
                this.sendAlert(false, 'seed planted!', 'the seed was planted and took x ms');
                console.log('seed planted!', seeds);
            }
        );
    }

    public killAPlant(seed: Seed) {
        this.SeedService.killPlant(
           seed,
           (seeds: Seed[]) => {
            this.sendAlert(false, 'seed killed!', 'the seed was killed and took x ms');
               console.log('we killed a plant', seeds);
           }
       );
   }

    public getSeedList(): void {
        this.SeedService.getSeeds(
            (seeds: Seed[]) => {
                this.sendAlert(false, 'seed list updated', 'the list off seeds was fetched and took x ms');
                console.log('hey hey we got some seeds', seeds);
            }
        );
    }
}