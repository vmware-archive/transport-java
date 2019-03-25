import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
    selector: 'appfab-ts-connecting-to-fabric',
    templateUrl: './ts-connecting-to-fabric.component.html',
    styleUrls: ['./ts-connecting-to-fabric.component.scss']
})
export class TsConnectingToFabricComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('TsConnectingToFabricComponent');
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
