
import { Injectable, EventEmitter } from '@angular/core';
import { SensorData, AlarmRecord } from './api.service';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private ws: WebSocket | null = null;
  
  sensorDataReceived = new EventEmitter<SensorData>();
  alarmReceived = new EventEmitter<AlarmRecord>();
  pointStatusChanged = new EventEmitter<{ pointCode: string; status: string }>();

  connect() {
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      return;
    }
    
    this.ws = new WebSocket('ws://localhost:8080/ws/safety');
    
    this.ws.onopen = () => {
      console.log('WebSocket connected');
    };
    
    this.ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        switch (message.type) {
          case 'sensorData':
            this.sensorDataReceived.emit(message.data);
            break;
          case 'alarm':
            this.alarmReceived.emit(message.data);
            break;
          case 'pointStatus':
            this.pointStatusChanged.emit(message.data);
            break;
        }
      } catch (error) {
        console.error('WebSocket message parse error:', error);
      }
    };
    
    this.ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };
    
    this.ws.onclose = () => {
      console.log('WebSocket disconnected, reconnecting...');
      setTimeout(() => this.connect(), 5000);
    };
  }

  disconnect() {
    if (this.ws) {
      this.ws.close();
      this.ws = null;
    }
  }
}
