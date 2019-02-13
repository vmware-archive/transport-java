import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from '../base.bifrost.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-ts-services',
    templateUrl: './ts-services.component.html',
    styleUrls: ['./ts-services.component.scss']
})
export class TsServicesComponent extends BaseBifrostComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('TsServicesComponent');
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
