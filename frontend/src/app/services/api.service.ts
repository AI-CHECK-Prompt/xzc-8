
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

const BASE_URL = 'http://localhost:8080/api';

export interface MonitoringPoint {
  id: number;
  pointCode: string;
  pointName: string;
  location: string;
  deviceType: string;
  deviceCode: string;
  status: string;
  ipAddress: string;
  port: number;
  description: string;
  createTime: string;
  updateTime: string;
}

export interface SensorData {
  id: number;
  pointId: number;
  pointCode: string;
  dataType: string;
  value: number;
  unit: string;
  collectTime: string;
  createTime: string;
}

export interface AlarmRule {
  id: number;
  pointId: number;
  pointCode: string;
  dataType: string;
  compareType: string;
  thresholdValue: number;
  alarmLevel: string;
  enabled: string;
  description: string;
  createTime: string;
  updateTime: string;
}

export interface AlarmRecord {
  id: number;
  pointId: number;
  pointCode: string;
  pointName: string;
  dataType: string;
  currentValue: number;
  unit: string;
  alarmLevel: string;
  alarmMessage: string;
  status: string;
  triggerTime: string;
  handleTime: string;
  handleUser: string;
  handleResult: string;
  createTime: string;
}

@Injectable({ providedIn: 'root' })
export class ApiService {
  constructor(private http: HttpClient) {}

  getPoints(): Observable<MonitoringPoint[]> {
    return this.http.get<MonitoringPoint[]>(`${BASE_URL}/points`);
  }

  getPointById(id: number): Observable<MonitoringPoint> {
    return this.http.get<MonitoringPoint>(`${BASE_URL}/points/${id}`);
  }

  getPointByCode(pointCode: string): Observable<MonitoringPoint> {
    return this.http.get<MonitoringPoint>(`${BASE_URL}/points/code/${pointCode}`);
  }

  createPoint(point: MonitoringPoint): Observable<MonitoringPoint> {
    return this.http.post<MonitoringPoint>(`${BASE_URL}/points`, point);
  }

  updatePoint(id: number, point: MonitoringPoint): Observable<MonitoringPoint> {
    return this.http.put<MonitoringPoint>(`${BASE_URL}/points/${id}`, point);
  }

  deletePoint(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/points/${id}`);
  }

  updatePointStatus(pointCode: string, status: string): Observable<void> {
    return this.http.put<void>(`${BASE_URL}/points/${pointCode}/status/${status}`, {});
  }

  getSensorData(pointCode: string): Observable<SensorData[]> {
    return this.http.get<SensorData[]>(`${BASE_URL}/sensor/data/${pointCode}`);
  }

  getRecentSensorData(pointCode: string, limit: number): Observable<SensorData[]> {
    return this.http.get<SensorData[]>(`${BASE_URL}/sensor/data/${pointCode}/recent/${limit}`);
  }

  submitSensorData(data: SensorData): Observable<SensorData> {
    return this.http.post<SensorData>(`${BASE_URL}/sensor/data`, data);
  }

  getAlarmRules(): Observable<AlarmRule[]> {
    return this.http.get<AlarmRule[]>(`${BASE_URL}/alarm/rules`);
  }

  getAlarmRulesByPoint(pointCode: string): Observable<AlarmRule[]> {
    return this.http.get<AlarmRule[]>(`${BASE_URL}/alarm/rules/point/${pointCode}`);
  }

  createAlarmRule(rule: AlarmRule): Observable<AlarmRule> {
    return this.http.post<AlarmRule>(`${BASE_URL}/alarm/rules`, rule);
  }

  updateAlarmRule(id: number, rule: AlarmRule): Observable<AlarmRule> {
    return this.http.put<AlarmRule>(`${BASE_URL}/alarm/rules/${id}`, rule);
  }

  deleteAlarmRule(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/alarm/rules/${id}`);
  }

  enableAlarmRule(id: number, enabled: boolean): Observable<void> {
    return this.http.put<void>(`${BASE_URL}/alarm/rules/${id}/enable/${enabled}`, {});
  }

  getAlarmRecords(): Observable<AlarmRecord[]> {
    return this.http.get<AlarmRecord[]>(`${BASE_URL}/alarm/records`);
  }

  getRecentAlarms(limit: number): Observable<AlarmRecord[]> {
    return this.http.get<AlarmRecord[]>(`${BASE_URL}/alarm/records/recent/${limit}`);
  }

  getUnhandledAlarms(): Observable<AlarmRecord[]> {
    return this.http.get<AlarmRecord[]>(`${BASE_URL}/alarm/records/unhandled`);
  }

  handleAlarm(id: number, user: string, result: string): Observable<AlarmRecord> {
    return this.http.put<AlarmRecord>(`${BASE_URL}/alarm/records/${id}/handle`, { user, result });
  }
}
