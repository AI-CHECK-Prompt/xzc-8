
import { Component, OnInit } from '@angular/core';
import { ApiService, MonitoringPoint } from '../services/api.service';

@Component({
  selector: 'app-monitoring-point-list',
  templateUrl: './monitoring-point-list.component.html',
  styleUrls: ['./monitoring-point-list.component.scss']
})
export class MonitoringPointListComponent implements OnInit {
  points: MonitoringPoint[] = [];
  selectedPoint: MonitoringPoint | null = null;
  isEditing = false;
  newPoint: MonitoringPoint = this.createEmptyPoint();

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadPoints();
  }

  loadPoints() {
    this.apiService.getPoints().subscribe(points => {
      this.points = points;
    });
  }

  createEmptyPoint(): MonitoringPoint {
    return {
      id: 0,
      pointCode: '',
      pointName: '',
      location: '',
      deviceType: '',
      deviceCode: '',
      status: 'NORMAL',
      ipAddress: '',
      port: 8080,
      description: '',
      createTime: '',
      updateTime: ''
    };
  }

  addPoint() {
    this.newPoint = this.createEmptyPoint();
    this.selectedPoint = null;
    this.isEditing = true;
  }

  editPoint(point: MonitoringPoint) {
    this.selectedPoint = point;
    this.newPoint = { ...point };
    this.isEditing = true;
  }

  savePoint() {
    if (this.selectedPoint) {
      this.apiService.updatePoint(this.selectedPoint.id, this.newPoint).subscribe(() => {
        this.loadPoints();
        this.cancelEdit();
      });
    } else {
      this.apiService.createPoint(this.newPoint).subscribe(() => {
        this.loadPoints();
        this.cancelEdit();
      });
    }
  }

  cancelEdit() {
    this.isEditing = false;
    this.selectedPoint = null;
    this.newPoint = this.createEmptyPoint();
  }

  deletePoint(id: number) {
    if (confirm('确定要删除这个监控点吗？')) {
      this.apiService.deletePoint(id).subscribe(() => {
        this.loadPoints();
      });
    }
  }

  getStatusColor(status: string): string {
    return status === 'ALARM' ? '#ff4d4f' : status === 'WARNING' ? '#faad14' : '#52c41a';
  }

  getStatusText(status: string): string {
    return status === 'ALARM' ? '告警' : status === 'WARNING' ? '预警' : '正常';
  }
}
