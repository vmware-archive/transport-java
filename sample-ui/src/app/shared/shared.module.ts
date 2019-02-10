import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ParticleHeaderComponent } from '../particle-header/particle-header.component';
import { FooterComponent } from '../footer/footer.component';

@NgModule({
    declarations: [
        ParticleHeaderComponent,
        FooterComponent
    ],
    imports: [
        CommonModule
    ],
    exports: [
        ParticleHeaderComponent,
        FooterComponent
    ]
})
export class SharedModule {
}
