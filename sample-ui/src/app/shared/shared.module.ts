import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ParticleHeaderComponent } from '../particle-header/particle-header.component';
import { FooterComponent } from '../footer/footer.component';
import { HeaderComponent } from '../header/header.component';
import { ClarityModule } from '@clr/angular';
import { RouterModule } from '@angular/router';
import { HighlightService } from '../local-services/highlight.service';
import { FabricConnectionStateComponent } from '../fabric-connection-state/fabric-connection-state.component';
import { JavaBadgesComponent } from './java-badges/java-badges.component';
import { TypescriptBadgesComponent } from './typescript-badges/typescript-badges.component';
import { ChangelogComponent } from './changelog/changelog.component';

@NgModule({
    declarations: [
        ParticleHeaderComponent,
        FooterComponent,
        HeaderComponent,
        FabricConnectionStateComponent,
        JavaBadgesComponent,
        TypescriptBadgesComponent,
        ChangelogComponent
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
        FabricConnectionStateComponent,
        JavaBadgesComponent,
        TypescriptBadgesComponent,
        ChangelogComponent
    ],
    providers: [
        HighlightService
    ]
})
export class SharedModule {
}
