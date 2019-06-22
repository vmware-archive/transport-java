import { AfterViewChecked, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../base.docs.component';
import { HighlightService } from '../../local-services/highlight.service';
import * as packageJSON from '@vmw/bifrost/package.json';
import { BusStore, StoreStream } from '@vmw/bifrost';
import { AppStores, FabricVersionState } from '../../../constants';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent extends BaseDocsComponent implements OnInit, OnDestroy, AfterViewChecked {

    packageJSON: any = packageJSON;
    public sewingMachineVersion;
    public javaVersion: string;
    private versionStore: BusStore<string>;
    private versionStoreStream: StoreStream<string>;


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
        this.setBifrostTsDocsActive(true);
        this.setBifrostJavaDocsActive(true);
        this.javaVersion = 'fetching...';
        this.sewingMachineVersion = 'fetching...';

        this.disableLocalRestService();

        const storeVersion = this.versionStore.get('java');
        if (storeVersion) {
            this.javaVersion = storeVersion;
        } else {
            this.versionStoreStream = this.versionStore.onChange('java', FabricVersionState.JavaSet);
            this.versionStoreStream.subscribe(
                (version: string) => {
                    this.javaVersion = version;
                    this.cd.detectChanges();
                }
            );
        }

    }

    ngOnDestroy(): void {
        if (this.versionStoreStream) {
            this.versionStoreStream.unsubscribe();
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
