import {Component} from '@angular/core';
import {MessagebusService, StompService} from "@vmw/bifrost";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'app';

    constructor(private bus: MessagebusService) {
        this.bus.api.enableMonitorDump(true);
        this.bus.api.silenceLog(false);
    }

}
