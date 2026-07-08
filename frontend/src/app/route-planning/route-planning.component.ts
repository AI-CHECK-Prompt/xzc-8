
import { Component, OnInit } from '@angular/core';
import { ApiService, MonitoringPoint, Route, RoutePoint, InspectionTask } from '../services/api.service';

@Component({
  selector: 'app-route-planning',
  templateUrl: './route-planning.component.html',
  styleUrls: ['./route-planning.component.scss']
})
export class RoutePlanningComponent implements OnInit {
  points: MonitoringPoint[] = [];
  routes: Route[] = [];
  selectedPoints: string[] = [];
  selectedStartPoint: string = '';
  routeName: string = '';
  selectedRoute: Route | null = null;
  routePoints: RoutePoint[] = [];
  tasks: InspectionTask[] = [];
  newTaskAssignee: string = '';
  newTaskStartTime: string = '';
  newTaskEndTime: string = '';

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadPoints();
    this.loadRoutes();
    this.loadTasks();
  }

  loadPoints() {
    this.apiService.getPoints().subscribe(points => {
      this.points = points;
    });
  }

  loadRoutes() {
    this.apiService.getRoutes().subscribe(routes => {
      this.routes = routes;
    });
  }

  loadTasks() {
    this.apiService.getTasks().subscribe(tasks => {
      this.tasks = tasks;
    });
  }

  togglePoint(pointCode: string) {
    const index = this.selectedPoints.indexOf(pointCode);
    if (index > -1) {
      this.selectedPoints.splice(index, 1);
    } else {
      this.selectedPoints.push(pointCode);
    }
  }

  createRoute() {
    if (this.selectedPoints.length > 0 && this.routeName && this.selectedStartPoint) {
      this.apiService.createRoute(this.routeName, this.selectedPoints, this.selectedStartPoint).subscribe(() => {
        this.loadRoutes();
        this.routeName = '';
        this.selectedPoints = [];
        this.selectedStartPoint = '';
      });
    }
  }

  selectRoute(route: Route) {
    this.selectedRoute = route;
    this.apiService.getRoutePoints(route.id).subscribe(points => {
      this.routePoints = points;
    });
  }

  deleteRoute(id: number) {
    if (confirm('确定要删除这条路线吗？')) {
      this.apiService.deleteRoute(id).subscribe(() => {
        this.loadRoutes();
        this.selectedRoute = null;
        this.routePoints = [];
      });
    }
  }

  createTask() {
    if (this.selectedRoute && this.newTaskAssignee) {
      this.apiService.createTask(
        this.selectedRoute.id,
        this.newTaskAssignee,
        this.newTaskStartTime,
        this.newTaskEndTime
      ).subscribe(() => {
        this.loadTasks();
        this.newTaskAssignee = '';
        this.newTaskStartTime = '';
        this.newTaskEndTime = '';
      });
    }
  }

  startTask(task: InspectionTask) {
    if (confirm('确定要开始执行此任务吗？')) {
      this.apiService.startTask(task.id).subscribe(() => {
        this.loadTasks();
      });
    }
  }

  completeTask(task: InspectionTask) {
    if (confirm('确定要完成此任务吗？')) {
      this.apiService.completeTask(task.id).subscribe(() => {
        this.loadTasks();
      });
    }
  }

  getStatusColor(status: string): string {
    return status === 'COMPLETED' ? '#52c41a' : status === 'IN_PROGRESS' ? '#1890ff' : '#faad14';
  }

  getStatusText(status: string): string {
    return status === 'COMPLETED' ? '已完成' : status === 'IN_PROGRESS' ? '进行中' : '待执行';
  }

  getProgress(task: InspectionTask): number {
    return task.totalPoints > 0 ? (task.completedPoints / task.totalPoints) * 100 : 0;
  }
}
