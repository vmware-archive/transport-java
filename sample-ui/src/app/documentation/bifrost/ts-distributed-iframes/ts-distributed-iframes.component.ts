import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseBifrostComponent } from '../base.bifrost.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
  selector: 'appfab-ts-distributed-iframes',
  templateUrl: './ts-distributed-iframes.component.html',
  styleUrls: ['./ts-distributed-iframes.component.scss']
})
export class TsDistributedIframesComponent extends BaseBifrostComponent implements OnInit, AfterViewChecked {

  constructor(private highlightService: HighlightService) {
    super('TsStoreBasicsComponent');
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
