import { Injectable, EventEmitter } from '@angular/core';
import { SensorData, AlarmRecord } from './api.service';

@Injectable({ providedIn: 'root' })
export class WebSocketService {
  private ws: WebSocket | null = null;
  private shouldReconnect = false;
  private reconnectTimerId: ReturnType<typeof setTimeout> | null = null;
  private receivedSeqs: Set<number> = new Set();
  private lastReceivedSeq = 0;
  private readonly MAX_SEQ_HISTORY = 100;
  
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
    this.loadLastSeqFromStorage();
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
    const host = window.location.host;
    this.ws = new WebSocket(`${protocol}//${host}/ws/safety?lastSeq=${this.lastReceivedSeq}`);
    
    this.ws.onopen = () => {
      console.log('WebSocket connected');
    };
    
    this.ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data);
        if (message.seq !== undefined && this.isDuplicate(message.seq)) {
          return;
        }
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

  private isDuplicate(seq: number): boolean {
    if (seq <= this.lastReceivedSeq) {
      return true;
    }
    
    if (this.receivedSeqs.has(seq)) {
      return true;
    }
    
    this.receivedSeqs.add(seq);
    this.lastReceivedSeq = seq;
    this.saveLastSeqToStorage(seq);
    
    if (this.receivedSeqs.size > this.MAX_SEQ_HISTORY) {
      const minSeq = Array.from(this.receivedSeqs).reduce((a, b) => Math.min(a, b), Infinity);
      this.receivedSeqs.delete(minSeq);
    }
    
    return false;
  }

  private saveLastSeqToStorage(seq: number): void {
    try {
      const maxSeq = Math.max(seq, this.getMaxStoredSeq());
      localStorage.setItem('ws_last_seq', maxSeq.toString());
    } catch (e) {
      console.warn('Failed to save last seq to localStorage:', e);
    }
  }

  private getMaxStoredSeq(): number {
    try {
      const stored = localStorage.getItem('ws_last_seq');
      return stored ? parseInt(stored, 10) : 0;
    } catch (e) {
      return 0;
    }
  }

  private loadLastSeqFromStorage(): void {
    const storedSeq = this.getMaxStoredSeq();
    if (storedSeq > 0) {
      this.lastReceivedSeq = storedSeq;
    }
  }
}