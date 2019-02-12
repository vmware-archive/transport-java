import { NgModule } from '@angular/core';
import { Routes, RouterModule } from '@angular/router';
import { BifrostHomeComponent } from './bifrost-home/bifrost-home.component';
import { TsImportingComponent } from './ts-importing/ts-importing.component';
import { TsConfiguringComponent } from './ts-configuring/ts-configuring.component';
import { TsConfiguringAngularComponent } from './ts-configuring-angular/ts-configuring-angular.component';
import { TsInitializingComponent } from './ts-initializing/ts-initializing.component';
import { TsHelloworldComponent } from './ts-helloworld/ts-helloworld.component';
import { JavaImportingComponent } from './java-importing/java-importing.component';
import { JavaConfiguringComponent } from './java-configuring/java-configuring.component';
import { JavaInitializingComponent } from './java-initializing/java-initializing.component';
import { JavaHelloworldComponent } from './java-helloworld/java-helloworld.component';

const bifrostRoutes: Routes = [
    {path: '', component: BifrostHomeComponent, pathMatch: 'true'},
    {path: 'ts/importing', component: TsImportingComponent, pathMatch: 'true'},
    {path: 'ts/configuring', component: TsConfiguringComponent, pathMatch: 'true'},
    {path: 'ts/configuring-angular', component: TsConfiguringAngularComponent, pathMatch: 'true'},
    {path: 'ts/initializing', component: TsInitializingComponent, pathMatch: 'true'},
    {path: 'ts/helloworld', component: TsHelloworldComponent, pathMatch: 'true'},
    {path: 'java/importing', component: JavaImportingComponent, pathMatch: 'true'},
    {path: 'java/configuring', component: JavaConfiguringComponent, pathMatch: 'true'},
    {path: 'java/initializing', component: JavaInitializingComponent, pathMatch: 'true'},
    {path: 'java/helloworld', component: JavaHelloworldComponent, pathMatch: 'true'},
];

@NgModule({
    imports: [RouterModule.forChild(bifrostRoutes)],
    exports: [RouterModule]
})
export class BifrostRoutingModule {
}
