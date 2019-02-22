import { Component, OnInit } from '@angular/core';
import { AbstractBase } from '@vmw/bifrost/core';
import { EventBus, ProxyType } from '@vmw/bifrost';

@Component({
    selector: 'chatty-chat',
    templateUrl: './chatty-chat.component.html',
    styleUrls: ['./chatty-chat.component.scss']
})
export class ChattyChatComponent extends AbstractBase implements OnInit {

    public messageCount = 0;
    public flash = false;
    public buttonDisabled = false;

    constructor() {
        super('ChattyChatComponent');
    }

    ngOnInit(): void {

        // enable message proxy.
        this.bus.enableMessageProxy({
            protectedChannels: ['chatty-chat'],
            proxyType: ProxyType.Child,
            parentOrigin: `http://localhost:4200`,
            acceptedOrigins: [
                'http://localhost:4200',
                'http://appfabric.vmware.com'],
            targetAllFrames: true,
            targetSpecificFrames: null,
        });

        // listen to chatty chat, increment message count for every message that comes in.
        this.bus.listenStream('chatty-chat')
            .handle(
                () => {
                    // increase message count.
                    this.messageCount++;

                    // flash message
                    this.flash = true;
                    this.bus.api.tickEventLoop(
                        () => {
                            // re-enable button and unflash message count after a second.
                            this.buttonDisabled = false;
                            this.flash = false;
                        }, 1000
                    );
                }
            );
    }

    sendChat(): void {

        if (!this.buttonDisabled) {
            // disable button.
            this.buttonDisabled = true;

            // send an empty message to chatty-chat.
            this.bus.sendResponseMessage('chatty-chat', {});
        }
    }
}
