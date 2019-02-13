/**
 * Copyright(c) VMware Inc. 2019
 */
import { AbstractBase } from '@vmw/bifrost/core';
import { Component } from '@angular/core';

@Component({
    selector: 'appfab-tryitout-component',
    template: `
        <hr/>
        <h4 class="orange">
            ⚡️Run Fabric Code
        </h4>
        <section class="try-it-out">
        <ng-content></ng-content>
    </section>
    <hr/>`,
    styleUrls: ['./tryitout.component.scss']
})
export class TryItOutComponent extends AbstractBase {

    constructor() {
        super('TryItOutComponent');
    }
}
