/**
 * Copyright(c) VMware Inc. 2019
 */
import { AbstractBase } from '@vmw/bifrost/core';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { PongRequestType, PongServiceChannel, PongServiceResponse } from './pong.service.model';

@Component({
    selector: 'ping-fabric-component',
    template: `
        <button (click)="sendPingBasic()" class="btn btn-primary">Ping (Basic)</button>
        <button (click)="sendPingFull()" class="btn btn-primary">Ping (Full)</button><br/>
        Response: {{response}}`
})
export class PingFabricComponent extends AbstractBase implements OnInit, OnDestroy {

    public response = 'nothing yet, request something!';

    constructor(private cd: ChangeDetectorRef) {
        super('PingFabricComponent');
    }

    /**
     * Send a basic ping request to the pong service.
     */
    public sendPingBasic(): void {
       this.sendPingRequest(PongRequestType.Basic);
    }

    ngOnDestroy(): void {
        this.bus.markChannelAsLocal(PongServiceChannel);
    }

    ngOnInit(): void {
        this.bus.markChannelAsGalactic(PongServiceChannel);
    }

    /**
     * Send a full ping request to the pong service.
     */
    public sendPingFull(): void {
        this.sendPingRequest(PongRequestType.Full);
    }

    private sendPingRequest(type: PongRequestType): void {
        this.bus.requestOnce(
            PongServiceChannel,
            this.fabric.generateFabricRequest(type, null))
            .handle(
                (response: any) => {
                    console.log('pizza', response);
                    this.response = response.payload;
                    this.cd.detectChanges();
                }
            );
    }
}
