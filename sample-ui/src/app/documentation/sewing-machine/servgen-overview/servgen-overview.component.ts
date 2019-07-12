import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
  selector: 'appfab-servgen',
  templateUrl: './servgen-overview.component.html',
  styleUrls: ['./servgen-overview.component.scss']
})
export class ServGenOverviewComponent extends BaseDocsComponent implements OnInit, OnDestroy, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('ServGenOverviewComponent');
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
