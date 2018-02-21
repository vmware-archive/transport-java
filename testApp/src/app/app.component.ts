import {Component} from '@angular/core';
import {MessagebusService, StompService} from "@vmw/bifrost";

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'app';

    toasts = [];
    fade: boolean = true;

    constructor(private bus: MessagebusService) {
        this.bus.api.enableMonitorDump(true);
        this.bus.api.silenceLog(false);
        this.toasts = [];
    }

    listenForAlerts(): void {
        this.bus.listenStream('app-alerts')
            .handle(
                (payload) => {
                    this.toasts.push({fade: true, error: false, title: 'pop', description: 'fishes', date: Date.now(), link: 'http://www.google.com'});
                }
            );
    }

}
