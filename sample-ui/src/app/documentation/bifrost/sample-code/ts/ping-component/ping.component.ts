/**
 * Copyright(c) VMware Inc. 2019
 */
import { AbstractBase } from '@vmw/bifrost/core';
import { Component } from '@angular/core';
import { PongRequestType, PongServiceChannel, PongServiceRequest, PongServiceResponse } from './pong.service.model';

@Component({
    selector: 'ping-component',
    template: `
        <button (click)="sendPingBasic()" class="btn btn-primary">Ping (Basic)</button>
        <button (click)="sendPingFull()" class="btn btn-primary">Ping (Full)</button><br/>
        Response: {{response}}`
})
export class PingComponent extends AbstractBase {

    public response = 'nothing yet, request something!';

    constructor() {
        super('PingComponent');
    }

    /**
     * Send a basic ping request to the pong service.
     */
    public sendPingBasic(): void {

        const request: PongServiceRequest = {
            command: PongRequestType.Basic,
            message: 'basic ping'
        };

        this.sendPingRequest(request);

    }

    /**
     * Send a full ping request to the pong service.
     */
    public sendPingFull(): void {

        const request: PongServiceRequest = {
            command: PongRequestType.Full,
            message: 'full ping'
        };

        this.sendPingRequest(request);

    }

    private sendPingRequest(request: PongServiceRequest): void {
        this.bus.requestOnce(PongServiceChannel, request)
            .handle(
                (response: PongServiceResponse) => {
                    this.response = response.value;
                }
            );
    }

}
