import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'myprefix-java-helloworld',
    templateUrl: './java-helloworld.component.html',
    styleUrls: ['./java-helloworld.component.scss']
})
export class JavaHelloworldComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('JavaHelloWorldComponent');
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
