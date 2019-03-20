import { Component, OnInit } from '@angular/core';

import * as packageJSON from "@vmw/bifrost/package.json";

@Component({
    selector: 'appfab-bifrost-home',
    templateUrl: './bifrost-home.component.html',
    styleUrls: ['./bifrost-home.component.scss']
})
export class BifrostHomeComponent implements OnInit {
    packageJSON: any = packageJSON;
    changeLog: any;

    constructor() {
    }

    ngOnInit() {
        this.changeLog = packageJSON.changelogHistory;
    }

}
