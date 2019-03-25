import { AfterViewChecked, Component, OnInit } from '@angular/core';
import { BaseDocsComponent } from '../../base.docs.component';
import { HighlightService } from '../../../local-services/highlight.service';
import HelloWorld from '../sample-code/react/HelloWorld';
import React from 'react';

@Component({
  selector: 'appfab-ts-helloworld',
  templateUrl: './ts-helloworld.component.html',
  styleUrls: ['./ts-helloworld.component.scss']
})
export class TsHelloworldComponent extends BaseDocsComponent implements OnInit, AfterViewChecked {

    reactComponent: React.FC = HelloWorld;

    constructor(private highlightService: HighlightService) {
        super('TsHelloWorldComponent');
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
