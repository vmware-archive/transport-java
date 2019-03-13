import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { AbstractBase } from '@vmw/bifrost/core';
import { BusStore } from '@vmw/bifrost';
import { AppStores, FabricVersionState } from '../../../constants';

@Component({
    selector: 'appfab-java-badges',
    templateUrl: './java-badges.component.html',
    styleUrls: ['./java-badges.component.scss']
})
export class JavaBadgesComponent extends AbstractBase implements OnInit, OnDestroy {

    public version: string;
    public versionStore: BusStore<string>;

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
            this.versionStore.onChange('java', FabricVersionState.JavaSet).subscribe(
                (version: string) => {
                    this.version = version;
                    this.cd.detectChanges();
                }
            );
        }
    }

    ngOnDestroy(): void {
        this.cd.detach();
    }
}
