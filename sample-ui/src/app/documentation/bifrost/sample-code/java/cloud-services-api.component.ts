import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ClrLoadingState } from '@clr/angular';
import { BaseDocsComponent } from '../../../base.docs.component';
import { APIResponse } from '@vmw/bifrost';
import { GeneralUtil } from '@vmw/bifrost/util/util';
import { FabricConnectionState } from '@vmw/bifrost/fabric.api';
import { CloudServicesStatusResponse } from './cloud-services.models';
import { getDefaultFabricConnectionString } from '../../../../shared/utils';

@Component({
    selector: 'cloudapi-service-component',
    template: `
        <div *ngIf="connected">
            <section>
                <button [clrLoading]="requestLoading" class="btn btn-primary-outline btn-sm" (click)="makeRequest()">
                    Request Cloud Services Status
                </button>
            </section>
            <div *ngIf="response">
                <table class="table left table-verticaltable-compact">
                    <tbody>
                    <tr>
                        <th>Status ID</th>
                        <td>{{response.page.id}}</td>
                    </tr>
                    <tr>
                        <th>Name</th>
                        <td>{{response.page.name}}</td>
                    </tr>
                    <tr>
                        <th>Timezone</th>
                        <td>{{response.page.time_zone}}</td>
                    </tr>
                    <tr>
                        <th>Last Updated</th>
                        <td>{{response.page.updated_at}}</td>
                    </tr>
                    <tr>
                        <th>Fetched From</th>
                        <td>{{response.page.url}}</td>
                    </tr>
                    <tr>
                        <th>Indicator</th>
                        <td>{{response.status.indicator}}</td>
                    </tr>
                    <tr>
                        <th>Service Status</th>
                        <td>{{response.status.description}}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>
        <strong *ngIf="!connected">Not connected to fabric, connect to run this code</strong>`
})
export class CloudServicesApiComponent extends BaseDocsComponent implements OnInit, OnDestroy {

    public response: CloudServicesStatusResponse;
    requestLoading: ClrLoadingState = ClrLoadingState.DEFAULT;

    constructor(private cd: ChangeDetectorRef) {
        super('CloudServicesApiComponent');
    }

    ngOnDestroy() {
        // stop channel from being extended to fabric.
        this.bus.markChannelAsLocal('services-CloudServiceStatus');
    }

    ngOnInit(): void {

        this.connected = this.fabric.isConnected(getDefaultFabricConnectionString());

        // extend channel to fabric.
        this.bus.markChannelAsGalactic('services-CloudServiceStatus');

        // make sure our component picks up connection state on boot.
        this.fabric.whenConnectionStateChanges(getDefaultFabricConnectionString()).subscribe(
            (state: FabricConnectionState) => {
                if (state === FabricConnectionState.Connected) {
                    this.connected = true;
                }
                this.cd.detectChanges();
            }
        );
    }

    private makeRequest() {
        // show state on the button
        this.requestLoading = ClrLoadingState.LOADING;
        this.cd.detectChanges();

        const request = this.fabric.generateFabricRequest('');

        // make request.
        this.bus.requestOnceWithId(request.id, 'services-CloudServiceStatus', request)
            .handle((response: APIResponse<CloudServicesStatusResponse>) => {
                this.response = response.payload;
                this.requestLoading = ClrLoadingState.DEFAULT;
                this.cd.detectChanges();
            });
    }
}
