
import { Component, OnInit } from '@angular/core';
import { WebSocketService } from './services/websocket.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent implements OnInit {
  title = '矿山安全巡检系统';
  currentTab = 'dashboard';

  constructor(private wsService: WebSocketService) {}

  ngOnInit() {
    this.wsService.connect();
  }

  switchTab(tab: string) {
    this.currentTab = tab;
  }
}
