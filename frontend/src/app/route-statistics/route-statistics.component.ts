
import { Component, OnInit, ViewChild } from '@angular/core';
import { ApiService, TaskStatistics, InspectionTask } from '../services/api.service';
import * as echarts from 'echarts';

@Component({
  selector: 'app-route-statistics',
  templateUrl: './route-statistics.component.html',
  styleUrls: ['./route-statistics.component.scss']
})
export class RouteStatisticsComponent implements OnInit {
  statistics: TaskStatistics | null = null;
  tasks: InspectionTask[] = [];
  
  @ViewChild('taskChart') taskChart: any;
  @ViewChild('distanceChart') distanceChart: any;
  
  private taskChartInstance: echarts.ECharts | null = null;
  private distanceChartInstance: echarts.ECharts | null = null;

  constructor(private apiService: ApiService) {}

  ngOnInit() {
    this.loadStatistics();
    this.loadTasks();
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.initCharts();
    }, 100);
  }

  loadStatistics() {
    this.apiService.getTaskStatistics().subscribe(stats => {
      this.statistics = stats;
    });
  }

  loadTasks() {
    this.apiService.getTasks().subscribe(tasks => {
      this.tasks = tasks;
      this.updateCharts();
    });
  }

  initCharts() {
    this.taskChartInstance = echarts.init(this.taskChart.nativeElement);
    this.distanceChartInstance = echarts.init(this.distanceChart.nativeElement);
    this.updateCharts();
  }

  updateCharts() {
    this.updateTaskChart();
    this.updateDistanceChart();
  }

  updateTaskChart() {
    if (!this.taskChartInstance || !this.statistics) return;

    this.taskChartInstance.setOption({
      title: { text: '任务状态统计', left: 'center', textStyle: { color: '#fff' } },
      tooltip: { trigger: 'item', backgroundColor: 'rgba(0,0,0,0.8)', textStyle: { color: '#fff' } },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#1a1a2e', borderWidth: 2 },
        label: { show: true, color: '#fff' },
        emphasis: { label: { show: true, fontSize: 16 } },
        data: [
          { value: this.statistics.completedTasks, name: '已完成', itemStyle: { color: '#52c41a' } },
          { value: this.statistics.inProgressTasks, name: '进行中', itemStyle: { color: '#1890ff' } },
          { value: this.statistics.pendingTasks, name: '待执行', itemStyle: { color: '#faad14' } }
        ]
      }]
    });
  }

  updateDistanceChart() {
    if (!this.distanceChartInstance) return;

    const completedTasks = this.tasks.filter(t => t.status === 'COMPLETED');
    const xData = completedTasks.map(t => t.taskCode.substring(0, 10));
    const savedData = completedTasks.map(t => t.savedDistance);
    const traveledData = completedTasks.map(t => t.traveledDistance);

    this.distanceChartInstance.setOption({
      title: { text: '路线优化效果', left: 'center', textStyle: { color: '#fff' } },
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.8)', textStyle: { color: '#fff' } },
      legend: { data: ['已走路程', '节省路程'], textStyle: { color: '#fff' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: xData, axisLine: { lineStyle: { color: '#fff' } } },
      yAxis: { type: 'value', name: '距离(m)', axisLine: { lineStyle: { color: '#fff' } }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
      series: [
        { name: '已走路程', type: 'bar', data: traveledData, itemStyle: { color: '#1890ff' } },
        { name: '节省路程', type: 'bar', data: savedData, itemStyle: { color: '#52c41a' } }
      ]
    });
  }

  getCompletionRate(): number {
    if (!this.statistics || this.statistics.totalTasks === 0) return 0;
    return Math.round((this.statistics.completedTasks / this.statistics.totalTasks) * 100);
  }

  getStatusColor(status: string): string {
    return status === 'COMPLETED' ? '#52c41a' : status === 'IN_PROGRESS' ? '#1890ff' : '#faad14';
  }

  getStatusText(status: string): string {
    return status === 'COMPLETED' ? '已完成' : status === 'IN_PROGRESS' ? '进行中' : '待执行';
  }
}
