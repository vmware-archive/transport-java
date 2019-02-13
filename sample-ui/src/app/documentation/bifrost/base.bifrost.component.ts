import { BaseComponent } from '../../models/abstractions/base.component';
import { OnInit } from '@angular/core';

export abstract class BaseBifrostComponent extends BaseComponent implements OnInit {

    public highlighted = false;
    public areBifrostTsDocsActive(): boolean {
        return this.storeManager.getStore<boolean>('docs').get('ts');
    }

    public areBifrostJavaDocsActive(): boolean {
        return this.storeManager.getStore<boolean>('docs').get('java');
    }

    public setBifrostTsDocsActive(state: boolean): void {
        this.storeManager.getStore<boolean>('docs').put('ts', state, null);
    }

    public setBifrostJavaDocsActive(state: boolean): void {
        this.storeManager.getStore<boolean>('docs').put('java', state, null);
    }

    public ngOnInit() {
    }
}
