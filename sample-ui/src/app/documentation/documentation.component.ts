import { Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from './bifrost/base.bifrost.component';

@Component({
    selector: 'appfab-documentation',
    templateUrl: './documentation.component.html',
    styleUrls: ['./documentation.component.scss']
})
export class DocumentationComponent extends BaseBifrostComponent implements OnInit {
    public tsDocsActive: string;
    public javaDocsActive: string;

    constructor() {
        super('DocumentationComponent');
    }

    ngOnInit() {
        this.tsDocsActive = String(this.areBifrostTsDocsActive());
        this.javaDocsActive = String(this.areBifrostJavaDocsActive());



    }

}
