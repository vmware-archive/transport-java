import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { HighlightService } from '../../../local-services/highlight.service';
import { BaseBifrostComponent } from '../base.bifrost.component';

@Component({
    selector: 'appfab-ts-calling-services',
    templateUrl: './ts-calling-services.component.html',
    styleUrls: ['./ts-calling-services.component.scss']
})
export class TsCallingServicesComponent extends BaseBifrostComponent implements OnInit, AfterViewChecked {

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
