import { Component, OnInit } from '@angular/core';

declare var particlesJS: any;

@Component({
  selector: 'particle-header',
  templateUrl: './particle-header.component.html',
  styleUrls: ['./particle-header.component.scss']
})
export class ParticleHeaderComponent implements OnInit {

  constructor() { }

  ngOnInit() {
      particlesJS.load('fabric-bg', '/assets/particles-small.json');
  }

}
