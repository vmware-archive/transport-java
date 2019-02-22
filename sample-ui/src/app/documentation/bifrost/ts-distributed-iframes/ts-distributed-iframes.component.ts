import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { BaseBifrostComponent } from '../base.bifrost.component';
import { HighlightService } from '../../../local-services/highlight.service';
import { MessageHandler, ProxyControl, ProxyType } from '@vmw/bifrost';

@Component({
    selector: 'appfab-ts-distributed-iframes',
    templateUrl: './ts-distributed-iframes.component.html',
    styleUrls: ['./ts-distributed-iframes.component.scss']
})
export class TsDistributedIframesComponent extends BaseBifrostComponent implements OnInit, AfterViewChecked, OnDestroy {

    private chatStream: MessageHandler<any>;
    private proxyControl: ProxyControl;

    constructor(private highlightService: HighlightService) {
        super('TsDistributedIframesComponent');
    }

    ngOnInit() {
        this.setBifrostTsDocsActive(true);

        // enable message proxy.
        this.proxyControl = this.bus.enableMessageProxy({
            protectedChannels: ['chatty-chat'],
            proxyType: ProxyType.Parent,
            parentOrigin: `http://appfabric.vmware.com`,
            acceptedOrigins: [
                'http://localhost:4200',
                'http://appfabric.vmware.com'],
            targetAllFrames: true,
            targetSpecificFrames: null,
        });

        // ensure we open the chatty chat channel, to ensure messages are not dropped.
        // we don't however care about doing anything with the traffic on the stream here.
        this.chatStream = this.bus.listenStream('chatty-chat');
        this.chatStream.handle(() => {
        }); // do nothing, just ensure the channel is open.
    }

    ngOnDestroy() {
        super.ngOnDestroy();
        this.chatStream.close();
        this.proxyControl.stopListening();
    }

    ngAfterViewChecked() {
        if (!this.highlighted) {
            this.highlightService.highlightAll();
            this.highlighted = true;
        }
    }
}
