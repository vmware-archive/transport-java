import { Component } from '@angular/core';
import { MessagebusService, StompService } from '@vmw/bifrost';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.css']
})
export class AppComponent {
    title = 'app';

    toasts = [];
    fade = true;

    constructor(private bus: MessagebusService) {
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
