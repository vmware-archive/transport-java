import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { HighlightService } from '../../../local-services/highlight.service';
import { BaseDocsComponent } from '../../base.docs.component';
import PingComponent from '../sample-code/react/PingComponent';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';
import { PongService } from '../sample-code/ts/ping-component/pong.service';

@Component({
    selector: 'appfab-ts-calling-services',
    templateUrl: './ts-calling-services.component.html',
    styleUrls: ['./ts-calling-services.component.scss']
})
export class TsCallingServicesComponent extends BaseDocsComponent implements OnInit, OnDestroy, AfterViewChecked {

    reactComponent = PingComponent;
    private pongService: PongService;

    constructor(private highlightService: HighlightService) {
        super('TsHelloWorldComponent');
    }

    ngOnInit() {
        this.setBifrostTsDocsActive(true);
        this.pongService = ServiceLoader.getService(PongService);
        this.pongService.online();
    }

    ngOnDestroy() {
        this.pongService.offline();
    }

    ngAfterViewChecked() {
        if (!this.highlighted) {
            this.highlightService.highlightAll();
            this.highlighted = true;
        }
    }

}
