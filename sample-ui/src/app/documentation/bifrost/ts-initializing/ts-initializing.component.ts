import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from '../base.bifrost.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-ts-initializing',
    templateUrl: './ts-initializing.component.html',
    styleUrls: ['./ts-initializing.component.scss']
})
export class TsInitializingComponent extends BaseBifrostComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('InitializingComponent');
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
