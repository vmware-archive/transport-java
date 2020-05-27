import { ChangeDetectorRef, Component, OnInit } from '@angular/core';
import { VERSION } from '@appfab/environments/version';
import { BusStore } from '@vmw/bifrost';
import { BaseDocsComponent } from '../documentation/base.docs.component';
import { AppStores, FabricConnectionStoreState } from '../../constants';

@Component({
    selector: 'footer-component',
    templateUrl: './footer.component.html',
    styleUrls: ['./footer.component.scss']
})
export class FooterComponent extends BaseDocsComponent implements OnInit {

    date = new Date().getFullYear();
    version = VERSION;
    buildTime = Date.parse(VERSION.time);
    connectionState = 'Disconnected from Fabric';
    connectionClass = 'label-purple';

    public connected = false;
    public fabricConnectionStore: BusStore<{[key: string]: any}>;

    constructor(private cd: ChangeDetectorRef) {
        super('HeaderComponent');
    }

    ngOnInit() {
        this.fabricConnectionStore = this.storeManager.getStore<{[key: string]: any}>(AppStores.FabricConnection);
        this.fabricConnectionStore.onChange('connectionState', FabricConnectionStoreState.ConnectionStateUpdate)
            .subscribe(() => this.updateConnectionState());
        this.updateConnectionState();
    }

    private updateConnectionState(): void {
        const connectionState = this.fabricConnectionStore.get('connectionState');
        this.connected = connectionState.connected;
        this.connectionClass = connectionState.connectionClass;
        this.connectionState = connectionState.connectionState;
        this.cd.detectChanges();
    }
}
