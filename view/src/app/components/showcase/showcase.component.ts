import {Component, OnInit} from '@angular/core';
import {ShowcaseService} from "../../services/showcase.service";

@Component({
  selector: 'app-showcase',
  templateUrl: './showcase.component.html',
  styleUrls: ['./showcase.component.scss']
})
export class ShowcaseComponent implements OnInit {

  type: string
  objects: Object[]
  dense: boolean;

  constructor(private service: ShowcaseService) {
    this.dense = true
  }

  ngOnInit(): void {
    this.service.doListAll()
      .subscribe(objects => {
        this.type = objects[0]
        this.objects = objects[1]
      })
  }

  getOne(id: string): void {
    this.service.getOne(this.type, id)
  }
}
