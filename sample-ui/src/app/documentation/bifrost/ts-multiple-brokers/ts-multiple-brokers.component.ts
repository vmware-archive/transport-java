import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { HighlightService } from '../../../local-services/highlight.service';
import { BaseDocsComponent } from '../../base.docs.component';

@Component({
    selector: 'appfab-ts-multiple-brokers',
    templateUrl: './ts-multiple-brokers.component.html',
    styleUrls: ['./ts-multiple-brokers.component.scss']
})
export class TsMultipleBrokersComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('TsMultipleBrokersComponent');
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
