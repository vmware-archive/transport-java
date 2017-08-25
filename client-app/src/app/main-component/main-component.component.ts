import {Component, OnInit} from '@angular/core';
import {
    MessagebusService,
    StompConfig,
    StompClient,
    StompParser,
    StompChannel,
    StompBusCommand,
    MessageHandler
} from '@vmw/bifrost';
import {LogLevel} from "@vmw/bifrost/log";

@Component({
    selector: 'app-main-component',
    templateUrl: './main-component.component.html',
    styleUrls: ['./main-component.component.css']
})
export class MainComponentComponent implements OnInit {
    private config: StompConfig;
    private connectionStream: MessageHandler;

    public metricsChannelA: string = "metrics-a";
    public metricsChannelB: string = "metrics-b";

    public taskChannelA: string = "task-a";
    public taskChannelB: string = "task-b";

    public taskTitleA: string = "Asynchronous Task A";
    public taskTitleB: string = "Asynchronous Task B";

    messages: Array<string>;

    constructor(private bus: MessagebusService) {

    }

    private tellEveryoneTheBridgeIsReady(): void {
        this.bus.sendResponseMessage('bridge-ready', true);
    }

    ngOnInit() {

        this.bus.connectBridge(
            () => {
                this.tellEveryoneTheBridgeIsReady();
            },
            '/bifrost',
            'localhost',
            8080,
            "test",
            "password",
            false
        );
    }
}

