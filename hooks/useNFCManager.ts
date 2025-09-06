import { useState, useEffect, useCallback } from 'react';

interface ActivityLogEntry {
  id: number;
  message: string;
  icon: string;
  timestamp: Date;
}

export const useNFCManager = () => {
  const [nfcStatus, setNfcStatus] = useState(false);
  const [autoReminderEnabled, setAutoReminderEnabled] = useState(false);
  const [reminderInterval, setReminderInterval] = useState(45);
  const [lastActivity, setLastActivity] = useState(Date.now());
  const [showNotification, setShowNotification] = useState(false);
  const [isAnimating, setIsAnimating] = useState(false);
  const [activityLog, setActivityLog] = useState<ActivityLogEntry[]>([
    { id: 1, message: 'App initialized.', icon: 'Shield', timestamp: new Date(Date.now() - 300000) },
    { id: 2, message: 'Auto-Reminder is off.', icon: 'Bell', timestamp: new Date(Date.now() - 180000) }
  ]);

  const addLogEntry = useCallback((message: string, icon: string) => {
    setActivityLog(prevLog => [
      { id: Date.now(), message, icon, timestamp: new Date() },
      ...prevLog
    ].slice(0, 10));
  }, []);

  // Simulate NFC activity when enabled
  useEffect(() => {
    let activityInterval: NodeJS.Timeout;
    if (nfcStatus) {
      activityInterval = setInterval(() => {
        if (Math.random() > 0.85) {
          setLastActivity(Date.now());
          addLogEntry('NFC activity detected.', 'Nfc');
        }
      }, 8000);
    }
    return () => clearInterval(activityInterval);
  }, [nfcStatus, addLogEntry]);

  // Check for reminder notifications
  useEffect(() => {
    if (!autoReminderEnabled || !nfcStatus) return;
    
    const checkTimer = setInterval(() => {
      if ((Date.now() - lastActivity) / 1000 >= reminderInterval) {
        setShowNotification(true);
      }
    }, 1000);
    
    return () => clearInterval(checkTimer);
  }, [autoReminderEnabled, nfcStatus, lastActivity, reminderInterval]);

  const toggleNFC = useCallback(() => {
    setIsAnimating(true);
    setTimeout(() => {
      setNfcStatus(prev => {
        const newStatus = !prev;
        addLogEntry(`NFC turned ${newStatus ? 'ON' : 'OFF'}.`, 'Power');
        if (newStatus) setLastActivity(Date.now());
        setShowNotification(false);
        setIsAnimating(false);
        return newStatus;
      });
    }, 400);
  }, [addLogEntry]);

  const toggleAutoReminder = useCallback(() => {
    setAutoReminderEnabled(prev => {
      const newStatus = !prev;
      addLogEntry(`Auto-Reminder turned ${newStatus ? 'ON' : 'OFF'}.`, 'Bell');
      setShowNotification(false);
      return newStatus;
    });
  }, [addLogEntry]);

  const dismissNotification = useCallback(() => {
    setShowNotification(false);
    setLastActivity(Date.now());
    addLogEntry('Security notification dismissed.', 'Shield');
  }, [addLogEntry]);

  const formatTime = (seconds: number): string => {
    if (seconds < 60) return `${Math.floor(seconds)}s`;
    return `${Math.floor(seconds / 60)}m ${Math.floor(seconds % 60)}s`;
  };

  const formatRelativeTime = (date: Date): string => {
    const seconds = Math.round((new Date().getTime() - date.getTime()) / 1000);
    if (seconds < 60) return 'now';
    if (seconds < 3600) return `${Math.round(seconds / 60)}m ago`;
    return `${Math.round(seconds / 3600)}h ago`;
  };

  return {
    nfcStatus,
    autoReminderEnabled,
    reminderInterval,
    setReminderInterval,
    lastActivity,
    showNotification,
    isAnimating,
    activityLog,
    toggleNFC,
    toggleAutoReminder,
    dismissNotification,
    formatTime,
    formatRelativeTime,
    addLogEntry,
  };
};