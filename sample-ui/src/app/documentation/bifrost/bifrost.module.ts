import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ImportingComponent } from './importing/importing.component';
import { BifrostHomeComponent } from './bifrost-home/bifrost-home.component';
import { BifrostRoutingModule } from './bifrost-routing.module';
import { SharedModule } from '../../shared/shared.module';

@NgModule({
    declarations: [ImportingComponent, BifrostHomeComponent],
    imports: [
        CommonModule,
        SharedModule,
        BifrostRoutingModule
    ]
})
export class BifrostModule {
}
