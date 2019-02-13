import { Component, OnInit } from '@angular/core';
import { AbstractBase } from '@vmw/bifrost/core';

@Component({
    selector: 'ts-hello-world',
    template: `{{message}}`
})
export class HelloWorldComponent extends AbstractBase implements OnInit {

    public message: string;

    constructor() {
        super('HelloWorldComponent');
    }

    ngOnInit() {

        // define a channel to talk on.
        const someChannel = 'some-channel';

        // listen for requests on 'someChannel' and return a response.
        this.bus.respondOnce(someChannel)
            .generate(
                (request: string) => {
                    this.log.info(`Request Received: ${request}, Sending Response...`);
                    return 'world';
                }
            );

        this.log.info('Sending Request');

        // send request 'hello' on channel 'someChannel'.
        this.bus.requestOnce(someChannel, 'hello')
            .handle(
                (response: string) => {
                    this.message = `hello ${response}`;
                }
            );

    }
}
