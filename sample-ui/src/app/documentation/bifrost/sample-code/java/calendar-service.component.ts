import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { ClrLoadingState } from '@clr/angular';
import { BaseBifrostComponent } from '../../base.bifrost.component';
import { APIResponse } from '@vmw/bifrost';
import { GeneralUtil } from '@vmw/bifrost/util/util';
import { FabricConnectionState } from '@vmw/bifrost/fabric.api';

@Component({
    selector: 'calendar-service-component',
    template: `
        <div class="clr-row" *ngIf="connected">
            <div class="clr-col-6">
                <button [clrLoading]="requestLoading" class="btn btn-primary-outline btn-sm" (click)="requestTime()">
                    Request Time
                </button>
                <button [clrLoading]="requestLoading" class="btn btn-primary-outline btn-sm" (click)="requestDate()">
                    Request Date
                </button>
                <button [clrLoading]="requestLoading" class="btn btn-primary-outline btn-sm" (click)="unknownCommand()">
                    Send Invalid Command
                </button>
            </div>
            <div class="clr-col-6" *ngIf="item">
                Fabric Calendar Service Response: <span class="emphasis">{{item}}</span>
            </div>
        </div>
        <strong *ngIf="!connected">Not connected to fabric, connect to run this code</strong>`
})
export class CalendarServiceComponent extends BaseBifrostComponent implements OnInit, OnDestroy {

    public item: string;
    requestLoading: ClrLoadingState = ClrLoadingState.DEFAULT;

    constructor(private cd: ChangeDetectorRef) {
        super('CalendarServiceComponent');
    }

    ngOnDestroy() {
        // stop channel from being extended to fabric.
        this.bus.markChannelAsLocal('calendar-service');
    }

    ngOnInit(): void {

        this.connected = this.fabric.isConnected();

        // extend channel to fabric.
        this.bus.markChannelAsGalactic('calendar-service');

        // make sure our component picks up connection state on boot.
        this.fabric.whenConnectionStateChanges().subscribe(
            (state: FabricConnectionState) => {
                if (state === FabricConnectionState.Connected) {
                    this.connected = true;
                }
                this.cd.detectChanges();
            }
        );
    }

    private makeRequest(command: string) {
        // show state on the button
        this.requestLoading = ClrLoadingState.LOADING;
        const request = this.fabric.generateFabricRequest(command);

        this.bus.requestOnceWithId(GeneralUtil.genUUIDShort(), 'calendar-service', request)
            .handle((response: APIResponse<string>) => {
                this.item = response.payload;
                this.requestLoading = ClrLoadingState.DEFAULT;
                this.cd.detectChanges();
            });
    }

    requestTime(): void {
        this.makeRequest('time');
    }

    requestDate(): void {
        this.makeRequest('date');
    }

    unknownCommand(): void {
        this.makeRequest('invalid');
    }


}
