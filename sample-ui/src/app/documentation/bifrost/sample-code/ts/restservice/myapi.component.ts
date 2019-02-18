/**
 * Copyright(c) VMware Inc. 2018
 */
import { AbstractBase } from '@vmw/bifrost/core';
import { Component } from '@angular/core';
import { MyAPIServiceChannel, MyAPIServiceRequest, MyAPIServiceResponse } from './myapi.service.model';
import { ClrLoadingState } from '@clr/angular';

@Component({
    selector: 'myapi-component',
    template: `
        <button (click)="requestAPI()" class="btn btn-primary" [clrLoading]="iconState">Request API</button><br/>
        Response: {{response}}`
})
export class MyAPIComponent extends AbstractBase {

    public response = 'nothing yet, request something!';
    public iconState: ClrLoadingState = ClrLoadingState.DEFAULT;

    constructor() {
        super('MyAPIComponent');
    }

    /**
     * Send a request to MyAPI Service
     */
    public requestAPI(): void {
        this.iconState = ClrLoadingState.LOADING;
        // generate an ID between 1 and 10.
        const request: MyAPIServiceRequest = {
            id:  Math.floor(Math.random() * (10 - 1 + 1)) + 1
        };

        this.bus.requestOnce(MyAPIServiceChannel, request)
            .handle(
                (response: MyAPIServiceResponse) => {
                    this.response = response.message;
                    this.iconState = ClrLoadingState.DEFAULT;
                }
            );
    }
}
