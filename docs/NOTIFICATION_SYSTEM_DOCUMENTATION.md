# توثيق نظام الإشعارات في NFC Manager

## نظرة عامة

نظام الإشعارات في التطبيق مصمم لإبقاء المستخدم على اطلاع بحالة NFC ومستوى الأمان في الوقت الفعلي. يتكون النظام من عدة مكونات تعمل معاً بشكل متكامل.

## مكونات النظام

### 1. NotificationManager (utils/NotificationManager.kt)
**الدور:** إدارة إنشاء وتحديث وإلغاء الإشعارات

**القنوات (Channels):**
```kotlin
// قناة حالة NFC - أولوية منخفضة
NFC_STATUS_CHANNEL_ID = "nfc_status_channel"
IMPORTANCE_LOW // لا تصدر صوت أو اهتزاز

// قناة تنبيهات الأمان - أولوية عالية
SECURITY_ALERT_CHANNEL_ID = "security_alert_channel"
IMPORTANCE_HIGH // تصدر صوت واهتزاز
```

**الوظائف الرئيسية:**
```kotlin
// إنشاء إشعار حالة NFC
fun createNfcStatusNotification(
    isEnabled: Boolean,
    enabledDuration: Long,
    securityLevel: SecurityLevel
): Notification

// تحديث إشعار الحالة
fun updateNfcStatusNotification(
    isEnabled: Boolean,
    enabledDuration: Long,
    securityLevel: SecurityLevel
)

// إلغاء إشعار الحالة
fun cancelNfcStatusNotification()

// إنشاء إشعار تنبيه أمان
fun createSecurityAlertNotification(
    title: String,
    message: String,
    priority: Int = NotificationCompat.PRIORITY_HIGH
): Notification
```

### 2. NfcMonitoringService (services/NfcMonitoringService.kt)
**الدور:** مراقبة حالة NFC بشكل مستمر في الخلفية

**دورة الحياة:**
```
START → onStartCommand() → startNfcMonitoring() → startPeriodicMonitoring()
  ↓
  Monitor NFC State (every 5 seconds)
  ↓
  updateNfcStatusNotification()
  ↓
STOP → onDestroy() → stopNfcMonitoring()
```

**الوظائف الرئيسية:**
```kotlin
// بدء المراقبة
private fun startNfcMonitoring()

// إيقاف المراقبة
private fun stopNfcMonitoring()

// المراقبة الدورية
private fun startPeriodicMonitoring()

// تحديث الإشعار
private fun updateNfcStatusNotification()

// حساب مستوى الأمان الحالي
private fun calculateCurrentSecurityLevel(): SecurityLevel
```

**الفاصل الزمني للمراقبة:**
```kotlin
MONITORING_CHECK_INTERVAL = 5000L // 5 ثوانٍ
```

### 3. SecurityScoreViewModel (viewmodel/SecurityScoreViewModel.kt)
**الدور:** حساب وإدارة درجة الأمان

**الوظائف:**
```kotlin
// حساب درجة الأمان
fun calculateSecurityScore()

// تحديث درجة الأمان
fun refreshSecurityScore()

// الحصول على درجة الأمان لفترة محددة
fun getSecurityScoreForPeriod(days: Int)
```

### 4. PrivacyScoreCalculator (utils/PrivacyScoreCalculator.kt)
**الدور:** حساب درجة الأمان بناءً على الأحداث

**منطق الحساب:**
```kotlin
fun calculateSecurityScore(events: List<NFCEventEntity>): SecurityScore {
    var score = 100 // البداية من 100
    
    // خصم نقاط بناءً على نوع الحدث:
    - NFC_ENABLED_LONG: -15 نقطة
    - UNKNOWN_TAG: -10 نقاط
    - MULTIPLE_TAGS_RAPID: -20 نقطة
    - SUSPICIOUS_ACTIVITY: -25 نقطة
    - TAG_DISCOVERED: -2 نقاط
    
    return SecurityScore(
        score = maxOf(0, score),
        level = determineSecurityLevel(score),
        violations = violations,
        recommendations = generateRecommendations(violations)
    )
}
```

