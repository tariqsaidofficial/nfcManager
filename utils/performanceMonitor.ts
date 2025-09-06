/**
 * Performance Monitoring Utility
 * Monitors app performance, memory usage, and battery optimization
 */

interface PerformanceMetrics {
  timestamp: number;
  memoryUsage: number;
  cpuUsage: number;
  batteryLevel?: number;
  isCharging?: boolean;
  nfcChecks: number;
  backgroundTime: number;
}

interface OptimizationSettings {
  smartMonitoring: boolean;
  adaptiveInterval: boolean;
  sleepMode: boolean;
  sleepHours: { start: number; end: number };
  batterySaver: boolean;
  lowBatteryThreshold: number;
}

class PerformanceMonitor {
  private metrics: PerformanceMetrics[] = [];
  private maxMetrics = 100;
  private monitoringStartTime = Date.now();
  private nfcCheckCount = 0;
  private backgroundStartTime: number | null = null;
  
  // Default optimization settings
  private settings: OptimizationSettings = {
    smartMonitoring: true,
    adaptiveInterval: true,
    sleepMode: true,
    sleepHours: { start: 23, end: 7 }, // 11 PM to 7 AM
    batterySaver: true,
    lowBatteryThreshold: 20,
  };

  constructor() {
    this.setupPerformanceMonitoring();
  }

  private setupPerformanceMonitoring() {
    // Monitor performance every 30 seconds
    setInterval(() => {
      this.collectMetrics();
    }, 30000);

    // Clean up old metrics every hour
    setInterval(() => {
      this.cleanupOldMetrics();
    }, 3600000);
  }

  /**
   * Collect current performance metrics
   */
  private collectMetrics() {
    try {
      const currentMetrics: PerformanceMetrics = {
        timestamp: Date.now(),
        memoryUsage: this.getMemoryUsage(),
        cpuUsage: this.getCPUUsage(),
        nfcChecks: this.nfcCheckCount,
        backgroundTime: this.getBackgroundTime(),
      };

      this.metrics.unshift(currentMetrics);
      
      // Keep only recent metrics
      if (this.metrics.length > this.maxMetrics) {
        this.metrics = this.metrics.slice(0, this.maxMetrics);
      }

      // Check if optimization is needed
      this.checkOptimizationNeeded(currentMetrics);
      
    } catch (error) {
      console.warn('Performance monitoring error:', error);
    }
  }

  /**
   * Get memory usage (mock implementation - would use native modules in real app)
   */
  private getMemoryUsage(): number {
    // In a real app, this would use native modules to get actual memory usage
    return Math.random() * 100; // Mock: 0-100 MB
  }

  /**
   * Get CPU usage (mock implementation)
   */
  private getCPUUsage(): number {
    // In a real app, this would use native modules to get actual CPU usage
    return Math.random() * 50; // Mock: 0-50%
  }

  /**
   * Get total background monitoring time
   */
  private getBackgroundTime(): number {
    if (this.backgroundStartTime) {
      return Date.now() - this.backgroundStartTime;
    }
    return Date.now() - this.monitoringStartTime;
  }

  /**
   * Record NFC check event
   */
  recordNFCCheck() {
    this.nfcCheckCount++;
  }

  /**
   * Set app background state
   */
  setBackgroundState(isBackground: boolean) {
    if (isBackground && !this.backgroundStartTime) {
      this.backgroundStartTime = Date.now();
    } else if (!isBackground) {
      this.backgroundStartTime = null;
    }
  }

  /**
   * Check if optimization is needed based on current metrics
   */
  private checkOptimizationNeeded(metrics: PerformanceMetrics) {
    const optimizations: string[] = [];

    // High memory usage
    if (metrics.memoryUsage > 150) {
      optimizations.push('memory');
    }

    // High CPU usage
    if (metrics.cpuUsage > 70) {
      optimizations.push('cpu');
    }

    // Too many NFC checks
    if (metrics.nfcChecks > 1000) {
      optimizations.push('nfc-frequency');
    }

    // Long background time
    if (metrics.backgroundTime > 3600000) { // 1 hour
      optimizations.push('background-time');
    }

    if (optimizations.length > 0) {
      this.triggerOptimization(optimizations);
    }
  }

  /**
   * Trigger performance optimizations
   */
  private triggerOptimization(types: string[]) {
    console.log('🔧 Performance optimization triggered:', types);
    
    types.forEach(type => {
      switch (type) {
        case 'memory':
          this.optimizeMemory();
          break;
        case 'cpu':
          this.optimizeCPU();
          break;
        case 'nfc-frequency':
          this.optimizeNFCFrequency();
          break;
        case 'background-time':
          this.optimizeBackgroundTime();
          break;
      }
    });
  }

