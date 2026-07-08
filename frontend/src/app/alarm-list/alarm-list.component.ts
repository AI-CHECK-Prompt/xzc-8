
import { Component, OnInit } from '@angular/core';
import { ApiService, AlarmRecord } from '../services/api.service';
import { WebSocketService } from '../services/websocket.service';

@Component({
  selector: 'app-alarm-list',
  templateUrl: './alarm-list.component.html',
  styleUrls: ['./alarm-list.component.scss']
})
export class AlarmListComponent implements OnInit {
  alarms: AlarmRecord[] = [];
  selectedAlarm: AlarmRecord | null = null;
  handleResult = '';

  constructor(private apiService: ApiService, private wsService: WebSocketService) {}

  ngOnInit() {
    this.loadAlarms();
    this.setupWebSocketListener();
  }

  loadAlarms() {
    this.apiService.getAlarmRecords().subscribe(alarms => {
      this.alarms = alarms;
    });
  }

  setupWebSocketListener() {
    this.wsService.alarmReceived.subscribe(alarm => {
      this.alarms.unshift(alarm);
    });
  }

  handleAlarmRecord(alarm: AlarmRecord) {
    this.selectedAlarm = alarm;
    this.handleResult = '';
  }

  confirmHandle() {
    if (this.selectedAlarm && this.handleResult) {
      this.apiService.handleAlarm(this.selectedAlarm.id, 'admin', this.handleResult).subscribe(() => {
        this.loadAlarms();
        this.selectedAlarm = null;
        this.handleResult = '';
      });
    }
  }

  getLevelColor(level: string): string {
    return level === 'DANGER' ? '#ff4d4f' : level === 'WARNING' ? '#faad14' : '#1890ff';
  }

  getLevelText(level: string): string {
    return level === 'DANGER' ? '危险' : level === 'WARNING' ? '预警' : '信息';
  }

  getStatusText(status: string): string {
    return status === 'HANDLED' ? '已处理' : '未处理';
  }
}
