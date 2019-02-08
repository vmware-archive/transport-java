import { Component, OnInit } from '@angular/core';

declare var particlesJS: any;

@Component({
    selector: 'app-welcome',
    templateUrl: './welcome.component.html',
    styleUrls: ['./welcome.component.scss']
})
export class WelcomeComponent implements OnInit {

    constructor() {
    }

    ngOnInit() {
        particlesJS.load('particles-js', 'assets/particles.json', () => {
            console.log('callback - particles.js config loaded');
        });


    }

}
