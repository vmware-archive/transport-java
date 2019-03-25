import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-java-logging',
    templateUrl: './java-logging.component.html',
    styleUrls: ['./java-logging.component.scss']
})
export class JavaLoggingComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('JavaLoggingComponent');
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
