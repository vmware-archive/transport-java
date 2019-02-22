import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from '../base.bifrost.component';
import { HighlightService } from '../../../local-services/highlight.service';
import { ProxyType } from '@vmw/bifrost';

@Component({
    selector: 'appfab-ts-distributed-iframes',
    templateUrl: './ts-distributed-iframes.component.html',
    styleUrls: ['./ts-distributed-iframes.component.scss']
})
export class TsDistributedIframesComponent extends BaseBifrostComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('TsDistributedIframesComponent');
    }

    ngOnInit() {
        this.setBifrostTsDocsActive(true);

        // enable message proxy.
        this.bus.enableMessageProxy({
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
        this.bus.listenStream('chatty-chat')
            .handle(() => {}); // do nothing, just ensure the channel is open.
    }

    ngAfterViewChecked() {
        if (!this.highlighted) {
            this.highlightService.highlightAll();
            this.highlighted = true;
        }
    }
}
