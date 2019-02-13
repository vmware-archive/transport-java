import { Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from '../base.bifrost.component';

@Component({
    selector: 'myprefix-java-importing',
    templateUrl: './java-importing.component.html',
    styleUrls: ['./java-importing.component.scss']
})
export class JavaImportingComponent extends BaseBifrostComponent implements OnInit {

    constructor() {
        super('BaseBifrostComponent');
    }

    ngOnInit() {
        this.setBifrostJavaDocsActive(true);
    }
}
