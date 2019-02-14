import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { VERSION } from '@appfab/environments/version';
import { StoreStream } from '@vmw/bifrost';
import { FabricConnectionState } from '@vmw/bifrost/fabric.api';
import { BaseBifrostComponent } from '../documentation/bifrost/base.bifrost.component';

@Component({
    selector: 'footer-component',
    templateUrl: './footer.component.html',
    styleUrls: ['./footer.component.scss']
})
export class FooterComponent extends BaseBifrostComponent implements OnInit, OnDestroy {

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
    }

    ngOnDestroy(): void {
        this.connectedStateStream.unsubscribe();
    }

    private listenForConnectionStateChange(): void {
        // when connection state changes, change our view state.
        this.connectedStateStream = this.fabric.whenConnectionStateChanges();
        this.connectedStateStream.subscribe(
            (stateChange: FabricConnectionState) => {
                switch (stateChange) {
                    case FabricConnectionState.Connected:
                        this.connected = true;
                        this.connectionClass = 'label-success';
                        this.connectionState = 'Connected to Fabric';
                        break;

                    case FabricConnectionState.Disconnected:
                        this.connected = false;
                        this.connectionClass = 'label-purple';
                        this.connectionState = 'Disconnected from Fabric';
                        break;
                    case FabricConnectionState.Failed:
                        this.connected = false;
                        this.connectionClass = 'label-danger';
                        this.connectionState = 'Unable to connect to Fabric';
                        break;
                }
                // ensure component re-renders.
                this.cd.detectChanges();
            }
        );
    }
}
