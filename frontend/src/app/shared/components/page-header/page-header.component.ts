import { Component, Input } from '@angular/core';

@Component({
    selector: 'app-page-header',
    templateUrl: './page-header.component.html',
    styleUrl: './page-header.component.css',
    standalone: false
})
export class PageHeaderComponent {
  @Input() kicker = '';
  @Input() title = '';
  @Input() description = '';
}
