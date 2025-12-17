# Frontend Integration Guide - Notification System

## Overview

Hướng dẫn tích hợp hệ thống thông báo real-time cho Frontend (React/Next.js).

---

## 1. Installation

### Install Dependencies

```bash
npm install @stomp/stompjs sockjs-client
```

---

## 2. WebSocket Connection Setup

### Create NotificationService.js

```javascript
import { Client } from "@stomp/stompjs";
import SockJS from "sockjs-client";

class NotificationService {
  constructor() {
    this.client = null;
    this.isConnected = false;
    this.subscriptions = new Map();
  }

  /**
   * Connect to WebSocket server
   * @param {string} token - JWT access token
   * @param {number} userId - User ID to subscribe
   */
  connect(token, userId) {
    if (this.isConnected) {
      console.log("WebSocket already connected");
      return;
    }

    // Create SockJS instance
    const socket = new SockJS(
      process.env.NEXT_PUBLIC_WS_URL || "http://localhost:8080/ws"
    );

    // Create STOMP client
    this.client = new Client({
      webSocketFactory: () => socket,
      connectHeaders: {
        Authorization: `Bearer ${token}`,
      },
      debug: (str) => {
        console.log("STOMP Debug:", str);
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    // On successful connection
    this.client.onConnect = (frame) => {
      console.log("WebSocket Connected:", frame);
      this.isConnected = true;

      // Subscribe to personal notifications
      this.subscribeToNotifications(userId);
    };

    // On connection error
    this.client.onStompError = (frame) => {
      console.error("STOMP Error:", frame.headers["message"]);
      console.error("Details:", frame.body);
    };

    // On disconnect
    this.client.onDisconnect = () => {
      console.log("WebSocket Disconnected");
      this.isConnected = false;
    };

    // Activate connection
    this.client.activate();
  }

  /**
   * Subscribe to user's notification channel
   * @param {number} userId - User ID
   */
  subscribeToNotifications(userId) {
    if (!this.client || !this.isConnected) {
      console.error("WebSocket not connected");
      return;
    }

    const destination = `/topic/notifications/${userId}`;

    const subscription = this.client.subscribe(destination, (message) => {
      const notification = JSON.parse(message.body);
      console.log("New notification received:", notification);

      // Trigger custom event
      window.dispatchEvent(
        new CustomEvent("notification:received", {
          detail: notification,
        })
      );
    });

    this.subscriptions.set(userId, subscription);
    console.log(`Subscribed to ${destination}`);
  }

  /**
   * Disconnect from WebSocket
   */
  disconnect() {
    if (this.client) {
      // Unsubscribe all
      this.subscriptions.forEach((subscription) => {
        subscription.unsubscribe();
      });
      this.subscriptions.clear();

      // Deactivate client
      this.client.deactivate();
      this.isConnected = false;
      console.log("WebSocket disconnected");
    }
  }

  /**
   * Check connection status
   */
  getConnectionStatus() {
    return this.isConnected;
  }
}

// Export singleton instance
export const notificationService = new NotificationService();
```

---

## 3. REST API Integration

### Create NotificationAPI.js

```javascript
import axios from "axios";

const BASE_URL = process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";

export const notificationAPI = {
  /**
   * Get user notifications (paginated)
   */
  getNotifications: async (page = 0, size = 20) => {
    const response = await axios.get(`${BASE_URL}/api/v1/notifications`, {
      params: { page, size },
      headers: {
        Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
      },
    });
    return response.data.data;
  },

  /**
   * Get unread notification count
   */
  getUnreadCount: async () => {
    const response = await axios.get(
      `${BASE_URL}/api/v1/notifications/unread-count`,
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
        },
      }
    );
    return response.data.data;
  },

  /**
   * Mark notification as read
   */
  markAsRead: async (notificationId) => {
    const response = await axios.patch(
      `${BASE_URL}/api/v1/notifications/${notificationId}/read`,
      null,
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
        },
      }
    );
    return response.data;
  },

  /**
   * Mark all notifications as read
   */
  markAllAsRead: async () => {
    const response = await axios.patch(
      `${BASE_URL}/api/v1/notifications/read-all`,
      null,
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
        },
      }
    );
    return response.data;
  },

  /**
   * Delete notification
   */
  deleteNotification: async (notificationId) => {
    const response = await axios.delete(
      `${BASE_URL}/api/v1/notifications/${notificationId}`,
      {
        headers: {
          Authorization: `Bearer ${localStorage.getItem("accessToken")}`,
        },
      }
    );
    return response.data;
  },
};
```

---

## 4. React Context (Notification State Management)

### Create NotificationContext.jsx

