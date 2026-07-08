
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, switchMap, throwError } from 'rxjs';

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

export interface Route {
  id: number;
  routeName: string;
  routeCode: string;
  totalPoints: number;
  totalDistance: number;
  estimatedTime: number;
  startPointCode: string;
  endPointCode: string;
  status: string;
  description: string;
  creator: string;
  createTime: string;
  updateTime: string;
}

export interface RoutePoint {
  id: number;
  routeId: number;
  routeCode: string;
  pointId: number;
  pointCode: string;
  pointName: string;
  sequence: number;
  distanceFromPrev: number;
  cumulativeDistance: number;
  createTime: string;
}

export interface InspectionTask {
  id: number;
  taskCode: string;
  routeId: number;
  routeCode: string;
  routeName: string;
  assignee: string;
  status: string;
  scheduledStartTime: string;
  scheduledEndTime: string;
  actualStartTime: string;
  actualEndTime: string;
  completedPoints: number;
  totalPoints: number;
  traveledDistance: number;
  savedDistance: number;
  remarks: string;
  createTime: string;
  updateTime: string;
}

export interface TaskStatistics {
  totalTasks: number;
  completedTasks: number;
  inProgressTasks: number;
  pendingTasks: number;
  totalSavedDistance: number;
  avgSavedDistance: number;
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

  getRoutes(): Observable<Route[]> {
    return this.http.get<Route[]>(`${BASE_URL}/route`);
  }

  getRouteById(id: number): Observable<Route> {
    return this.http.get<Route>(`${BASE_URL}/route/${id}`);
  }

  getRoutePoints(routeId: number): Observable<RoutePoint[]> {
    return this.http.get<RoutePoint[]>(`${BASE_URL}/route/${routeId}/points`);
  }

  private validatePointCodes(pointCodes: string[]): Observable<string[]> {
    return this.getPoints().pipe(
      switchMap(points => {
        const validCodes = new Set(points.map(p => p.pointCode));
        const invalidCodes = pointCodes.filter(code => !validCodes.has(code));
        if (invalidCodes.length > 0) {
          return throwError(() => new Error(`以下监控点代码不存在: ${invalidCodes.join(', ')}`));
        }
        return [pointCodes];
      })
    );
  }

  createRoute(routeName: string, pointCodes: string[], startPointCode: string): Observable<Route> {
    const allCodes = [...pointCodes, startPointCode];
    return this.validatePointCodes(allCodes).pipe(
      switchMap(() => this.http.post<Route>(`${BASE_URL}/route`, { routeName, pointCodes, startPointCode }))
    );
  }

  updateRoute(id: number, routeName: string, pointCodes: string[]): Observable<Route> {
    return this.validatePointCodes(pointCodes).pipe(
      switchMap(() => this.http.put<Route>(`${BASE_URL}/route/${id}`, { routeName, pointCodes }))
    );
  }

  deleteRoute(id: number): Observable<void> {
    return this.http.delete<void>(`${BASE_URL}/route/${id}`);
  }

  getTasks(): Observable<InspectionTask[]> {
    return this.http.get<InspectionTask[]>(`${BASE_URL}/task`);
  }

  getTaskById(id: number): Observable<InspectionTask> {
    return this.http.get<InspectionTask>(`${BASE_URL}/task/${id}`);
  }

  getTaskRoutePoints(taskId: number): Observable<RoutePoint[]> {
    return this.http.get<RoutePoint[]>(`${BASE_URL}/task/${taskId}/route-points`);
  }

  getTasksByStatus(status: string): Observable<InspectionTask[]> {
    return this.http.get<InspectionTask[]>(`${BASE_URL}/task/status/${status}`);
  }

  createTask(routeId: number, assignee: string, scheduledStartTime: string, scheduledEndTime: string): Observable<InspectionTask> {
    return this.http.post<InspectionTask>(`${BASE_URL}/task`, { routeId, assignee, scheduledStartTime, scheduledEndTime });
  }

  startTask(id: number): Observable<InspectionTask> {
    return this.http.put<InspectionTask>(`${BASE_URL}/task/${id}/start`, {});
  }

  completeTask(id: number): Observable<InspectionTask> {
    return this.http.put<InspectionTask>(`${BASE_URL}/task/${id}/complete`, {});
  }

  updateTaskProgress(id: number, completedPoints: number, traveledDistance: number): Observable<InspectionTask> {
    return this.http.put<InspectionTask>(`${BASE_URL}/task/${id}/progress`, { completedPoints, traveledDistance });
  }

  getTaskStatistics(): Observable<TaskStatistics> {
    return this.http.get<TaskStatistics>(`${BASE_URL}/task/statistics`);
  }
}
