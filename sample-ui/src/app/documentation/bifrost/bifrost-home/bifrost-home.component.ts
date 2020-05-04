import { AfterViewInit, ChangeDetectorRef, Component, OnDestroy, OnInit, ViewChild } from '@angular/core';

import { AbstractBase } from '@vmw/bifrost/core';
import { Subscription } from 'rxjs';
import { ChangelogComponent } from '../../../shared/changelog/changelog.component';
import { BusStore, StoreStream } from '@vmw/bifrost';
import {
    AppStores,
    BIFROST_METADATA_SERVICE_CHANNEL,
    BIFROST_METRICS_SERVICE_CHANNEL,
    FabricVersionState, GET_TS_LIB_DOWNLOADS_COUNT, GET_TS_LIB_METADATA
} from '../../../../constants';

interface ArtifactoryDownloadStatsInfo {
    totalDownloads: number
    keywords: string[]
}

@Component({
    selector: 'appfab-bifrost-home',
    templateUrl: './bifrost-home.component.html',
    styleUrls: ['./bifrost-home.component.scss'],
})
export class BifrostHomeComponent extends AbstractBase implements AfterViewInit, OnDestroy {
    @ViewChild(ChangelogComponent, null) changeLogComponent: ChangelogComponent;

    public changeLog: any[];
    public bifrostTSDownloadsCount: number;

    private bifrostStatsSubscription: Subscription;
    private bifrostTSMetadataSubscription: Subscription;
    private versionStore: BusStore<string>;

    constructor(private cdr: ChangeDetectorRef) {
        super('BifrostHomeComponent');
        this.versionStore = this.storeManager.createStore<string>(AppStores.Versions);
        this.changeLog = [];
    }

    ngAfterViewInit(): void {
        this.bifrostStatsSubscription = this.bus.requestOnce(BIFROST_METRICS_SERVICE_CHANNEL,
            this.fabric.generateFabricRequest(GET_TS_LIB_DOWNLOADS_COUNT, null))
            .handle((response: ArtifactoryDownloadStatsInfo) => {
                this.bifrostTSDownloadsCount = response.totalDownloads;
                this.cdr.detectChanges();
            }, (error) => {
                this.log.error(error);
            });

        this.bifrostTSMetadataSubscription = this.bus.requestOnce(BIFROST_METADATA_SERVICE_CHANNEL,
            this.fabric.generateFabricRequest(GET_TS_LIB_METADATA, null))
            .handle((response: any) => {
                if (response && response.changelogHistory && response.changelogHistory instanceof Array) {
                    // update version store
                    this.versionStore.put('ui', response.changelogHistory[0].version, FabricVersionState.UiSet);

                    // supply the change logs array to the data grid
                    this.changeLog = response.changelogHistory;
                    this.changeLogComponent.loading = false;
                    this.cdr.detectChanges();
                }
            }, (error) => {
                this.log.error(error);
            });
    }

    ngOnDestroy(): void {
        if (this.bifrostStatsSubscription) {
            this.bifrostStatsSubscription.unsubscribe();
        }

        if (this.bifrostTSMetadataSubscription) {
            this.bifrostTSMetadataSubscription.unsubscribe();
        }
    }
}
