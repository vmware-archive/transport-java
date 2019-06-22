import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { HighlightService } from '../../local-services/highlight.service';
import { BaseDocsComponent } from '../base.docs.component';

@Component({
  selector: 'app-getting-started',
  templateUrl: './getting-started.component.html',
  styleUrls: ['./getting-started.component.scss']
})
export class GettingStartedComponent extends BaseDocsComponent implements  OnInit, OnDestroy, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('GettingStartedComponent');
    }

    ngOnInit() {
        this.setSewingMachineDocsActive(true);
    }

    ngOnDestroy() {
    }

    ngAfterViewChecked() {
        if (!this.highlighted) {
            this.highlightService.highlightAll();
            this.highlighted = true;
        }
    }

}
