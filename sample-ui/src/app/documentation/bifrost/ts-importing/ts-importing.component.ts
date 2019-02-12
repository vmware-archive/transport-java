import { Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from '../base.bifrost.component';



@Component({
    selector: 'myprefix-ts-importing',
    templateUrl: './ts-importing.component.html',
    styleUrls: ['./ts-importing.component.scss']
})
export class TsImportingComponent extends BaseBifrostComponent implements OnInit {
    constructor() {
        super('BaseBifröstComponent');
    }

    ngOnInit() {
        super.ngOnInit();
        this.setBifrostTsDocsActive(true);
        //hljs.initHighlighting();
    }

}
