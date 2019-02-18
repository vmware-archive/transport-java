/**
 * Copyright(c) VMware Inc. 2019
 */
import { AbstractService } from '@vmw/bifrost/core';
import { EventBus, MessageArgs } from '@vmw/bifrost';
import { PongRequestType, PongServiceChannel, PongServiceRequest, PongServiceResponse } from './pong.service.model';

export class PongService extends AbstractService<PongServiceRequest, PongServiceResponse> {

    constructor() {

        // identify the name of this service, and the channel it operates on.
        super('PongService', PongServiceChannel);

        // log that the service is online (if logging enabled)
        this.log.info('PongService Loaded');
    }

    /**
     * Handle a request send on the service channel.
     * @param request the request object
     * @param args automatically passed by superclass, important details within.
     */
    protected handleServiceRequest(request: PongServiceRequest, args?: MessageArgs): void {

        switch (request.command) {
            case PongRequestType.Basic:
                this.handleBasicRequest(request.message, args);
                break;

            case PongRequestType.Full:
                this.handleFullRequest(request.message, args);
        }
    }

    /**]
     * Handle a basic request (limited details)
     * @param message this is the message sent by the requesting actor.
     * @param args these are important values that identify who sent the request.
     */
    private handleBasicRequest(message: string, args: MessageArgs): void {

        const basicResponse = {
            value: `pong '${message}'`
        };

        // send a response to whomever requested it.
        this.postResponse(PongServiceChannel, basicResponse, args);
    }

    /**
     * Handle a more detailed response (more details)
     * @param message this is the message sent by the requesting actor.
     * * @param args these are important values that identify who sent the request.
     */
    private handleFullRequest(message: string, args: MessageArgs): void {

        // get a timestamp.
        const date = new Date();
        const dateString = date.toLocaleDateString('en-US');
        const perf = performance.now();

        const basicResponse = {
            value: `pong '${message}' (on ${dateString} by event bus with id ${EventBus.id}) time: ${perf}`
        };

        // send a response to whomever requested it.
        this.postResponse(PongServiceChannel, basicResponse, args);

    }
}
