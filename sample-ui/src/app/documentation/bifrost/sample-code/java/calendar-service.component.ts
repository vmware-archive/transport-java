import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { APIRequest, APIResponse } from '@vmw/bifrost';
import { ClrLoadingState } from '@clr/angular';
import { GeneralUtil } from '@vmw/bifrost/util/util';
import { BaseBifrostComponent } from '../../base.bifrost.component';

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

    ngOnInit(): void {
        super.ngOnInit();

        // make sure our component picks up connection state on boot.
        this.connectedStateStream.subscribe(
            () => {
                this.cd.detectChanges();
            }
        );
    }

    private makeRequest(command: string) {
        // show state on the button
        this.requestLoading = ClrLoadingState.LOADING;

        // make galactic joke request!
        this.bus.requestGalactic(
            'calendar-service',
            new APIRequest(command, null, GeneralUtil.genUUID(), 1),
            (response: APIResponse<any>) => {
                this.item = response.payload;
                this.requestLoading = ClrLoadingState.DEFAULT;
                this.cd.detectChanges();
            }
        );
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

    ngOnDestroy() {
        super.ngOnDestroy();
    }
}