**مستويات الأمان:**
```kotlin
enum class SecurityLevel {
    EXCELLENT    // 90-100 (أخضر)
    GOOD         // 75-89  (أخضر فاتح)
    MODERATE     // 60-74  (برتقالي)
    POOR         // 40-59  (برتقالي غامق)
    CRITICAL     // 0-39   (أحمر)
    NOT_APPLICABLE // N/A  (رمادي)
}
```

## دورة عمل النظام (Flow)

### 1. عند تشغيل التطبيق

```
MainActivity.onCreate()
  ↓
Check Background Monitoring Setting
  ↓
If Enabled:
  Start NfcMonitoringService
  ↓
  Service creates foreground notification
  ↓
  Start periodic monitoring (every 5 seconds)
```

### 2. عند تفعيل NFC

```
User enables NFC in system settings
  ↓
NfcMonitoringService detects change
  ↓
nfcEnabledStartTime = System.currentTimeMillis()
  ↓
updateNfcStatusNotification()
  ↓
Notification shows:
  - "NFC Enabled"
  - Duration: "Active for X minutes"
  - Security Level: calculated
  - Actions: [Disable NFC] [Security Score]
```

### 3. عند اكتشاف علامة NFC

```
NFC Tag Detected
  ↓
Log event to database
  ↓
Event type determined:
  - TAG_DISCOVERED
  - UNKNOWN_TAG
  - MULTIPLE_TAGS_RAPID
  - SUSPICIOUS_ACTIVITY
  ↓
SecurityScoreViewModel recalculates score
  ↓
updateNfcStatusNotification() with new score
  ↓
If score drops significantly:
  Show security alert notification
```

### 4. عند إيقاف NFC

```
User disables NFC
  ↓
NfcMonitoringService detects change
  ↓
Calculate total enabled duration
  ↓
updateNfcStatusNotification()
  ↓
Notification shows:
  - "NFC Disabled"
  - Security Level: based on history or N/A
  - Actions: [Enable NFC] [Security Score]
```

### 5. عند إيقاف المراقبة

```
User disables background monitoring
  ↓
stopService(NfcMonitoringService)
  ↓
stopNfcMonitoring()
  ↓
cancelNfcStatusNotification()
  ↓
Service stops
```

## أنواع الإشعارات

### 1. إشعار الحالة (Status Notification)
**الخصائص:**
- **النوع:** Foreground Service Notification
- **الأولوية:** LOW
- **الصوت:** قابل للتخصيص
- **الاهتزاز:** قابل للتخصيص
- **الإجراءات:**
  - تفعيل/تعطيل NFC
  - فتح شاشة Security Score
  - فتح التطبيق

**المحتوى:**
```
Title: "NFC Manager"
Content: 
  - If NFC ON: "NFC Enabled - Active for X minutes"
  - If NFC OFF: "NFC Disabled - Tap to enable"
Big Text: Security recommendations
Progress: Security score (0-100)
```

### 2. إشعار التنبيه الأمني (Security Alert)
**الخصائص:**
- **النوع:** Standard Notification
- **الأولوية:** HIGH
- **الصوت:** نعم
- **الاهتزاز:** نعم

**متى يظهر:**
- عند اكتشاف نشاط مشبوه
- عند انخفاض درجة الأمان بشكل كبير
- عند اكتشاف علامات غير معروفة متعددة

**المحتوى:**
```
Title: "Security Alert"
Content: Description of the issue
Actions: [View Details] [Dismiss]
```

### 3. إشعار التذكير (Reminder Notification)
**الخصائص:**
- **النوع:** Standard Notification
- **الأولوية:** DEFAULT
- **التكرار:** حسب الإعدادات (5-300 ثانية)

**متى يظهر:**
- عند تفعيل التذكير التلقائي
- بعد مرور الفترة المحددة مع NFC مفعل

**المحتوى:**
```
Title: "NFC Still Enabled"
Content: "NFC has been enabled for X minutes. Consider disabling it."
Actions: [Disable NFC] [Dismiss]
```

