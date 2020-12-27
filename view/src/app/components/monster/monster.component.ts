import {Component, OnInit} from '@angular/core';
import {HomeService} from "../../services/home.service";

@Component({
  selector: 'app-home',
  templateUrl: './monster.component.html',
  styleUrls: ['./monster.component.scss']
})
export class MonsterComponent implements OnInit {

  helloMsg = 'Welcome to CDDADb!'
  monsters: Object[]
  dense: boolean;

  constructor(private homeService: HomeService) {
    this.dense = true
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
