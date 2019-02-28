import { Component, OnInit, OnChanges, AfterViewInit, OnDestroy, Input } from '@angular/core';
import React from 'react';
import ReactDOM from 'react-dom';
import uuid from 'uuid';
import { Provider, Bifrost } from '../../../react-bifrost';

@Component({
    selector: 'react-sample',
    template: `<div [id]="rootDomID"></div>`
})
export class ReactSampleComponent implements OnInit, OnChanges, AfterViewInit, OnDestroy {

    public message: string;
    public bifrost = new Bifrost();

    @Input() component;

    rootDomID: string;

    protected getRootDomNode() {
        const node = document.getElementById(this.rootDomID);

        if (!node) {
            console.error(node, `Node '${this.rootDomID} not found!`);
        }

        return node;
    }

    private isMounted(): boolean {
        return !!this.rootDomID;
    }

    protected render() {
        const ReactComponent = this.component;

        if (this.isMounted()) {
            ReactDOM.render(
                <Provider bifrost={this.bifrost}>
                    <ReactComponent />
                </Provider>,
                this.getRootDomNode()
            );
        }
    }

    ngOnInit() {
        this.rootDomID = uuid.v1();
    }

    ngOnChanges() {
        this.render();
    }

    ngAfterViewInit() {
        this.render();
    }

    ngOnDestroy() {
        ReactDOM.unmountComponentAtNode(this.getRootDomNode());
    }
}
