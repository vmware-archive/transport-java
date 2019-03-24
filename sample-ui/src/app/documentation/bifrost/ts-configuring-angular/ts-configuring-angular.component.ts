import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
  selector: 'myprefix-ts-configuring-angular',
  templateUrl: './ts-configuring-angular.component.html',
  styleUrls: ['./ts-configuring-angular.component.scss']
})
export class TsConfiguringAngularComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

  constructor(private highlightService: HighlightService) {
      super('ConfiguringAngularComponent');
  }

  ngOnInit() {
      this.setBifrostTsDocsActive(true);
  }

    ngAfterViewChecked() {
        if (!this.highlighted) {
            this.highlightService.highlightAll();
            this.highlighted = true;
        }
    }

}
