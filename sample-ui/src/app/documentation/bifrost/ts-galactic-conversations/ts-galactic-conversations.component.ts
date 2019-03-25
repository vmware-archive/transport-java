import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';

@Component({
  selector: 'appfab-ts-galactic-conversations',
  templateUrl: './ts-galactic-conversations.component.html',
  styleUrls: ['./ts-galactic-conversations.component.scss']
})
export class TsGalacticConversationsComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

    constructor(private highlightService: HighlightService) {
        super('TsAbstractionsComponent');
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