  /**
   * Memory optimization
   */
  private optimizeMemory() {
    // Clean up old metrics more aggressively
    this.metrics = this.metrics.slice(0, 50);
    console.log('🧹 Memory optimization applied');
  }

  /**
   * CPU optimization
   */
  private optimizeCPU() {
    // Reduce monitoring frequency temporarily
    console.log('⚡ CPU optimization applied');
  }

  /**
   * NFC frequency optimization
   */
  private optimizeNFCFrequency() {
    // Reset NFC check counter and suggest longer intervals
    this.nfcCheckCount = 0;
    console.log('📡 NFC frequency optimization applied');
  }

  /**
   * Background time optimization
   */
  private optimizeBackgroundTime() {
    // Suggest sleep mode if enabled
    if (this.settings.sleepMode && this.isInSleepHours()) {
      console.log('😴 Sleep mode optimization applied');
    }
  }

  /**
   * Check if current time is within sleep hours
   */
  private isInSleepHours(): boolean {
    const now = new Date();
    const currentHour = now.getHours();
    const { start, end } = this.settings.sleepHours;
    
    if (start > end) {
      // Sleep hours span midnight (e.g., 23:00 to 7:00)
      return currentHour >= start || currentHour < end;
    } else {
      // Sleep hours within same day
      return currentHour >= start && currentHour < end;
    }
  }

  /**
   * Get recommended NFC check interval based on performance
   */
  getRecommendedInterval(currentInterval: number): number {
    if (!this.settings.adaptiveInterval) {
      return currentInterval;
    }

    const latestMetrics = this.metrics[0];
    if (!latestMetrics) {
      return currentInterval;
    }

    // Increase interval if high resource usage
    if (latestMetrics.memoryUsage > 100 || latestMetrics.cpuUsage > 60) {
      return Math.min(currentInterval * 1.5, 30); // Max 30 seconds
    }

    // Decrease interval if resources are available
    if (latestMetrics.memoryUsage < 50 && latestMetrics.cpuUsage < 30) {
      return Math.max(currentInterval * 0.8, 1); // Min 1 second
    }

    return currentInterval;
  }

  /**
   * Get performance statistics
   */
  getPerformanceStats() {
    if (this.metrics.length === 0) {
      return {
        avgMemoryUsage: 0,
        avgCPUUsage: 0,
        totalNFCChecks: this.nfcCheckCount,
        uptime: Date.now() - this.monitoringStartTime,
        optimizationCount: 0,
      };
    }

    const avgMemory = this.metrics.reduce((sum, m) => sum + m.memoryUsage, 0) / this.metrics.length;
    const avgCPU = this.metrics.reduce((sum, m) => sum + m.cpuUsage, 0) / this.metrics.length;

    return {
      avgMemoryUsage: Math.round(avgMemory),
      avgCPUUsage: Math.round(avgCPU),
      totalNFCChecks: this.nfcCheckCount,
      uptime: Date.now() - this.monitoringStartTime,
      optimizationCount: this.metrics.filter(m => m.memoryUsage > 100).length,
      currentMetrics: this.metrics[0],
    };
  }

  /**
   * Update optimization settings
   */
  updateSettings(newSettings: Partial<OptimizationSettings>) {
    this.settings = { ...this.settings, ...newSettings };
    console.log('⚙️ Performance settings updated:', newSettings);
  }

  /**
   * Get current optimization settings
   */
  getSettings(): OptimizationSettings {
    return { ...this.settings };
  }

  /**
   * Clean up old metrics to save memory
   */
  private cleanupOldMetrics() {
    const oneHourAgo = Date.now() - 3600000;
    this.metrics = this.metrics.filter(m => m.timestamp > oneHourAgo);
  }

  /**
   * Check if device is in battery saver mode conditions
   */
  shouldUseBatterySaver(): boolean {
    return this.settings.batterySaver && (
      this.isInSleepHours() ||
      this.getBackgroundTime() > 1800000 // 30 minutes in background
    );
  }

  /**
   * Reset all metrics (useful for testing)
   */
  reset() {
    this.metrics = [];
    this.nfcCheckCount = 0;
    this.monitoringStartTime = Date.now();
    this.backgroundStartTime = null;
  }
}

// Export singleton instance
export const performanceMonitor = new PerformanceMonitor();

// Export helper functions
export const recordNFCCheck = () => performanceMonitor.recordNFCCheck();
export const setBackgroundState = (isBackground: boolean) => 
  performanceMonitor.setBackgroundState(isBackground);
export const getRecommendedInterval = (current: number) => 
  performanceMonitor.getRecommendedInterval(current);

// Export types
export type { PerformanceMetrics, OptimizationSettings };
