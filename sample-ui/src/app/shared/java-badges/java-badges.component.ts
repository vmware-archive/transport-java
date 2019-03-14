import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { AbstractBase } from '@vmw/bifrost/core';
import { BusStore, StoreStream } from '@vmw/bifrost';
import { AppStores, FabricVersionState } from '../../../constants';

@Component({
    selector: 'appfab-java-badges',
    templateUrl: './java-badges.component.html',
    styleUrls: ['./java-badges.component.scss']
})
export class JavaBadgesComponent extends AbstractBase implements OnInit, OnDestroy {

    public version: string;
    private versionStore: BusStore<string>;
    private versionStoreStream: StoreStream<string>;

    constructor(private cd: ChangeDetectorRef) {
        super('JavaBadgesComponent');
        this.versionStore = this.storeManager.getStore<string>(AppStores.Versions);
    }

    ngOnInit() {
        this.version = 'fetching...';
        const storeVersion = this.versionStore.get('java');
        if (storeVersion) {
            this.version = storeVersion;
        } else {
            this.versionStoreStream = this.versionStore.onChange('java', FabricVersionState.JavaSet);
            this.versionStoreStream.subscribe(
                (version: string) => {
                    this.version = version;
                    this.cd.detectChanges();
                }
            );
        }
    }

    ngOnDestroy(): void {
        if (this.versionStoreStream) {
            this.versionStoreStream.unsubscribe();
        }
    }
}
