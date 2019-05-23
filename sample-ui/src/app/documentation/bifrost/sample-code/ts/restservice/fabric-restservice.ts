/**
 * Copyright(c) VMware Inc. 2019
 */
import { AbstractBase, HttpRequest, RestError } from '@vmw/bifrost/core';
import { ChangeDetectorRef, Component, OnDestroy, OnInit } from '@angular/core';
import { MyAPIServiceChannel, MyAPIServiceRequest, MyAPIServiceResponse } from './myapi.service.model';
import { ClrLoadingState } from '@clr/angular';
import { RestService } from '@vmw/bifrost/core/services/rest/rest.service';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { GeneralUtil } from '@vmw/bifrost/util/util';

@Component({
    selector: 'fabric-restservice-component',
    template: `
        <button (click)="requestAPI()" class="btn btn-primary" [clrLoading]="iconState">Request API via Fabric Rest Service</button>
        <button (click)="requestInvalidAPI()" class="btn btn-primary" [clrLoading]="iconState">Error 404 via Fabric Rest Service</button><br/>
        Response: {{response}}`
})
export class FabricRestServiceComponent extends AbstractBase {

    public response = 'nothing yet, request something!';
    public iconState: ClrLoadingState = ClrLoadingState.DEFAULT;
    private uri: string;

    constructor(private cd: ChangeDetectorRef) {
        super('FabricRestServiceComponent');
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

    private makeApiRequest() {
        this.iconState = ClrLoadingState.LOADING;

        // disable local Rest Service
        this.disableLocalRestService();
        this.response = "requesting from API, via Fabric (not local)....";

        this.restServiceRequest(
            {
                uri: this.uri,
                method: HttpRequest.Get,
                successHandler: (response: any) => {
                    this.iconState = ClrLoadingState.SUCCESS;
                    this.response = `API Call Success: ${response.title}`;
                    this.cd.detectChanges();
                    this.enableLocalRestService();
                },
                errorHandler: (error: RestError) => {
                    this.iconState = ClrLoadingState.ERROR;
                    this.response = `API Response Error: ${error.message}`;
                    this.cd.detectChanges();
                    this.enableLocalRestService();
                }
            }
        );
    }

    public requestAPI(): void {
        this.uri = `https://jsonplaceholder.typicode.com/todos/${Math.floor(Math.random() * (10 - 1 + 1)) + 1}`;
        this.makeApiRequest();
    }

    public requestInvalidAPI(): void {
        this.uri = `https://jsonplaceholder.typicode.com/I-DO-NOT-EXIST`;
        this.makeApiRequest();
    }
}
