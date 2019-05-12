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
        <button (click)="requestAPI()" class="btn btn-primary" [clrLoading]="iconState">Request API via Fabric Rest
            Service
        </button><br/>
        Response: {{response}}`
})
export class FabricRestServiceComponent extends AbstractBase implements OnInit, OnDestroy {

    public response = 'nothing yet, request something!';
    public iconState: ClrLoadingState = ClrLoadingState.DEFAULT;
    private restService: RestService;

    constructor(private cd: ChangeDetectorRef) {
        super('FabricRestServiceComponent');
    }


    disableLocalRestService(): void {
        this.restService.offline();
        this.bus.markChannelAsGalactic('fabric-rest');
        // get a reference to
    }

    enableLocalRestService(): void {
        this.bus.markChannelAsLocal('fabric-rest');
        this.restService.online();
    }

    ngOnInit(): void {

        this.restService = ServiceLoader.getService(RestService);
        this.disableLocalRestService();
    }

    ngOnDestroy(): void {
        this.enableLocalRestService();
    }

    /**
     * s
     */
    public requestAPI(): void {
        this.iconState = ClrLoadingState.LOADING;



        this.restServiceRequest(
            {
                apiClass: 'java.lang.String',
                id: GeneralUtil.genUUID(),
                uri: `https://jsonplaceholder.typicode.com/todos/1`,
                method: HttpRequest.Get,
                successHandler: (response: any) => {
                    this.iconState = ClrLoadingState.SUCCESS;
                    this.response = `API Response Success: userID: ${response.userId}, title: ${response.title}`;
                    this.cd.detectChanges();
                },
                errorHandler: (error: RestError) => {
                    this.log.error('bad kitty! ' + error.message);
                    this.iconState = ClrLoadingState.SUCCESS;
                    this.response = `API Response Error: ${error.message}`;
                    this.cd.detectChanges();
                }
            }
        );
    }
}
