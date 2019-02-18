import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from '../base.bifrost.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-ts-importing',
    templateUrl: './ts-importing.component.html',
    styleUrls: ['./ts-importing.component.scss']
})
export class TsImportingComponent extends BaseBifrostComponent implements OnInit, AfterViewChecked {
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
