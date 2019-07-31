import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../../base.docs.component';
import { HighlightService } from '../../../../local-services/highlight.service';

@Component({
  selector: 'appfab-servgen',
  templateUrl: './sets-json.component.html',
  styleUrls: ['./sets-json.component.scss']
})
export class SetsJsonComponent extends BaseDocsComponent implements OnInit, OnDestroy, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('ServGenSpecsSetsComponent');
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
