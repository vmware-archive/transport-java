import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { HomeComponent } from './home/home.component';
import { GettingStartedComponent } from './getting-started/getting-started.component';

@NgModule({
  declarations: [HomeComponent, GettingStartedComponent],
  imports: [
    CommonModule
  ]
})
export class DocumentationModule { }
