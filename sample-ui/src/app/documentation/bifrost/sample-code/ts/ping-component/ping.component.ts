/**
 * Copyright(c) VMware Inc. 2019
 */
import { AbstractBase } from '@vmw/bifrost/core';
import { Component, OnInit } from '@angular/core';
import { PongRequestType, PongServiceChannel, PongServiceResponse } from './pong.service.model';
import { APIRequest } from '@vmw/bifrost';
import { GeneralUtil } from '@vmw/bifrost/util/util';

@Component({
    selector: 'ping-component',
    template: `
        <button (click)="sendPingBasic()" class="btn btn-primary">Ping (Basic)</button>
        <button (click)="sendPingFull()" class="btn btn-primary">Ping (Full)</button><br/>
        Response: {{response}}`
})
export class PingComponent extends AbstractBase implements OnInit {

    public response = 'nothing yet, request something!';

    constructor() {
        super('PingComponent');
    }

    /**
     * Send a basic ping request to the pong service.
     */
    public sendPingBasic(): void {
        const request = this.fabric.generateFabricRequest(PongRequestType.Basic, 'basic ping');
        this.sendPingRequest(request);
    }

    /**
     * Send a full ping request to the pong service.
     */
    public sendPingFull(): void {
        const request = this.fabric.generateFabricRequest(PongRequestType.Full, 'full ping');
        this.sendPingRequest(request);
    }

    private sendPingRequest(request: APIRequest<string>): void {
        this.bus.requestOnceWithId(GeneralUtil.genUUIDShort(), PongServiceChannel, request)
            .handle(
                (response: PongServiceResponse) => {
                    this.response = response.payload;
                }
            );
    }

    ngOnInit(): void {}
}
