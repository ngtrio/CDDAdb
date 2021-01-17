import { Component, OnInit } from '@angular/core';

export interface Tile {
  color: string;
  cols: number;
  rows: number;
  class: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  colsPerRow = 16

  tiles: Tile[] = [
    {class: 'header', cols: 16, rows: 1, color: 'lightblue'},
    {class: 'sideBar', cols: 1, rows: 7, color: 'lightblue'},
    {class: 'showCase', cols: 3, rows: 7, color: 'lightblue'},
    {class: 'panel', cols: 12, rows: 7, color: 'lightblue'},
    {class: 'footer', cols: 16, rows: 1, color: 'lightblue'},
  ]

  constructor() { }

  ngOnInit(): void {
  }

}
