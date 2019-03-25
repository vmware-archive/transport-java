import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-java-configuring',
    templateUrl: './java-configuring.component.html',
    styleUrls: ['./java-configuring.component.scss']
})
export class JavaConfiguringComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('JavaConfiguringComponent');
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