```javascript
import React, { createContext, useContext, useState, useEffect } from "react";
import { notificationService } from "../services/NotificationService";
import { notificationAPI } from "../api/NotificationAPI";
import { useAuth } from "./AuthContext"; // Your auth context

const NotificationContext = createContext();

export const useNotifications = () => {
  const context = useContext(NotificationContext);
  if (!context) {
    throw new Error(
      "useNotifications must be used within NotificationProvider"
    );
  }
  return context;
};

export const NotificationProvider = ({ children }) => {
  const { user, token } = useAuth(); // Get user and token from auth context
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  // Connect to WebSocket when user logs in
  useEffect(() => {
    if (user && token) {
      notificationService.connect(token, user.userId);

      // Listen for new notifications
      const handleNewNotification = (event) => {
        const notification = event.detail;
        console.log("New notification:", notification);

        // Add to notifications list
        setNotifications((prev) => [notification, ...prev]);

        // Update unread count
        setUnreadCount((prev) => prev + 1);

        // Show toast notification
        showToast(notification);
      };

      window.addEventListener("notification:received", handleNewNotification);

      // Load initial notifications
      loadNotifications();
      loadUnreadCount();

      return () => {
        window.removeEventListener(
          "notification:received",
          handleNewNotification
        );
        notificationService.disconnect();
      };
    }
  }, [user, token]);

  /**
   * Load notifications from API
   */
  const loadNotifications = async (page = 0, size = 20) => {
    setIsLoading(true);
    try {
      const data = await notificationAPI.getNotifications(page, size);
      setNotifications(data.content);
    } catch (error) {
      console.error("Error loading notifications:", error);
    } finally {
      setIsLoading(false);
    }
  };

  /**
   * Load unread count
   */
  const loadUnreadCount = async () => {
    try {
      const count = await notificationAPI.getUnreadCount();
      setUnreadCount(count);
    } catch (error) {
      console.error("Error loading unread count:", error);
    }
  };

  /**
   * Mark notification as read
   */
  const markAsRead = async (notificationId) => {
    try {
      await notificationAPI.markAsRead(notificationId);

      // Update local state
      setNotifications((prev) =>
        prev.map((n) =>
          n.notificationId === notificationId ? { ...n, isRead: true } : n
        )
      );

      // Decrease unread count
      setUnreadCount((prev) => Math.max(0, prev - 1));
    } catch (error) {
      console.error("Error marking notification as read:", error);
    }
  };

  /**
   * Mark all as read
   */
  const markAllAsRead = async () => {
    try {
      await notificationAPI.markAllAsRead();

      // Update local state
      setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })));

      setUnreadCount(0);
    } catch (error) {
      console.error("Error marking all as read:", error);
    }
  };

  /**
   * Delete notification
   */
  const deleteNotification = async (notificationId) => {
    try {
      await notificationAPI.deleteNotification(notificationId);

      // Remove from local state
      setNotifications((prev) =>
        prev.filter((n) => n.notificationId !== notificationId)
      );

      // Update unread count if notification was unread
      const notification = notifications.find(
        (n) => n.notificationId === notificationId
      );
      if (notification && !notification.isRead) {
        setUnreadCount((prev) => Math.max(0, prev - 1));
      }
    } catch (error) {
      console.error("Error deleting notification:", error);
    }
  };

  /**
   * Show toast notification
   */
  const showToast = (notification) => {
    // Implement your toast/snackbar here
    console.log("Show toast:", notification.title, notification.message);
    // Example: toast.success(notification.message);
  };

  const value = {
    notifications,
    unreadCount,
    isLoading,
    loadNotifications,
    markAsRead,
    markAllAsRead,
    deleteNotification,
    refreshUnreadCount: loadUnreadCount,
  };

  return (
    <NotificationContext.Provider value={value}>
      {children}
    </NotificationContext.Provider>
  );
};
```

---

## 5. UI Components

### NotificationBell Component

