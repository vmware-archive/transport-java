import { BaseComponent } from '../../models/abstractions/base.component';
import { OnInit } from '@angular/core';
declare var hljs: any;

export abstract class BaseBifrostComponent extends BaseComponent implements OnInit {

    public highlighted = false;
    protected areBifrostTsDocsActive(): boolean {
        return this.storeManager.getStore<boolean>('docs').get('ts');
    }

    protected areBifrostJavaDocsActive(): boolean {
        return this.storeManager.getStore<boolean>('docs').get('java');
    }

    protected setBifrostTsDocsActive(state: boolean): void {
        this.storeManager.getStore<boolean>('docs').put('ts', state, null);
    }

    protected setBifrostJavaDocsActive(state: boolean): void {
        this.storeManager.getStore<boolean>('docs').put('java', state, null);
    }

    public ngOnInit() {
    }
}
