import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

const WS_URL = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws/tasks';

export interface TaskUpdate {
  taskId: number;
  action: 'CREATED' | 'UPDATED' | 'DELETED' | 'COMPLETED' | 'SHARED';
  userId: number;
  description?: string;
  isCompleted?: boolean;
  priority?: string;
  dueDate?: string;
  timestamp: string;
}

export type TaskUpdateCallback = (update: TaskUpdate) => void;

export class WebSocketClient {
  private client: Client | null = null;
  private callbacks: Set<TaskUpdateCallback> = new Set();
  private isConnected = false;

  constructor() {
    this.client = null;
  }

  connect(token: string, _userId: number): Promise<void> {
    return new Promise((resolve, reject) => {

      const stompClient = new Client({
        webSocketFactory: () => new SockJS(WS_URL),
        connectHeaders: {
          Authorization: `Bearer ${token}`,
        },
        debug: (str) => {
          if (process.env.NODE_ENV === 'development') {
            // eslint-disable-next-line no-console
            console.log(str);
          }
        },
        reconnectDelay: 5000,
        heartbeatIncoming: 4000,
        heartbeatOutgoing: 4000,
      });

      stompClient.onConnect = () => {
        this.isConnected = true;

        // Subscribe to user-specific task update queue
        stompClient.subscribe(`/user/queue/task-updates`, (message: IMessage) => {
          try {
            const update: TaskUpdate = JSON.parse(message.body);
            this.notifyCallbacks(update);
          } catch (error) {
            // eslint-disable-next-line no-console
            console.error('Failed to parse task update:', error);
          }
        });

        resolve();
      };

      stompClient.onDisconnect = () => {
        this.isConnected = false;
      };

      stompClient.onStompError = (frame) => {
        // eslint-disable-next-line no-console
        console.error('STOMP error:', frame);
        this.isConnected = false;
        reject(new Error('WebSocket connection failed'));
      };

      stompClient.activate();
      this.client = stompClient;
    });
  }

  disconnect(): void {
    if (this.client && this.client.active) {
      this.client.deactivate();
    }
    this.isConnected = false;
    this.callbacks.clear();
  }

  subscribe(callback: TaskUpdateCallback): () => void {
    this.callbacks.add(callback);

    // Return unsubscribe function
    return () => {
      this.callbacks.delete(callback);
    };
  }

  private notifyCallbacks(update: TaskUpdate): void {
    this.callbacks.forEach((callback) => {
      try {
        callback(update);
      } catch (error) {
        // eslint-disable-next-line no-console
        console.error('Error in WebSocket callback:', error);
      }
    });
  }

  getConnectionStatus(): boolean {
    return this.isConnected;
  }
}

// Singleton instance
let webSocketClient: WebSocketClient | null = null;

export const getWebSocketClient = (): WebSocketClient => {
  if (!webSocketClient) {
    webSocketClient = new WebSocketClient();
  }
  return webSocketClient;
};
