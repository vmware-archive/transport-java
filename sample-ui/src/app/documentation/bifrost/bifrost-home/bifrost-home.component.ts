import { AfterViewInit, ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';

import * as packageJSON from '@vmw/bifrost/package.json';
import { AbstractBase } from '@vmw/bifrost/core';
import { Subscription } from 'rxjs';

interface ArtifactoryDownloadStatsInfo {
    totalDownloads: number
    keywords: string[]
}

const BIFROST_METRICS_SERVICE_CHANNEL = 'bifrost-metrics-service';
const GET_TS_LIB_DOWNLOADS_COUNT = 'ts-lib-download-count';

@Component({
    selector: 'appfab-bifrost-home',
    templateUrl: './bifrost-home.component.html',
    styleUrls: ['./bifrost-home.component.scss'],
})
export class BifrostHomeComponent extends AbstractBase implements OnInit, AfterViewInit, OnDestroy {
    packageJSON: any = packageJSON;
    changeLog: any;
    bifrostTSDownloadsCount: number;

    private bifrostStatsSubscription: Subscription;

    constructor(private cdr: ChangeDetectorRef) {
        super('BifrostHomeComponent');
    }

    ngOnInit() {
        this.changeLog = packageJSON.changelogHistory;
        this.bus.markChannelAsGalactic(BIFROST_METRICS_SERVICE_CHANNEL);
    }

    ngAfterViewInit(): void {
        // intentional delay before a request could be received by the backend so it has enough time to process
        // subscription events
        this.bus.api.tickEventLoop(() => {
            this.bifrostStatsSubscription = this.bus.requestOnce(BIFROST_METRICS_SERVICE_CHANNEL,
                this.fabric.generateFabricRequest(GET_TS_LIB_DOWNLOADS_COUNT, null))
                .handle((response: ArtifactoryDownloadStatsInfo) => {
                    this.bifrostTSDownloadsCount = response.totalDownloads;
                    this.cdr.detectChanges();
                }, (error) => {
                    this.log.error(error);
                });
        }, 1000);
    }

    ngOnDestroy(): void {
        this.bus.markChannelAsLocal(BIFROST_METRICS_SERVICE_CHANNEL);
        if (this.bifrostStatsSubscription) {
            this.bifrostStatsSubscription.unsubscribe();
        }
    }
}
