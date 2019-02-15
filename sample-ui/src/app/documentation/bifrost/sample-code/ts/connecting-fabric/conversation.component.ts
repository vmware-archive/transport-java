/**
 * Copyright(c) VMware Inc. 2019
 */
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { AbstractBase } from '@vmw/bifrost/core';
import { APIRequest, APIResponse, BusStore, StoreStream } from '@vmw/bifrost';
import { GeneralUtil } from '@vmw/bifrost/util/util';
import { ClrLoadingState } from '@clr/angular';

@Component({
    selector: 'galactic-request-sample',
    template: `
        <strong>Make a Galactic Joke Request</strong><br/>
        <div *ngIf="connected">
            <button [clrLoading]="requestLoading" class="btn btn-success btn-sm" (click)="makeRequest()">Ask For A Joke</button>
            <h5 *ngIf="connected">
                {{item}}
            </h5>
        </div>
        <div *ngIf="!connected">Connect to broker first, see previous demo!</div>`
})
export class GalacticRequestComponent extends AbstractBase implements OnInit, OnDestroy {

    public connected = false;
    public item: string;
    private store: BusStore<boolean>;
    private storeChangeListener: StoreStream<boolean, any>;

    requestLoading: ClrLoadingState = ClrLoadingState.DEFAULT;

    constructor(private cd: ChangeDetectorRef) {
        super('NoteComponent');
    }

    ngOnInit(): void {
        this.connected = this.fabric.isConnected();
    }

    makeRequest(): void {

        // show state on the button
        this.requestLoading = ClrLoadingState.LOADING;

        // make galactic joke request!
        this.bus.requestGalactic(
            'servbot',
            new APIRequest('Joke', null, GeneralUtil.genUUID(), 1),
            (response: APIResponse<any>) => {
                this.item = response.payload;
                this.requestLoading = ClrLoadingState.DEFAULT;
                this.cd.detectChanges();
            }
        );
    }

    ngOnDestroy() {
        // this.storeChangeListener.unsubscribe();
    }
}
