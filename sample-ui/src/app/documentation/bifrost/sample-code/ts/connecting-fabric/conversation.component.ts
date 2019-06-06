/**
 * Copyright(c) VMware Inc. 2019
 */
import { ChangeDetectorRef, Component, Input, OnDestroy, OnInit } from '@angular/core';
import { ClrLoadingState } from '@clr/angular';
import { BaseDocsComponent } from '../../../../base.docs.component';

@Component({
    selector: 'galactic-request-sample',
    template: `
        <div class="clr-row" *ngIf="connected">
            <div class="clr-col-2">
                <button [clrLoading]="requestLoading" class="btn btn-primary-outline btn-sm"
                        (click)="makeRequest()">Ask For A Joke
                </button>
            </div>
            <div class="clr-col-10" *ngIf="item">
                <blockquote>{{item}}</blockquote>
            </div>
        </div>
        <strong *ngIf="!connected">Not connected to fabric, connect to run this code</strong>`
})
export class GalacticRequestComponent extends BaseDocsComponent implements OnInit, OnDestroy {

    @Input() usePrivateChannel: boolean = false;
    public item: string;
    requestLoading: ClrLoadingState = ClrLoadingState.DEFAULT;

    constructor(private cd: ChangeDetectorRef) {
        super('GalacticRequestComponent');
    }

    ngOnInit(): void {
        super.ngOnInit();
        // TODO: discussion point
        // There is a race when you switch from "Private Channel" to "Talking via the Fabric" or vice versa that
        // causes an infinite spinner when you switch from one of those pages to the other.
        // This happens because the same channel name is used between the two pages where this component's ngOnInit
        // is invoked to start listening to the channel. When markChannelAsGalatic() is called, deep down the stack
        // StompSession's subscribe() is called with an ID that consists of the session ID and the service channel
        // name (note #1). Between the transition between the public vs private channels, the order in which unsubscribe and
        // subscribe methods are fired on the same ID causes the StompSession map to overwrite an existing subscription.

        // I think this might not be a huge problem in real world scenarios under an assumption on how a channel name
        // could be differently set based on the semantics that private vs. public implies (e.g. servbot-broadcast,
        // servbot-private). However, it is an indeed reproducible issue and should be noted.

        // notes:
        // 1. https://gitlab.eng.vmware.com/bifrost/typescript/blob/master/src/bridge/stomp.model.ts#L114
        // 2. wrapping markChannelAsGalactic() in setTimeout() fixes the problem by pushing the method execution by a
        //    tick, but it is a bad workaround and in real world it is better to use a different name for the channel
        this.bus.api.tickEventLoop(() => {
            this.bus.markChannelAsGalactic('servbot', this.usePrivateChannel);
        });

        // make sure our component picks up connection state on boot.
        this.connectedStateStream.subscribe(
            () => {
                this.cd.detectChanges();
            }
        );
    }

    makeRequest(): void {

        // show state on the button
        this.requestLoading = ClrLoadingState.LOADING;

        // make galactic joke request!
        this.bus.requestOnce(
            'servbot',
            this.fabric.generateFabricRequest('Joke', null))
            .handle(
                (response: any) => {
                    this.item = response.payload;
                    this.requestLoading = ClrLoadingState.DEFAULT;
                    this.cd.detectChanges();
                }
            );
    }

    ngOnDestroy() {
        this.bus.markChannelAsLocal('servbot');
        super.ngOnDestroy();
    }
}