```jsx
import React, { useState } from "react";
import { useNotifications } from "../contexts/NotificationContext";

export const NotificationBell = () => {
  const { unreadCount, notifications, markAsRead, markAllAsRead } =
    useNotifications();
  const [isOpen, setIsOpen] = useState(false);

  return (
    <div className="notification-bell">
      {/* Bell Icon */}
      <button onClick={() => setIsOpen(!isOpen)} className="bell-button">
        <BellIcon />
        {unreadCount > 0 && (
          <span className="badge">
            {unreadCount > 99 ? "99+" : unreadCount}
          </span>
        )}
      </button>

      {/* Dropdown */}
      {isOpen && (
        <div className="notification-dropdown">
          <div className="header">
            <h3>Thông báo</h3>
            <button onClick={markAllAsRead}>Đánh dấu tất cả đã đọc</button>
          </div>

          <div className="notification-list">
            {notifications.length === 0 ? (
              <p className="empty">Không có thông báo</p>
            ) : (
              notifications.map((notification) => (
                <NotificationItem
                  key={notification.notificationId}
                  notification={notification}
                  onMarkAsRead={markAsRead}
                />
              ))
            )}
          </div>
        </div>
      )}
    </div>
  );
};

const NotificationItem = ({ notification, onMarkAsRead }) => {
  return (
    <div
      className={`notification-item ${!notification.isRead ? "unread" : ""}`}
      onClick={() => onMarkAsRead(notification.notificationId)}
    >
      <div className="icon">{getIcon(notification.type)}</div>
      <div className="content">
        <h4>{notification.title}</h4>
        <p>{notification.message}</p>
        <span className="time">{formatTime(notification.createdAt)}</span>
      </div>
    </div>
  );
};

const getIcon = (type) => {
  switch (type) {
    case "APPOINTMENT_CREATED":
      return "📅";
    case "APPOINTMENT_REMINDER":
      return "⏰";
    case "TREATMENT_PLAN_APPROVED":
      return "✅";
    case "PAYMENT_RECEIVED":
      return "💰";
    default:
      return "🔔";
  }
};

const formatTime = (timestamp) => {
  const date = new Date(timestamp);
  const now = new Date();
  const diff = (now - date) / 1000; // seconds

  if (diff < 60) return "Vừa xong";
  if (diff < 3600) return `${Math.floor(diff / 60)} phút trước`;
  if (diff < 86400) return `${Math.floor(diff / 3600)} giờ trước`;
  return `${Math.floor(diff / 86400)} ngày trước`;
};
```

---

## 6. Environment Variables

### .env.local

```env
NEXT_PUBLIC_API_URL=http://localhost:8080
NEXT_PUBLIC_WS_URL=http://localhost:8080/ws

# Production
# NEXT_PUBLIC_API_URL=https://api.dentalclinic.com
# NEXT_PUBLIC_WS_URL=wss://api.dentalclinic.com/ws
```

---

## 7. Usage in App

### \_app.js (Next.js) or App.jsx (React)

```jsx
import { NotificationProvider } from "../contexts/NotificationContext";
import { AuthProvider } from "../contexts/AuthContext";

function MyApp({ Component, pageProps }) {
  return (
    <AuthProvider>
      <NotificationProvider>
        <Component {...pageProps} />
      </NotificationProvider>
    </AuthProvider>
  );
}

export default MyApp;
```

### Layout.jsx (with NotificationBell)

```jsx
import { NotificationBell } from "../components/NotificationBell";

export const Layout = ({ children }) => {
  return (
    <div>
      <header>
        <nav>
          <Logo />
          <Menu />
          <NotificationBell />
          <UserMenu />
        </nav>
      </header>
      <main>{children}</main>
    </div>
  );
};
```

---

## 8. Testing WebSocket Connection

### Test in Browser Console

```javascript
// After login
console.log("WebSocket connected:", notificationService.getConnectionStatus());

// Trigger test notification (create an appointment via API)
// Check console for: "New notification received: {...}"
```

---

## 9. Troubleshooting

### WebSocket không kết nối được

1. Kiểm tra JWT token có hợp lệ không
2. Kiểm tra CORS settings trên backend
3. Kiểm tra WebSocket URL đúng chưa
4. Check browser console cho STOMP errors

### Không nhận được real-time notification

1. Verify WebSocket connection status
2. Check subscription destination: `/topic/notifications/{userId}`
3. Verify userId đúng với user hiện tại
4. Test bằng cách tạo appointment mới

### Unread count không đúng

1. Call `refreshUnreadCount()` sau khi mark as read
2. Check API response từ `/api/v1/notifications/unread-count`

---

## 10. Best Practices

1. **Auto-reconnect**: STOMP client tự động reconnect khi mất kết nối
2. **Error handling**: Luôn có try-catch cho API calls
3. **Loading states**: Show loading spinner khi fetch notifications
4. **Optimistic updates**: Update UI trước, rồi call API
5. **Toast notifications**: Show toast cho real-time notifications
6. **Cleanup**: Disconnect WebSocket khi user logout
7. **Pagination**: Load more notifications khi scroll down
8. **Badge**: Hiển thị số lượng unread trong badge

---

## 11. Future Enhancements

1. **Sound notification**: Play sound khi có notification mới
2. **Desktop notifications**: Browser notification API
3. **Notification filtering**: Filter theo type
4. **Search notifications**: Search trong history
5. **Notification settings**: User tự config loại notification muốn nhận
