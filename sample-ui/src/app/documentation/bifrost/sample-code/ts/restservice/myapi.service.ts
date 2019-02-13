/**
 * Copyright(c) VMware Inc. 2019
 */
import { AbstractService } from '@vmw/bifrost/core';
import { MessageArgs } from '@vmw/bifrost';
import { MyAPIServiceChannel, MyAPIServiceRequest, MyAPIServiceResponse } from './myapi.service.model';
import { HttpRequest, RestError } from '@vmw/bifrost/core/services/rest/rest.model';

export class MyAPIService extends AbstractService<MyAPIServiceRequest, MyAPIServiceResponse> {

    constructor() {

        // identify the name of this service, and the channel it operates on.
        super('MyAPIService', MyAPIServiceChannel);

        // log that the service is online (if logging enabled)
        this.log.info('MyAPIService Loaded');
    }

    /**
     * Handle a request send on the service channel.
     * @param request the request object
     * @param args automatically passed by superclass, important details within.
     */
    protected handleServiceRequest(request: MyAPIServiceRequest , args?: MessageArgs): void {

        // call an API out there on the internets.
        this.restServiceRequest(
            {
                uri: `https://jsonplaceholder.typicode.com/todos/${request.id}`,
                method: HttpRequest.Get,
                successHandler: (response: any) => {
                    this.postResponse(MyAPIServiceChannel, { message: response.title }, args);
                },
                errorHandler: (error: RestError) => {
                    this.postError(MyAPIServiceChannel, error, args);
                }
            }
        );
    }
}
