import {Component, OnInit} from '@angular/core';
import {
    MessagebusService,
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

