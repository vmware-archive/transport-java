import { Component, OnInit } from '@angular/core';
import { VERSION } from '@appfab/environments/version';

@Component({
    selector: 'footer-component',
    templateUrl: './footer.component.html',
    styleUrls: ['./footer.component.scss']
})
export class FooterComponent implements OnInit {

    date = new Date().getFullYear();
    version = VERSION;
    buildTime = Date.parse(VERSION.time);

    constructor() {
    }

    ngOnInit() {
    }

}
