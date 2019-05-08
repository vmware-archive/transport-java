/**
 * Copyright(c) VMware Inc. 2019
 */
import { AbstractBase } from '@vmw/bifrost/core';
import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { PongRequestType, PongServiceChannel, PongServiceResponse } from './pong.service.model';
import { APIRequest } from '@vmw/bifrost';
import { GeneralUtil } from '@vmw/bifrost/util/util';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { PongService } from './pong.service';

@Component({
    selector: 'ping-component',
    template: `
        <button (click)="sendPingBasic()" class="btn btn-primary">Ping (Basic)</button>
        <button (click)="sendPingFull()" class="btn btn-primary">Ping (Full)</button><br/>
<!--        <button (click)="switchFabric()" class="btn btn-primary">Switch Fab</button><br/>-->
<!--        <button (click)="switchLocal()" class="btn btn-primary">Switch Local</button><br/>-->
        Response: {{response}}`
})
export class PingComponent extends AbstractBase implements OnInit {

    public response = 'nothing yet, request something!';
    private pongService: PongService;

    constructor(private cd: ChangeDetectorRef) {
        super('PingComponent');
    }

    switchFabric(): void {
        this.pongService.offline();
        this.bus.markChannelAsGalactic(PongServiceChannel);

    }

    switchLocal(): void {
        this.pongService.online();
        this.bus.markChannelAsLocal(PongServiceChannel);
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
                    this.cd.detectChanges();
                }
            );
    }

    ngOnInit(): void {
        this.pongService = ServiceLoader.getService(PongService);
    }
}
