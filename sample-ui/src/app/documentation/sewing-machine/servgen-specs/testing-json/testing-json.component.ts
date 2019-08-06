import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../../base.docs.component';
import { HighlightService } from '../../../../local-services/highlight.service';

@Component({
  selector: 'appfab-servgen',
  templateUrl: './testing-json.component.html',
  styleUrls: ['./testing-json.component.scss']
})
export class TestingJsonComponent extends BaseDocsComponent implements OnInit, OnDestroy, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('ServGenSpecsTestingComponent');
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
