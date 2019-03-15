/**
 * Copyright(c) VMware Inc. 2019
 */
import { AbstractBase } from '@vmw/bifrost/core';
import { Component } from '@angular/core';
import { PongRequestType, PongServiceChannel, PongServiceRequest, PongServiceResponse } from './pong.service.model';
import { APIRequest, BusTransaction, TransactionType } from '@vmw/bifrost';
import { GeneralUtil } from '@vmw/bifrost/util/util';
import { GeneralError } from '@vmw/bifrost/core/model/error.model';

@Component({
    selector: 'ping-transaction-component',
    template: `<button (click)="sendPingTransaction()" class="btn btn-primary">Ping (Transaction)</button><br/>
        <ul>
            <li *ngFor="let response of responses">{{response.payload}}</li>
        </ul>`
})
export class PingTransactionComponent extends AbstractBase {

    public responses = [{payload: 'nothing yet, request something!'}];

    constructor() {
        super('PingTransactionComponent');
    }

    /**
     * Send a three PongService requests as a part of a transaction.
     */
    public sendPingTransaction(): void {

        const createRequest = (type: PongRequestType, payload: string) => {
            return this.fabric.generateFabricRequest(type, payload);
        };

        // send the same request, three times.
        this.pingTransaction([
            createRequest(PongRequestType.Full, 'request 1'),
            createRequest(PongRequestType.Full, 'request 2'),
            createRequest(PongRequestType.Full, 'request 3')
        ]);
    }

    private pingTransaction(requests: APIRequest<string>[]): void {

        // create a transaction
        const transaction: BusTransaction = this.bus.createTransaction(TransactionType.ASYNC, GeneralUtil.genUUIDShort());

        // for each request submitted, queue up a request in transaction
        for (const request of requests) {

            // add request to transaction
            transaction.sendRequest(PongServiceChannel, request);
        }

        // register a transaction completion handler
        transaction.onComplete<PongServiceResponse>(
            (pongResponses: PongServiceResponse[]) => {

                this.responses = [];

                // push responses
                pongResponses.forEach(
                    (response: PongServiceResponse) => {
                        this.responses.push(response);
                    }
                );
            }
        );

        // register a transaction error handler
        transaction.onError<GeneralError>(
            (error: GeneralError) => {
                // do something with this error
            }
        );

        // run the transaction!
        transaction.commit();
    }

}
