import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-paginator',
  templateUrl: './paginator.component.html',
  styleUrl: './paginator.component.css'
})
export class PaginatorComponent {
  @Input() page = 0;
  @Input() totalPages = 0;
  @Input() totalElements = 0;
  @Input() size = 10;
  @Output() pageChange = new EventEmitter<number>();

  get canGoPrevious(): boolean {
    return this.page > 0;
  }

  get canGoNext(): boolean {
    return this.totalPages > 0 && this.page < this.totalPages - 1;
  }

  previous(): void {
    if (this.canGoPrevious) {
      this.pageChange.emit(this.page - 1);
    }
  }

  next(): void {
    if (this.canGoNext) {
      this.pageChange.emit(this.page + 1);
    }
  }
}
