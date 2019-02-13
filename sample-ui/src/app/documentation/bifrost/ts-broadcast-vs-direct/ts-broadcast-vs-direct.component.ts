import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { HighlightService } from '../../../local-services/highlight.service';
import { BaseBifrostComponent } from '../base.bifrost.component';

@Component({
    selector: 'appfab-ts-broadcast-vs-direct',
    templateUrl: './ts-broadcast-vs-direct.component.html',
    styleUrls: ['./ts-broadcast-vs-direct.component.scss']
})
export class TsBroadcastVsDirectComponent extends BaseBifrostComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('TsHelloWorldComponent');
    }

    ngOnInit() {
        this.setBifrostTsDocsActive(true);
    }

    ngAfterViewChecked() {
        if (!this.highlighted) {
            this.highlightService.highlightAll();
            this.highlighted = true;
        }
    }
}
