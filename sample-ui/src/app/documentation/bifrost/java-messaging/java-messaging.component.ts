import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-java-messaging',
    templateUrl: './java-messaging.component.html',
    styleUrls: ['./java-messaging.component.scss']
})
export class JavaMessagingComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('JavaMessagingComponent');
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
