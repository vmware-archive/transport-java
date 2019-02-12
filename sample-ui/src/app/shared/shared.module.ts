import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ParticleHeaderComponent } from '../particle-header/particle-header.component';
import { FooterComponent } from '../footer/footer.component';
import { HeaderComponent } from '../header/header.component';
import { ClarityModule } from '@clr/angular';
import { RouterModule } from '@angular/router';
import { HighlightService } from '../local-services/highlight.service';

@NgModule({
    declarations: [
        ParticleHeaderComponent,
        FooterComponent,
        HeaderComponent
    ],
    imports: [
        CommonModule,
        ClarityModule,
        RouterModule
    ],
    exports: [
        ParticleHeaderComponent,
        FooterComponent,
        HeaderComponent
    ],
    providers: [
        HighlightService
    ]
})
export class SharedModule {
}
