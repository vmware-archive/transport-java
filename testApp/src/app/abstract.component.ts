import { EventBus } from '@vmw/bifrost';
import { ServiceApi } from './service.mixin';
import { OnInit } from '@angular/core';

export abstract class AbstractComponent extends ServiceApi implements OnInit {

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

    constructor() {
        super();
    }

    private tellEveryoneTheBridgeIsReady(): void {
        this.bus.sendResponseMessage('bridge-ready', true);
    }

    protected sendAlert(error: boolean, title: string, description: string): void {
        this.bus.sendResponseMessage('app-alerts', {error: error, title: title, description: description});
    }
}
