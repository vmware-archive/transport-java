import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-java-rest',
    templateUrl: './java-rest.component.html',
    styleUrls: ['./java-rest.component.scss']
})
export class JavaRestComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('JavaRestComponent');
    }

    ngOnInit() {
        this.setBifrostJavaDocsActive(true);
    }

    ngAfterViewChecked() {
        if (!this.highlighted) {
            this.highlightService.highlightAll();
            this.highlighted = true;
        }
    }

}
