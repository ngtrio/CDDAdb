import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {ShowcaseComponent} from './components/showcase/showcase.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatChipsModule} from "@angular/material/chips";
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatListModule} from "@angular/material/list";
import {MatIconModule} from "@angular/material/icon";
import {MatGridListModule} from '@angular/material/grid-list';

import {HomeComponent} from './components/home/home.component';
import { SideBarComponent } from './components/side-bar/side-bar.component';
import { PanelComponent } from './components/panel/panel.component';


@NgModule({
  declarations: [
    AppComponent,
    ShowcaseComponent,
    HomeComponent,
    SideBarComponent,
    PanelComponent
  ],
  imports: [
    BrowserModule,
    AppRoutingModule,
    HttpClientModule,
    BrowserAnimationsModule,
    MatChipsModule,
    MatSidenavModule,
    MatExpansionModule,
    MatListModule,
    MatIconModule,
    MatGridListModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
