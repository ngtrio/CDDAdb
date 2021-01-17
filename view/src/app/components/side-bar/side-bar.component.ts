import { Component, OnInit } from '@angular/core';
import {ShowcaseComponent} from "../showcase/showcase.component";
import {ShowcaseService} from "../../services/showcase.service";

@Component({
  selector: 'app-side-bar',
  templateUrl: './side-bar.component.html',
  styleUrls: ['./side-bar.component.scss']
})
export class SideBarComponent implements OnInit {

  constructor(private showcaseService: ShowcaseService) { }

  ngOnInit(): void {
  }

  issueRequest(type: string): void {
    this.showcaseService.listAll(type)
  }
}
