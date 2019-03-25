import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-ts-importing',
    templateUrl: './ts-importing.component.html',
    styleUrls: ['./ts-importing.component.scss']
})
export class TsImportingComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {
    constructor(private highlightService: HighlightService) {
        super('TsImportingComponent');
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
