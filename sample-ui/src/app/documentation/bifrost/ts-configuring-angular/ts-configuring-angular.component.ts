import { Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from '../base.bifrost.component';

@Component({
  selector: 'myprefix-ts-configuring-angular',
  templateUrl: './ts-configuring-angular.component.html',
  styleUrls: ['./ts-configuring-angular.component.scss']
})
export class TsConfiguringAngularComponent extends BaseBifrostComponent implements OnInit {

  constructor() {
      super('ConfiguringAngularComponent');
  }

  ngOnInit() {
      this.setBifrostTsDocsActive(true);
  }

}
