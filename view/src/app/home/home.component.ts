import {Component, OnInit} from '@angular/core';
import {HomeService} from "../home.service";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  helloMsg = 'Welcome to CDDADb!'
  monsters: Object[]

  constructor(private homeService: HomeService) {
  }

  ngOnInit(): void {
    this.getMonster()
  }

  getMonster(): void {
    this.homeService
      .getMonsters()
      .subscribe(monsters => this.monsters = monsters)
  }
}
