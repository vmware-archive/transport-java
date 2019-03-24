import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../base.docs.component';
import { HighlightService } from '../../local-services/highlight.service';

@Component({
    selector: 'app-home',
    templateUrl: './home.component.html',
    styleUrls: ['./home.component.scss']
})
export class HomeComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {
    constructor(private highlightService: HighlightService) {
        super('DocsHomeComponent');
    }x

    ngOnInit() {
        this.setBifrostTsDocsActive(true);
        this.setBifrostJavaDocsActive(true);
    }

    ngAfterViewChecked() {
        if (!this.highlighted) {
            this.highlightService.highlightAll();
            this.highlighted = true;
        }
    }

}
