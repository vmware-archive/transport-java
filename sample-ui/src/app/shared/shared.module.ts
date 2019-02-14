import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ParticleHeaderComponent } from '../particle-header/particle-header.component';
import { FooterComponent } from '../footer/footer.component';
import { HeaderComponent } from '../header/header.component';
import { ClarityModule } from '@clr/angular';
import { RouterModule } from '@angular/router';
import { HighlightService } from '../local-services/highlight.service';
import { FabricConnectionStateComponent } from '../fabric-connection-state/fabric-connection-state.component';

@NgModule({
    declarations: [
        ParticleHeaderComponent,
        FooterComponent,
        HeaderComponent,
        FabricConnectionStateComponent
    ],
    imports: [
        CommonModule,
        ClarityModule,
        RouterModule
    ],
    exports: [
        ParticleHeaderComponent,
        FooterComponent,
        HeaderComponent,
        FabricConnectionStateComponent
    ],
    providers: [
        HighlightService
    ]
})
export class SharedModule {
}
