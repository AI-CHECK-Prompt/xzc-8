
import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule } from '@angular/forms';
import { NgxEchartsModule } from 'ngx-echarts';

import { AppComponent } from './app.component';
import { DashboardComponent } from './dashboard/dashboard.component';
import { MonitoringPointListComponent } from './monitoring-point-list/monitoring-point-list.component';
import { AlarmListComponent } from './alarm-list/alarm-list.component';
import { PointDetailComponent } from './point-detail/point-detail.component';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    MonitoringPointListComponent,
    AlarmListComponent,
    PointDetailComponent
  ],
  imports: [
    BrowserModule,
    HttpClientModule,
    FormsModule,
    NgxEchartsModule.forRoot({
      echarts: () => import('echarts')
    })
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
