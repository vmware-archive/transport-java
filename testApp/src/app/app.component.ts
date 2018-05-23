import { Component } from '@angular/core';
import { EventBus, BifrostEventBus } from '@vmw/bifrost';
import { LogLevel } from '../../../../bifrost-symlink/log';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'app';

    toasts = [];
    fade = true;
    bus: EventBus;
    constructor() {
        this.bus = new BifrostEventBus(LogLevel.Debug);
        this.bus.api.enableMonitorDump(true);
        this.bus.api.silenceLog(false);
        this.toasts = [];
        this.listenForAlerts();
    }

    listenForAlerts(): void {
        this.bus.listenStream('app-alerts')
            .handle(
                (payload: any) => {
                    this.toasts.push(
                        {
                            fade: false,
                            error: payload.error,
                            title: payload.title,
                            description: payload.description,
                            date: Date.now(),
                            link: 'http://www.google.com'
                        });
                }
            );
    }

}
