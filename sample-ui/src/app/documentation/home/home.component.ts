import { AfterViewChecked, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../base.docs.component';
import { HighlightService } from '../../local-services/highlight.service';
import { APIResponse, BusStore, StoreStream } from '@vmw/bifrost';
import {
    AppStores,
    BIFROST_METADATA_SERVICE_CHANNEL,
    FabricVersionState, GET_TS_LIB_LATEST_VERSION
} from '../../../constants';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent extends BaseDocsComponent implements OnInit, OnDestroy, AfterViewChecked {

    public sewingMachineVersion;
    public uiVersion: string;
    public javaVersion: string;
    private versionStore: BusStore<string>;
    private javaVersionStream: StoreStream<string>;
    private uiVersionStream: StoreStream<string>;


    constructor(private highlightService: HighlightService, private cd: ChangeDetectorRef) {
        super('DocsHomeComponent');
        this.versionStore = this.storeManager.getStore<string>(AppStores.Versions);
    }

    disableLocalRestService(): void {
        // disable local Rest Service
        ServiceLoader.offlineLocalRestService();
        this.fabric.useFabricRestService();
    }

    enableLocalRestService(): void {
        // disable local Rest Service
        ServiceLoader.onlineLocalRestService();
        this.fabric.useLocalRestService();
    }


    ngOnInit() {
        this.setBifrostTsDocsActive(false);
        this.setBifrostJavaDocsActive(false);
        this.uiVersion = 'fetching...';
        this.javaVersion = 'fetching...';
        this.sewingMachineVersion = 'fetching...';

        this.disableLocalRestService();

        if (this.versionStore.get('java')) {
            this.javaVersion = this.versionStore.get('java');
        } else {
            this.javaVersionStream = this.versionStore.onChange('java', FabricVersionState.JavaSet);
            this.javaVersionStream.subscribe(
                (version: string) => {
                    this.javaVersion = version;
                    this.cd.detectChanges();
                }
            );
        }

        if (this.versionStore.get('ui')) {
            this.uiVersion = this.versionStore.get('ui');
        } else {
            this.bus.requestOnce(BIFROST_METADATA_SERVICE_CHANNEL,
                this.fabric.generateFabricRequest(GET_TS_LIB_LATEST_VERSION, null))
                .handle((version: APIResponse<string>) => {
                    this.uiVersion = version.payload;
                    this.cd.detectChanges();
                });
        }
    }

    ngOnDestroy(): void {
        if (this.javaVersionStream) {
            this.javaVersionStream.unsubscribe();
        }

        if (this.uiVersionStream) {
            this.uiVersionStream.unsubscribe();
        }
        this.enableLocalRestService();
    }


    ngAfterViewChecked() {
        if (!this.highlighted) {
            this.highlightService.highlightAll();
            this.highlighted = true;
        }
    }

}