## الإعدادات القابلة للتخصيص

### 1. إعدادات الإشعارات
```kotlin
data class NotificationSettings(
    val enabled: Boolean = true,           // تفعيل/تعطيل الإشعارات
    val soundEnabled: Boolean = true,      // تفعيل/تعطيل الصوت
    val vibrationEnabled: Boolean = true,  // تفعيل/تعطيل الاهتزاز
    val customSoundUri: String? = null,    // نغمة مخصصة
    val notificationStyle: String = "standard" // نمط الإشعار
)
```

### 2. إعدادات المراقبة
```kotlin
data class MonitoringSettings(
    val backgroundMonitoringEnabled: Boolean = false, // المراقبة في الخلفية
    val autoReminderEnabled: Boolean = false,         // التذكير التلقائي
    val reminderIntervalSeconds: Int = 60             // فترة التذكير
)
```

## معالجة الأخطاء

### 1. عدم توفر NFC
```kotlin
if (nfcAdapter == null) {
    // عرض رسالة: "جهازك لا يدعم NFC"
    // إيقاف المراقبة
    // إخفاء الإشعارات
}
```

### 2. NFC معطل
```kotlin
if (!nfcAdapter.isEnabled) {
    // تحديث الإشعار: "NFC معطل"
    // عرض درجة أمان تاريخية أو N/A
    // إيقاف المراقبة النشطة
}
```

### 3. فقدان الصلاحيات
```kotlin
try {
    // محاولة الوصول لـ NFC
} catch (se: SecurityException) {
    // طلب الصلاحيات من المستخدم
    // إيقاف الخدمة مؤقتاً
}
```

### 4. خطأ في الخدمة
```kotlin
try {
    // عمليات الخدمة
} catch (e: Exception) {
    AppLogger.e(TAG, "Service error", e)
    // محاولة إعادة التشغيل
    // إشعار المستخدم بالخطأ
}
```

## الأداء والموارد

### 1. استهلاك البطارية
**التحسينات المطبقة:**
- استخدام `PARTIAL_WAKE_LOCK` فقط عند الحاجة
- فترة مراقبة معقولة (5 ثوانٍ)
- إيقاف المراقبة عند عدم الحاجة
- استخدام `Foreground Service` لضمان عدم القتل

**الاستهلاك المتوقع:**
- مع المراقبة: ~2-3% في الساعة
- بدون المراقبة: ~0%

### 2. استهلاك الذاكرة
**التحسينات المطبقة:**
- تنظيف الموارد في `onDestroy()`
- استخدام `StateFlow` بدلاً من `LiveData`
- عدم الاحتفاظ بمراجع غير ضرورية

**الاستهلاك المتوقع:**
- الخدمة: ~10-15 MB
- الإشعارات: ~2-3 MB

### 3. استهلاك المعالج
**التحسينات المطبقة:**
- عمليات خفيفة في الخلفية
- استخدام Coroutines للعمليات غير المتزامنة
- تجنب العمليات الثقيلة في الـ Main Thread

## الاختبار

### سيناريوهات الاختبار الأساسية

#### 1. تشغيل/إيقاف المراقبة
```
✅ تفعيل المراقبة → يظهر الإشعار
✅ إيقاف المراقبة → يختفي الإشعار
✅ إعادة تشغيل التطبيق → الإشعار يستمر
```

#### 2. تغيير حالة NFC
```
✅ تفعيل NFC → الإشعار يتحدث
✅ إيقاف NFC → الإشعار يتحدث
✅ التبديل السريع → لا تعطل
```

#### 3. اكتشاف العلامات
```
✅ علامة واحدة → تسجيل الحدث
✅ علامات متعددة → تسجيل جميع الأحداث
✅ علامات سريعة → كشف النشاط المشبوه
```

#### 4. حساب الأمان
```
✅ NFC معطل بدون بيانات → N/A
✅ NFC معطل مع بيانات → درجة تاريخية
✅ NFC مفعل → درجة محسوبة
✅ أحداث مشبوهة → انخفاض الدرجة
```

