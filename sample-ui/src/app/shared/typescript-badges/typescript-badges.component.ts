import { Component, OnInit } from '@angular/core';

import * as packageJSON from '@vmw/bifrost/package.json';

@Component({
    selector: 'appfab-typescript-badges',
    templateUrl: './typescript-badges.component.html',
    styleUrls: ['./typescript-badges.component.scss']
})
export class TypescriptBadgesComponent implements OnInit {

    packageJSON: any = packageJSON;

    constructor() {
    }

    ngOnInit() {
    }

}
