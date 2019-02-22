import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from '../base.bifrost.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-ts-store-advanced',
    templateUrl: './ts-store-advanced.component.html',
    styleUrls: ['./ts-store-advanced.component.scss']
})
export class TsStoreAdvancedComponent extends BaseBifrostComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('TsStoreAdvancedComponent');
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