#### 5. الإشعارات المخصصة
```
✅ اختيار نغمة → تحديث النغمة
✅ تعطيل الصوت → لا صوت
✅ تعطيل الاهتزاز → لا اهتزاز
```

### اختبارات الضغط

#### 1. اكتشاف علامات متعددة
```
Test: قراءة 100 علامة في دقيقة واحدة
Expected: 
  - جميع الأحداث مسجلة
  - لا تعطل في التطبيق
  - الإشعار يتحدث بشكل صحيح
```

#### 2. تشغيل طويل
```
Test: المراقبة لمدة 24 ساعة
Expected:
  - الخدمة تستمر في العمل
  - لا تسرب في الذاكرة
  - استهلاك بطارية معقول
```

#### 3. إعادة تشغيل الجهاز
```
Test: إعادة تشغيل الجهاز مع المراقبة مفعلة
Expected:
  - الخدمة تبدأ تلقائياً (إذا كانت الإعدادات تسمح)
  - الإشعار يظهر بشكل صحيح
```

## المشاكل المعروفة والحلول

### 1. الإشعار يختفي على بعض الأجهزة
**السبب:** بعض الشركات المصنعة تقتل الخدمات في الخلفية

**الحل:**
```kotlin
// استخدام Foreground Service
startForeground(NOTIFICATION_ID, notification)

// طلب استثناء من Battery Optimization
if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
    val intent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
    intent.data = Uri.parse("package:$packageName")
    startActivity(intent)
}
```

### 2. النغمة المخصصة لا تعمل بعد إعادة التشغيل
**السبب:** فقدان صلاحية الوصول للملف

**الحل:**
```kotlin
// محاولة الحصول على صلاحية دائمة
try {
    context.contentResolver.takePersistableUriPermission(
        uri,
        Intent.FLAG_GRANT_READ_URI_PERMISSION
    )
} catch (e: SecurityException) {
    // استخدام النغمة الافتراضية كـ fallback
}
```

### 3. تأخير في تحديث الإشعار
**السبب:** فترة المراقبة 5 ثوانٍ

**الحل:**
```kotlin
// تقليل الفترة للحالات الحرجة
if (suspiciousActivityDetected) {
    MONITORING_CHECK_INTERVAL = 1000L // ثانية واحدة
}
```

## أفضل الممارسات

### 1. إدارة الموارد
```kotlin
// ✅ صحيح
override fun onDestroy() {
    super.onDestroy()
    stopNfcMonitoring()
    releaseWakeLock()
    monitoringJob?.cancel()
}

// ❌ خطأ
override fun onDestroy() {
    super.onDestroy()
    // عدم تنظيف الموارد
}
```

### 2. معالجة الأخطاء
```kotlin
// ✅ صحيح
try {
    updateNfcStatusNotification()
} catch (e: Exception) {
    AppLogger.e(TAG, "Error updating notification", e)
    // محاولة إعادة المحاولة أو استخدام قيم افتراضية
}

// ❌ خطأ
updateNfcStatusNotification() // قد يتعطل التطبيق
```

### 3. الأداء
```kotlin
// ✅ صحيح
viewModelScope.launch(Dispatchers.IO) {
    val events = repository.getEvents()
    withContext(Dispatchers.Main) {
        updateUI(events)
    }
}

// ❌ خطأ
val events = repository.getEvents() // يحجب Main Thread
updateUI(events)
```

## الخلاصة

نظام الإشعارات في NFC Manager:
- ✅ **شامل:** يغطي جميع حالات الاستخدام
- ✅ **موثوق:** معالجة جيدة للأخطاء
- ✅ **فعال:** استهلاك معقول للموارد
- ✅ **قابل للتخصيص:** إعدادات مرنة للمستخدم
- ✅ **واضح:** رسائل مفهومة وتوصيات مفيدة

**التحسينات المستقبلية:**
1. إضافة BroadcastReceiver لحالة NFC
2. تحسين خوارزمية حساب الأمان
3. إضافة إشعارات ذكية بناءً على السلوك
4. دعم Android 14+ notification permissions
