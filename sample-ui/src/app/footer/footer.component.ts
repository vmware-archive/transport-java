import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { VERSION } from '@appfab/environments/version';
import { StoreStream } from '@vmw/bifrost';
import { FabricConnectionState } from '@vmw/bifrost/fabric.api';
import { BaseDocsComponent } from '../documentation/base.docs.component';
import { getDefaultFabricConnectionString } from '../shared/utils';

@Component({
    selector: 'footer-component',
    templateUrl: './footer.component.html',
    styleUrls: ['./footer.component.scss']
})
export class FooterComponent extends BaseDocsComponent implements OnInit, OnDestroy {

    date = new Date().getFullYear();
    version = VERSION;
    buildTime = Date.parse(VERSION.time);
    connectionState = 'Disconnected from Fabric';
    connectionClass = 'label-purple';

    public connected = false;
    public connectedStateStream: StoreStream<FabricConnectionState>;

    constructor(private cd: ChangeDetectorRef) {
        super('HeaderComponent');
    }

    ngOnInit() {
        this.listenForConnectionStateChange();
        this.connected = this.fabric.isConnected(getDefaultFabricConnectionString());
        if (this.connected) {
            this.setConnected();
        }
    }

    ngOnDestroy(): void {
        if (this.connectedStateStream) {
            this.connectedStateStream.unsubscribe();
        }
    }

    private setConnected(): void {
        this.connected = true;
        this.connectionClass = 'label-success';
        this.connectionState = 'Connected to Fabric';
    }

    private setDisconnected(): void {
        this.connected = false;
        this.connectionClass = 'label-purple';
        this.connectionState = 'Disconnected from Fabric';
    }

    private setConnectError(): void {
        this.connected = false;
        this.connectionClass = 'label-danger';
        this.connectionState = 'Unable to connect to Fabric';
    }

    listenForConnectionStateChange(): void {
        // when connection state changes, change our view state.
        this.connectedStateStream = this.fabric.whenConnectionStateChanges(getDefaultFabricConnectionString());
        this.connectedStateStream.subscribe(
            (stateChange: FabricConnectionState) => {
                switch (stateChange) {
                    case FabricConnectionState.Connected:
                        this.setConnected();
                        break;

                    case FabricConnectionState.Disconnected:
                        this.setDisconnected();
                        break;
                    case FabricConnectionState.Failed:
                        this.setConnectError();
                        break;
                }
                // ensure component re-renders.
                this.cd.detectChanges();
            }
        );
    }
}
