
import { Injectable, EventEmitter } from '@angular/core';
import { SensorData, AlarmRecord } from './api.service';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private ws: WebSocket | null = null;
  private shouldReconnect = false;
  private reconnectTimerId: ReturnType<typeof setTimeout> | null = null;
  
  sensorDataReceived = new EventEmitter<SensorData>();
  alarmReceived = new EventEmitter<AlarmRecord>();
  pointStatusChanged = new EventEmitter<{ pointCode: string; status: string }>();

  connect() {
    if (this.ws) {
      if (this.ws.readyState === WebSocket.OPEN) {
        return;
      }
      this.cleanup();
    }
    
    this.shouldReconnect = true;
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
      console.log('WebSocket disconnected');
      if (this.shouldReconnect) {
        this.reconnectTimerId = setTimeout(() => this.connect(), 5000);
      }
    };
  }

  disconnect() {
    this.shouldReconnect = false;
    if (this.reconnectTimerId) {
      clearTimeout(this.reconnectTimerId);
      this.reconnectTimerId = null;
    }
    this.cleanup();
  }

  private cleanup() {
    if (this.ws) {
      this.ws.onopen = null;
      this.ws.onmessage = null;
      this.ws.onerror = null;
      this.ws.onclose = null;
      this.ws.close();
      this.ws = null;
    }
  }
}
