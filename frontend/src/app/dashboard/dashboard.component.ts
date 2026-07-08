
import { Component, OnInit, OnDestroy, ViewChild } from '@angular/core';
import { ApiService, MonitoringPoint, SensorData, AlarmRecord } from '../services/api.service';
import { WebSocketService } from '../services/websocket.service';
import * as echarts from 'echarts';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit, OnDestroy {
  points: MonitoringPoint[] = [];
  recentAlarms: AlarmRecord[] = [];
  sensorDataMap: Map<string, SensorData[]> = new Map();
  
  @ViewChild('gasChart') gasChart: any;
  @ViewChild('tempChart') tempChart: any;
  @ViewChild('alarmChart') alarmChart: any;
  
  private gasChartInstance: echarts.ECharts | null = null;
  private tempChartInstance: echarts.ECharts | null = null;
  private alarmChartInstance: echarts.ECharts | null = null;

  constructor(private apiService: ApiService, private wsService: WebSocketService) {}

  ngOnInit() {
    this.loadData();
    this.setupWebSocketListeners();
  }

  ngOnDestroy() {
    if (this.gasChartInstance) this.gasChartInstance.dispose();
    if (this.tempChartInstance) this.tempChartInstance.dispose();
    if (this.alarmChartInstance) this.alarmChartInstance.dispose();
  }

  loadData() {
    this.apiService.getPoints().subscribe(points => {
      this.points = points;
      this.loadSensorData();
    });
    
    this.apiService.getRecentAlarms(10).subscribe(alarms => {
      this.recentAlarms = alarms;
      this.updateAlarmChart();
    });
  }

  loadSensorData() {
    this.points.forEach(point => {
      this.apiService.getRecentSensorData(point.pointCode, 20).subscribe(data => {
        this.sensorDataMap.set(point.pointCode, data);
      });
    });
  }

  setupWebSocketListeners() {
    this.wsService.sensorDataReceived.subscribe(data => {
      const existing = this.sensorDataMap.get(data.pointCode) || [];
      existing.unshift(data);
      if (existing.length > 20) existing.pop();
      this.sensorDataMap.set(data.pointCode, existing);
      
      const point = this.points.find(p => p.pointCode === data.pointCode);
      if (point) {
        this.updateCharts();
      }
    });

    this.wsService.alarmReceived.subscribe(alarm => {
      this.recentAlarms.unshift(alarm);
      if (this.recentAlarms.length > 10) this.recentAlarms.pop();
      this.updateAlarmChart();
    });

    this.wsService.pointStatusChanged.subscribe(status => {
      const point = this.points.find(p => p.pointCode === status.pointCode);
      if (point) {
        point.status = status.status;
      }
    });
  }

  ngAfterViewInit() {
    setTimeout(() => {
      this.initCharts();
    }, 100);
  }

  initCharts() {
    this.gasChartInstance = echarts.init(this.gasChart.nativeElement);
    this.tempChartInstance = echarts.init(this.tempChart.nativeElement);
    this.alarmChartInstance = echarts.init(this.alarmChart.nativeElement);
    this.updateCharts();
  }

  updateCharts() {
    this.updateGasChart();
    this.updateTempChart();
    this.updateAlarmChart();
  }

  updateGasChart() {
    if (!this.gasChartInstance) return;
    
    const gasPoints = this.points.filter(p => p.deviceType === '瓦斯传感器');
    const series = gasPoints.map(point => {
      const data = this.sensorDataMap.get(point.pointCode) || [];
      return {
        name: point.pointName,
        type: 'line',
        data: data.map(d => d.value),
        smooth: true,
        lineStyle: { width: 2 },
        symbol: 'circle',
        symbolSize: 6
      };
    });

    this.gasChartInstance.setOption({
      title: { text: '瓦斯浓度监测', left: 'center', textStyle: { color: '#fff' } },
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.8)', textStyle: { color: '#fff' } },
      legend: { data: gasPoints.map(p => p.pointName), textStyle: { color: '#fff' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: Array(20).fill(''), axisLine: { lineStyle: { color: '#fff' } } },
      yAxis: { type: 'value', name: '浓度(%)', axisLine: { lineStyle: { color: '#fff' } }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
      series
    });
  }

  updateTempChart() {
    if (!this.tempChartInstance) return;
    
    const tempPoints = this.points.filter(p => p.deviceType === '温度传感器');
    const series = tempPoints.map(point => {
      const data = this.sensorDataMap.get(point.pointCode) || [];
      return {
        name: point.pointName,
        type: 'line',
        data: data.map(d => d.value),
        smooth: true,
        lineStyle: { width: 2 },
        symbol: 'circle',
        symbolSize: 6
      };
    });

    this.tempChartInstance.setOption({
      title: { text: '温度监测', left: 'center', textStyle: { color: '#fff' } },
      tooltip: { trigger: 'axis', backgroundColor: 'rgba(0,0,0,0.8)', textStyle: { color: '#fff' } },
      legend: { data: tempPoints.map(p => p.pointName), textStyle: { color: '#fff' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: Array(20).fill(''), axisLine: { lineStyle: { color: '#fff' } } },
      yAxis: { type: 'value', name: '温度(°C)', axisLine: { lineStyle: { color: '#fff' } }, splitLine: { lineStyle: { color: 'rgba(255,255,255,0.1)' } } },
      series
    });
  }

  updateAlarmChart() {
    if (!this.alarmChartInstance) return;
    
    const levelCounts = { WARNING: 0, DANGER: 0, INFO: 0 };
    this.recentAlarms.forEach(alarm => {
      if (levelCounts[alarm.alarmLevel as keyof typeof levelCounts] !== undefined) {
        levelCounts[alarm.alarmLevel as keyof typeof levelCounts]++;
      }
    });

    this.alarmChartInstance.setOption({
      title: { text: '告警统计', left: 'center', textStyle: { color: '#fff' } },
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
          { value: levelCounts.DANGER, name: '危险', itemStyle: { color: '#ff4d4f' } },
          { value: levelCounts.WARNING, name: '预警', itemStyle: { color: '#faad14' } },
          { value: levelCounts.INFO, name: '信息', itemStyle: { color: '#1890ff' } }
        ]
      }]
    });
  }

  getStatusColor(status: string): string {
    return status === 'ALARM' ? '#ff4d4f' : status === 'WARNING' ? '#faad14' : '#52c41a';
  }

  getStatusText(status: string): string {
    return status === 'ALARM' ? '告警' : status === 'WARNING' ? '预警' : '正常';
  }

  getLatestValue(pointCode: string): string {
    const data = this.sensorDataMap.get(pointCode);
    return data && data.length > 0 ? `${data[0].value}${data[0].unit}` : '--';
  }
}
