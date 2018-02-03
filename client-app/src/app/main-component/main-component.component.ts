import { Component, OnInit } from '@angular/core';
import { MessagebusService, StompParser } from '@vmw/bifrost';
import { AbstractComponent } from '../abstract.component';
import { GalacticRequest } from '@vmw/bifrost/bus/model/request.model';
import { GalacticResponse } from '@vmw/bifrost/bus/model/response.model';





@Component({
    selector: 'app-main-component',
    templateUrl: './main-component.component.html',
    styleUrls: ['./main-component.component.css']
})
export class MainComponentComponent extends AbstractComponent implements OnInit {

    public metricsChannelA: string = "metrics-a";
    public metricsChannelB: string = "metrics-b";

    public taskChannelA: string = "task-a";
    public taskChannelB: string = "task-b";

    public taskTitleA: string = "Asynchronous Task A";
    public taskTitleB: string = "Asynchronous Task B";

    constructor() {
        super();
    }

    private tellEveryoneTheBridgeIsReady(): void {
        this.bus.sendResponseMessage('bridge-ready', true);
        //this.listenForSeeds();
    }

    ngOnInit() {

        this.bus.connectBridge(
            () => {
                this.tellEveryoneTheBridgeIsReady();
            },
            '/bifrost',
            '/topic',
            '/queue',
            1,
            'localhost',
            8080,
            '/pub'
        );
    }

    // listenForSeeds(): void {
    //     let handler = this.bus.listenGalacticStream("service-seed");
    //     handler.handle(
    //         (metric) => {
    //             console.log('got a response!', metric);
    //         }
    //     );
    // }

    public plantSeed(type) {

        let seedRequest = {
            id: StompParser.genUUID(),
            created: Date.now(),
            version: 1,
            type: "PlantSeed",
            payload: { type: type }
        }

        this.bus.sendGalacticMessage("service-seed", seedRequest);

    }

    public getSeeds(): void {

        const request = new GalacticRequest("GetSeeds", null, StompParser.genUUID(), 1);
    
        this.bus.requestGalactic('service-seed', request,
            (seeds: GalacticResponse<any>) => {
                console.log(seeds.payload);
            }    
        );
    }

}

