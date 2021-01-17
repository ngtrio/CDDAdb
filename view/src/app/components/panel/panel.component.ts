import { Component, OnInit } from '@angular/core';
import {ShowcaseService} from "../../services/showcase.service";

@Component({
  selector: 'app-panel',
  templateUrl: './panel.component.html',
  styleUrls: ['./panel.component.scss']
})
export class PanelComponent implements OnInit {

  json: string

  constructor(private showcaseService: ShowcaseService) { }

  ngOnInit(): void {
    this.showcaseService.doGetOne()
      .subscribe(object => this.json = JSON.stringify(object))
  }
}
