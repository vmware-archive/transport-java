import { BaseComponent } from '../models/abstractions/base.component';
import { OnDestroy, OnInit } from '@angular/core';
import { FabricConnectionState } from '@vmw/bifrost/fabric.api';
import { StoreStream } from '@vmw/bifrost';
import { getDefaultFabricConnectionString } from '../shared/utils';

export abstract class BaseDocsComponent extends BaseComponent implements OnInit, OnDestroy {

    public highlighted = false;
    public connected = false;
    public connectedStateStream: StoreStream<FabricConnectionState>;

    public areBifrostTsDocsActive(): boolean {
        return this.storeManager.getStore<boolean>('docs').get('ts');
        //return true;
    }

    public areBifrostJavaDocsActive(): boolean {
        return this.storeManager.getStore<boolean>('docs').get('java');
        //return true;
    }

    public areSewingMachineDocsActive(): boolean {
        return this.storeManager.getStore<boolean>('docs').get('sm');
        //return true;
    }

    public areVideoDocsActive(): boolean {
        return true;
    }

    public setBifrostTsDocsActive(state: boolean): void {
        this.storeManager.getStore<boolean>('docs').put('ts', state, null);
    }

    public setBifrostJavaDocsActive(state: boolean): void {
        this.storeManager.getStore<boolean>('docs').put('java', state, null);
    }

    public setSewingMachineDocsActive(state: boolean): void {
        this.storeManager.getStore<boolean>('docs').put('sm', state, null);
    }

    public ngOnInit() {
        this.listenForConnectionStateChange();
    }

    public ngOnDestroy() {
        if (this.connectedStateStream) {
            this.connectedStateStream.unsubscribe();
        }
    }

    public listenForConnectionStateChange(): void {

        this.connected = this.fabric.isConnected(getDefaultFabricConnectionString());

        // when connection state changes, change our view state.
        this.connectedStateStream = this.fabric.whenConnectionStateChanges(getDefaultFabricConnectionString());
        this.connectedStateStream.subscribe(
            (stateChange: FabricConnectionState) => {
                switch (stateChange) {
                    case FabricConnectionState.Connected:
                        this.connected = true;
                        break;

                    case FabricConnectionState.Disconnected:
                        this.connected = true;
                        break;
                    case FabricConnectionState.Failed:
                        this.connected = false;
                        break;
                }
            }
        );
    }

}
