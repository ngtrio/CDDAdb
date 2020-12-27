import {BrowserModule} from '@angular/platform-browser';
import {NgModule} from '@angular/core';
import {HttpClientModule} from '@angular/common/http';
import {AppRoutingModule} from './app-routing.module';
import {AppComponent} from './app.component';
import {MonsterComponent} from './components/monster/monster.component';
import {ThemeComponent} from './components/theme/theme.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatChipsModule} from "@angular/material/chips";
import {SidenavComponent} from './components/sidenav/sidenav.component';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatListModule} from "@angular/material/list";
import {MatIconModule} from "@angular/material/icon";


@NgModule({
  declarations: [
    AppComponent,
    MonsterComponent,
    ThemeComponent,
    SidenavComponent,
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
    MatIconModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule {
}
