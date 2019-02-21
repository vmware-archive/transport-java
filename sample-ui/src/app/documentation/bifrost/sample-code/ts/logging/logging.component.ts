/**
 * Copyright(c) VMware Inc. 2019
 */
import { AbstractBase } from '@vmw/bifrost/core';
import { Component } from '@angular/core';
import { LogLevel } from '@vmw/bifrost/log';

@Component({
    selector: 'appfab-log-component',
    template: `
        <button (click)="logMessages()" class="btn btn-primary-outline">Log To Console</button>`
})
export class LogComponent extends AbstractBase {

    constructor() {
        super('LogComponent');

        // turn on verbose logging for our bus.
        this.bus.api.setLogLevel(LogLevel.Verbose);
    }

    /**
     * Log messages to the console.
     */
    public logMessages(): void {
        this.log.verbose('this is a Verbose log message');
        this.log.debug('this is a Debug log message');
        this.log.info('this is an Info log message');
        this.log.warn('this is a Warn log message');
        this.log.error('this is an Error log message');
    }
}
