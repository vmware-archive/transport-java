import { Component, OnInit } from '@angular/core';

@Component({
    selector: 'appfab-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss']
})
export class HeaderComponent implements OnInit {

    showAlerts = true;

    constructor() {
    }

    ngOnInit() {
    }

    hideAlerts() {
        this.showAlerts = false;
    }



}
