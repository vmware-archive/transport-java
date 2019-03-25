import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-ts-logging',
    templateUrl: './ts-logging.component.html',
    styleUrls: ['./ts-logging.component.scss']
})
export class TsLoggingComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('TsLoggingComponent');
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
