/**
 * Crash Reporting and Error Monitoring Utility
 * Provides local error logging and crash reporting for NFC Manager
 */

interface CrashReport {
  id: string;
  timestamp: number;
  error: string;
  stack?: string;
  context: string;
  deviceInfo: {
    platform: string;
    version: string;
    model: string;
  };
  appInfo: {
    version: string;
    buildNumber: string;
  };
}

interface ErrorContext {
  component: string;
  action: string;
  userId?: string;
  additionalInfo?: Record<string, any>;
}

class CrashReporter {
  private crashes: CrashReport[] = [];
  private maxCrashReports = 50;
  
  constructor() {
    // Set up global error handlers
    this.setupGlobalErrorHandlers();
  }

  private setupGlobalErrorHandlers() {
    // Handle unhandled promise rejections
    if (typeof global !== 'undefined') {
      const originalHandler = global.onunhandledrejection;
      global.onunhandledrejection = (event: any) => {
        this.reportCrash(
          new Error(`Unhandled Promise Rejection: ${event.reason}`),
          { component: 'Global', action: 'unhandledRejection' }
        );
        
        if (originalHandler) {
          originalHandler(event);
        }
      };
    }
  }

  /**
   * Report a crash or error
   */
  reportCrash(error: Error, context: ErrorContext) {
    try {
      const crashReport: CrashReport = {
        id: this.generateId(),
        timestamp: Date.now(),
        error: error.message || 'Unknown error',
        stack: error.stack,
        context: `${context.component}:${context.action}`,
        deviceInfo: this.getDeviceInfo(),
        appInfo: this.getAppInfo(),
      };

      // Add to local storage
      this.crashes.unshift(crashReport);
      
      // Keep only recent crashes
      if (this.crashes.length > this.maxCrashReports) {
        this.crashes = this.crashes.slice(0, this.maxCrashReports);
      }

      // Log to console in development
      if (__DEV__) {
        console.group('🚨 Crash Report');
        console.error('Error:', error.message);
        console.error('Context:', context);
        console.error('Stack:', error.stack);
        console.groupEnd();
      }

      // Store locally (no external reporting for privacy)
      this.persistCrashes();
      
    } catch (reportingError) {
      console.error('Failed to report crash:', reportingError);
    }
  }

  /**
   * Report a non-fatal error
   */
  reportError(error: Error | string, context: ErrorContext) {
    const errorObj = typeof error === 'string' ? new Error(error) : error;
    
    // For non-fatal errors, we just log them
    if (__DEV__) {
      console.warn('⚠️ Non-fatal error:', errorObj.message, 'Context:', context);
    }
    
    // Still store for debugging
    this.reportCrash(errorObj, { ...context, action: `non-fatal:${context.action}` });
  }

  /**
   * Get recent crash reports
   */
  getCrashReports(): CrashReport[] {
    return [...this.crashes];
  }

  /**
   * Clear all crash reports
   */
  clearCrashReports() {
    this.crashes = [];
    this.persistCrashes();
  }

  /**
   * Get crash statistics
   */
  getCrashStats() {
    const now = Date.now();
    const last24h = this.crashes.filter(crash => now - crash.timestamp < 24 * 60 * 60 * 1000);
    const last7days = this.crashes.filter(crash => now - crash.timestamp < 7 * 24 * 60 * 60 * 1000);

    return {
      total: this.crashes.length,
      last24h: last24h.length,
      last7days: last7days.length,
      mostCommonErrors: this.getMostCommonErrors(),
    };
  }

  private getMostCommonErrors() {
    const errorCounts: Record<string, number> = {};
    
    this.crashes.forEach(crash => {
      const errorKey = crash.error.substring(0, 100); // First 100 chars
      errorCounts[errorKey] = (errorCounts[errorKey] || 0) + 1;
    });

    return Object.entries(errorCounts)
      .sort(([, a], [, b]) => b - a)
      .slice(0, 5)
      .map(([error, count]) => ({ error, count }));
  }

  private generateId(): string {
    return Date.now().toString(36) + Math.random().toString(36).substr(2);
  }

  private getDeviceInfo() {
    return {
      platform: 'android', // Since this is Android-only
      version: 'unknown',
      model: 'unknown',
    };
  }

  private getAppInfo() {
    return {
      version: '1.0.0',
      buildNumber: '1',
    };
  }

  private persistCrashes() {
    // In a real app, you might use AsyncStorage or similar
    // For now, we keep them in memory only
    try {
      if (typeof localStorage !== 'undefined') {
        localStorage.setItem('nfc_manager_crashes', JSON.stringify(this.crashes));
      }
    } catch (error) {
      console.warn('Failed to persist crashes:', error);
    }
  }

  private loadPersistedCrashes() {
    try {
      if (typeof localStorage !== 'undefined') {
        const stored = localStorage.getItem('nfc_manager_crashes');
        if (stored) {
          this.crashes = JSON.parse(stored);
        }
      }
    } catch (error) {
      console.warn('Failed to load persisted crashes:', error);
    }
  }
}

// Export singleton instance
export const crashReporter = new CrashReporter();

// Export helper functions
export const reportCrash = (error: Error, context: ErrorContext) => {
  crashReporter.reportCrash(error, context);
};

export const reportError = (error: Error | string, context: ErrorContext) => {
  crashReporter.reportError(error, context);
};

// Export types
export type { CrashReport, ErrorContext };
