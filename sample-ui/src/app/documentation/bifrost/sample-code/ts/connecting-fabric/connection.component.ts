/**
 * Copyright(c) VMware Inc. 2019
 */
import {
    ChangeDetectorRef,
    Component,
    OnDestroy,
    OnInit
} from '@angular/core';
import { AbstractBase } from '@vmw/bifrost/core';
import { FabricConnectionState } from '@vmw/bifrost/fabric.api';
import { MessageHandler, StoreStream } from '@vmw/bifrost';

export interface SimpleStreamObject {
    payload: string;
}

@Component({
    selector: 'fabric-connect-sample',
    template: `<button class='btn btn-primary btn-sm' (click)='connect()' *ngIf="!connected">Connect To Fabric</button>
        <button class='btn btn-danger btn-sm' (click)='disconnect()' *ngIf="connected">Disconnect From Fabric</button>
        <span class="label label-danger" *ngIf="error">{{error}}</span>
        <span class="label label-purple" *ngIf="connected">{{item}}</span>`
})
export class FabricConnectionComponent extends AbstractBase implements OnInit, OnDestroy {

    public connected = false;
    public error = null;
    public item: string;
    public connectedStateStream: StoreStream<FabricConnectionState>;
    public simpleStream: MessageHandler<SimpleStreamObject>;

    constructor(private cd: ChangeDetectorRef) {
        super('FabricConnectionComponent');
    }

    ngOnInit(): void {

        this.item = 'waiting for stream event...';

        // mark this channel as galactic, extend the stream to the fabric.
        this.bus.markChannelAsGalactic('simple-stream');

        // set initial state.
        this.connected = this.fabric.isConnected();

        // listen for connection state changes.
        this.listenForConnectionStateChange();

        this.listenToSimpleStream();
    }

    ngOnDestroy(): void {
        this.connectedStateStream.unsubscribe();
        this.simpleStream.close();

        // mark this channel as local, this will stop streams.
        this.bus.markChannelAsLocal('simple-stream');
    }

    private listenToSimpleStream() {
        // listen to channel on bus.
        this.simpleStream = this.bus.listenStream('simple-stream');
        this.simpleStream.handle(
            (response: SimpleStreamObject) => {
                this.item = `Stream: ${response.payload}`;
                try {
                    this.cd.detectChanges();
                } catch (e) {
                    // ignore.
                }
                this.cd.detectChanges();
            }
        );
    }

    connect(): void {

        this.listenToSimpleStream();

        // function called when connected to fabric
        const connectedHandler = (sessionId: string) => {
            this.log.info(`Connected to Application Fabric with sessionId ${sessionId}`, this.getName());
            this.connected = true;
        };

        // function called when disconnected.
        const disconnectedHandler = () => {
           this.log.info('Disconnected from Application Fabric.', this.getName());
           this.connected = false;
           this.simpleStream.close();
        };

        // connect to the fabric.
        this.bus.fabric.connect(connectedHandler, disconnectedHandler);
    }

    disconnect(): void {
        // disconnect.
        this.bus.fabric.disconnect();
        this.simpleStream.close();
    }

    private listenForConnectionStateChange(): void {
        // when connection state changes, change our view state.
        this.connectedStateStream = this.fabric.whenConnectionStateChanges();
        this.connectedStateStream.subscribe(
            (stateChange: FabricConnectionState) => {
                switch (stateChange) {
                    case FabricConnectionState.Connected:
                        this.connected = true;
                        this.error = null;
                        break;

                    case FabricConnectionState.Disconnected:
                        this.connected = false;
                        this.error = null;
                        break;
                    case FabricConnectionState.Failed:
                        this.connected = false;
                        this.error = 'unable to connect';
                        break;
                }
            }
        );
    }
}
