import { CommonModule } from '@angular/common';
import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { ConfirmDialogComponent } from './components/confirm-dialog/confirm-dialog.component';
import { PageHeaderComponent } from './components/page-header/page-header.component';
import { PaginatorComponent } from './components/paginator/paginator.component';

@NgModule({
  declarations: [
    PageHeaderComponent,
    PaginatorComponent,
    ConfirmDialogComponent
  ],
  imports: [
    CommonModule,
    RouterModule
  ],
  exports: [
    PageHeaderComponent,
    PaginatorComponent,
    ConfirmDialogComponent
  ]
})
export class SharedModule {}
