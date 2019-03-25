import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';
import { PongService } from '../sample-code/ts/ping-component/pong.service';
import { ServiceLoader } from '@vmw/bifrost/util/service.loader';

@Component({
    selector: 'appfab-ts-transactions',
    templateUrl: './ts-transactions.component.html',
    styleUrls: ['./ts-transactions.component.scss']
})
export class TsTransactionsComponent extends BaseDocsComponent implements OnInit, OnDestroy, AfterViewChecked {

    private pongService: PongService;

    constructor(private highlightService: HighlightService) {
        super('TsTransactionsComponent');
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
