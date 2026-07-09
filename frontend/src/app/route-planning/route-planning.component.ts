import { Component, OnInit } from '@angular/core';
import { ApiService, MonitoringPoint, Route, RoutePoint, InspectionTask, RouteVersion, RouteChangeLog } from '../services/api.service';

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
  versions: RouteVersion[] = [];
  selectedVersion: RouteVersion | null = null;
  changeLogs: RouteChangeLog[] = [];
  recommendedRoute: Route | null = null;
  recommendedRoutePoints: RoutePoint[] = [];
  showVersionHistory: boolean = false;
  showRecommendedRoute: boolean = false;
  recalculateReason: string = '';
  addPointCodes: string[] = [];
  removePointCodes: string[] = [];
  assignInspectors: string = '';
  draggingPoint: RoutePoint | null = null;
  dragOverIndex: number = -1;

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
      const validCodes = new Set(this.points.map(p => p.pointCode));
      const allCodes = [...this.selectedPoints, this.selectedStartPoint];
      const invalidCodes = allCodes.filter(code => !validCodes.has(code));
      if (invalidCodes.length > 0) {
        alert(`以下监控点代码不存在: ${invalidCodes.join(', ')}`);
        return;
      }
      this.apiService.createRoute(this.routeName, this.selectedPoints, this.selectedStartPoint).subscribe({
        next: () => {
          this.loadRoutes();
          this.routeName = '';
          this.selectedPoints = [];
          this.selectedStartPoint = '';
        },
        error: (err) => {
          alert('创建路线失败: ' + err.message);
        }
      });
    }
  }

  selectRoute(route: Route) {
    this.selectedRoute = route;
    this.recommendedRoute = null;
    this.recommendedRoutePoints = [];
    this.showRecommendedRoute = false;
    this.apiService.getRoutePoints(route.id).subscribe(points => {
      this.routePoints = points;
    });
    this.loadVersions(route.id);
  }

  loadVersions(routeId: number) {
    this.apiService.getRouteVersions(routeId).subscribe(versions => {
      this.versions = versions;
    });
  }

  selectVersion(version: RouteVersion) {
    this.selectedVersion = version;
    this.apiService.getVersionChangeLogs(version.id).subscribe(logs => {
      this.changeLogs = logs;
    });
  }

  deleteRoute(id: number) {
    if (confirm('确定要删除这条路线吗？')) {
      this.apiService.deleteRoute(id).subscribe(() => {
        this.loadRoutes();
        this.selectedRoute = null;
        this.routePoints = [];
        this.versions = [];
        this.changeLogs = [];
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

  getPointStatusColor(status: string): string {
    switch (status) {
      case 'COMPLETED': return '#52c41a';
      case 'PENDING': return '#1890ff';
      case 'SKIPPED': return '#d9d9d9';
      default: return '#faad14';
    }
  }

  getPointStatusText(status: string): string {
    switch (status) {
      case 'COMPLETED': return '已完成';
      case 'PENDING': return '待巡检';
      case 'SKIPPED': return '已跳过';
      default: return '未知';
    }
  }

  getChangeTypeText(type: string): string {
    switch (type) {
      case 'ADDED': return '新增';
      case 'REMOVED': return '删除';
      case 'REORDERED': return '顺序调整';
      case 'UNCHANGED': return '无变化';
      case 'CREATE': return '创建';
      case 'RECALCULATE': return '重新计算';
      case 'MANUAL_REORDER': return '手动调整';
      case 'ACCEPT_RECOMMEND': return '采纳推荐';
      default: return type;
    }
  }

  getChangeTypeColor(type: string): string {
    switch (type) {
      case 'ADDED': return '#22c55e';
      case 'REMOVED': return '#ef4444';
      case 'REORDERED': return '#f59e0b';
      case 'UNCHANGED': return '#9ca3af';
      default: return '#6b7280';
    }
  }

  generateRecommendedRoute() {
    if (!this.selectedRoute) return;
    this.apiService.generateRecommendedRoute(this.selectedRoute.id).subscribe({
      next: (route) => {
        this.recommendedRoute = route;
        this.apiService.getRoutePoints(route.id).subscribe(points => {
          this.recommendedRoutePoints = points;
          this.showRecommendedRoute = true;
        });
      },
      error: (err) => {
        alert('生成推荐路线失败: ' + err.message);
      }
    });
  }

  acceptRecommendedRoute() {
    if (!this.selectedRoute || !this.recommendedRoute) return;
    if (confirm('确定要采纳推荐路线吗？这将替换当前路线。')) {
      this.apiService.acceptRecommendedRoute(this.selectedRoute.id, this.recommendedRoute.id).subscribe({
        next: () => {
          this.loadRoutes();
          this.selectRoute(this.selectedRoute!);
          this.recommendedRoute = null;
          this.recommendedRoutePoints = [];
          this.showRecommendedRoute = false;
          alert('推荐路线已采纳');
        },
        error: (err) => {
          alert('采纳推荐路线失败: ' + err.message);
        }
      });
    }
  }

  rejectRecommendedRoute() {
    this.recommendedRoute = null;
    this.recommendedRoutePoints = [];
    this.showRecommendedRoute = false;
  }

  recalculateRoute() {
    if (!this.selectedRoute || !this.recalculateReason) {
      alert('请输入重新计算原因');
      return;
    }
    if (confirm('确定要重新计算路线吗？已完成的部分将保持不变。')) {
      this.apiService.recalculateRoute(
        this.selectedRoute.id,
        this.recalculateReason,
        this.addPointCodes,
        this.removePointCodes
      ).subscribe({
        next: () => {
          this.selectRoute(this.selectedRoute!);
          this.recalculateReason = '';
          this.addPointCodes = [];
          this.removePointCodes = [];
          alert('路线已重新计算');
        },
        error: (err) => {
          alert('重新计算路线失败: ' + err.message);
        }
      });
    }
  }

  updatePointStatus(pointCode: string, status: string) {
    if (!this.selectedRoute) return;
    const point = this.routePoints.find(p => p.pointCode === pointCode);
    if (point && point.inspectionStatus === 'COMPLETED' && status !== 'COMPLETED') {
      if (!confirm('该监控点已完成巡检，确定要更改状态吗？')) {
        return;
      }
    }
    this.apiService.updatePointInspectionStatus(this.selectedRoute.id, pointCode, status).subscribe({
      next: () => {
        this.selectRoute(this.selectedRoute!);
      },
      error: (err) => {
        alert('更新状态失败: ' + err.message);
      }
    });
  }

  assignInspectorsToRoute() {
    if (!this.selectedRoute || !this.assignInspectors.trim()) {
      alert('请输入巡检人员（逗号分隔）');
      return;
    }
    const inspectors = this.assignInspectors.split(',').map(i => i.trim()).filter(i => i);
    this.apiService.assignPointsToInspectors(this.selectedRoute.id, inspectors).subscribe({
      next: () => {
        this.selectRoute(this.selectedRoute!);
        this.assignInspectors = '';
        alert(`已分配给 ${inspectors.join('、')}`);
      },
      error: (err) => {
        alert('分配巡检人员失败: ' + err.message);
      }
    });
  }

  onDragStart(point: RoutePoint, event: DragEvent) {
    if (point.inspectionStatus === 'COMPLETED') {
      event.preventDefault();
      return;
    }
    this.draggingPoint = point;
    event.dataTransfer?.setData('text/plain', point.pointCode);
  }

  onDragOver(index: number, event: DragEvent) {
    event.preventDefault();
    this.dragOverIndex = index;
  }

  onDrop(index: number, event: DragEvent) {
    event.preventDefault();
    if (!this.draggingPoint || !this.selectedRoute) return;
    
    const draggedIndex = this.routePoints.findIndex(p => p.pointCode === this.draggingPoint!.pointCode);
    if (draggedIndex === -1) return;
    
    const targetPoint = this.routePoints[index];
    if (targetPoint.inspectionStatus === 'COMPLETED') {
      alert('不能将监控点拖动到已完成的位置');
      this.dragOverIndex = -1;
      this.draggingPoint = null;
      return;
    }

    const removed = this.routePoints.splice(draggedIndex, 1)[0];
    this.routePoints.splice(index, 0, removed);
    
    this.routePoints.forEach((p, i) => {
      p.sequence = i + 1;
    });

    const pointCodes = this.routePoints.map(p => p.pointCode);
    this.apiService.reorderRoutePoints(this.selectedRoute.id, pointCodes).subscribe({
      next: () => {
        this.loadVersions(this.selectedRoute!.id);
      },
      error: (err) => {
        alert('保存顺序失败: ' + err.message);
        this.selectRoute(this.selectedRoute!);
      }
    });

    this.dragOverIndex = -1;
    this.draggingPoint = null;
  }

  onDragEnd() {
    this.dragOverIndex = -1;
    this.draggingPoint = null;
  }

  getPriorityText(priority: number): string {
    if (!priority) return '普通';
    switch (priority) {
      case 1: return '紧急';
      case 2: return '高';
      case 3: return '中';
      case 4: return '低';
      case 5: return '普通';
      default: return '普通';
    }
  }

  getPriorityColor(priority: number): string {
    if (!priority) return '#999';
    switch (priority) {
      case 1: return '#f5222d';
      case 2: return '#fa541c';
      case 3: return '#faad14';
      case 4: return '#1890ff';
      case 5: return '#999';
      default: return '#999';
    }
  }
}