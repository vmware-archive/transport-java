import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BifrostHomeComponent } from './bifrost-home/bifrost-home.component';
import { ImportingComponent } from './importing/importing.component';

const bifrostRoutes: Routes = [
    {
        path: '',
        component: BifrostHomeComponent,
        children: [
            { path: 'importing', component: ImportingComponent },
        ]
    }
];

@NgModule({
    imports: [RouterModule.forChild(bifrostRoutes)],
    exports: [RouterModule]
})
export class BifrostRoutingModule {}
