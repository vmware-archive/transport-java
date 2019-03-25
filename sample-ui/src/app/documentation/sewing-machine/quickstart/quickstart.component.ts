import { AfterViewChecked, Component, OnDestroy, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-quickstart',
    templateUrl: './quickstart.component.html',
    styleUrls: ['./quickstart.component.scss']
})
export class QuickstartComponent extends BaseDocsComponent implements OnInit, OnDestroy, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('SMQuickStartComponent');
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
