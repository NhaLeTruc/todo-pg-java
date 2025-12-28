import { useCallback, useEffect, useState } from 'react';

import { Client, IMessage } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

import { Notification } from '../types/notification';

const WS_URL = process.env.REACT_APP_WS_URL || 'http://localhost:8080/ws/notifications';

export const useNotificationWebSocket = (userId: number | null) => {
  const [_client, setClient] = useState<Client | null>(null);
  const [isConnected, setIsConnected] = useState(false);
  const [newNotification, setNewNotification] = useState<Notification | null>(null);

  const connect = useCallback(() => {
    if (!userId) return;

    const stompClient = new Client({
      webSocketFactory: () => new SockJS(WS_URL),
      debug: (_str) => {
        // Debug logging disabled in production
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    stompClient.onConnect = () => {
      setIsConnected(true);

      // Subscribe to user-specific notification queue
      stompClient.subscribe(`/user/queue/notifications`, (message: IMessage) => {
        try {
          const notification: Notification = JSON.parse(message.body);
          setNewNotification(notification);
        } catch (_error) {
          // Failed to parse notification
        }
      });
    };

    stompClient.onDisconnect = () => {
      setIsConnected(false);
    };

    stompClient.onStompError = (_frame) => {
      setIsConnected(false);
    };

    stompClient.activate();
    setClient(stompClient);

    return () => {
      if (stompClient.active) {
        stompClient.deactivate();
      }
    };
  }, [userId]);

  useEffect(() => {
    const cleanup = connect();
    return () => {
      if (cleanup) cleanup();
    };
  }, [connect]);

  const clearNotification = useCallback(() => {
    setNewNotification(null);
  }, []);

  return {
    isConnected,
    newNotification,
    clearNotification,
  };
};
