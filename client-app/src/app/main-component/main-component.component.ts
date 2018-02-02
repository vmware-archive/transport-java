import {Component, OnInit} from '@angular/core';
import {
    MessagebusService, StompParser,
} from '@vmw/bifrost';

@Component({
    selector: 'app-main-component',
    templateUrl: './main-component.component.html',
    styleUrls: ['./main-component.component.css']
})
export class MainComponentComponent implements OnInit {

    public metricsChannelA: string = "metrics-a";
    public metricsChannelB: string = "metrics-b";

    public taskChannelA: string = "task-a";
    public taskChannelB: string = "task-b";

    public taskTitleA: string = "Asynchronous Task A";
    public taskTitleB: string = "Asynchronous Task B";

    constructor(private bus: MessagebusService) {

    }

    private tellEveryoneTheBridgeIsReady(): void {
        this.bus.sendResponseMessage('bridge-ready', true);
        this.listenForSeeds();
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
            8080
        );
    }

    listenForSeeds(): void {
        let handler = this.bus.listenGalacticStream("service-seed");
        handler.handle(
            (metric) => {
                console.log('got a response!', metric);
            }
        );
    }

    public plantSeed(type) {
       
        let seedRequest = {
            id: StompParser.genUUID(),
            created: Date.now(),
            version: 1,
            type: "PlantSeed",
            payload: {type: type }
        }

        this.bus.sendGalacticMessage("/pub/service-seed", seedRequest);

    }

    public getSeeds(): void {
    
        let seedRequest = {
            id: StompParser.genUUID(),
            created: Date.now(),
            version: 1,
            type: "GetSeeds",
            payload: null
        }

        this.bus.sendGalacticMessage("/pub/service-seed", seedRequest);
    }

}

