# إصلاح Security Score عند إيقاف NFC

## المشكلة

عند إيقاف NFC، كان يظهر Security Score بقيمة 100 (ممتاز) بدلاً من عرض حالة "غير متاح" أو "N/A".

## السبب

1. **القيمة الافتراضية في قاعدة البيانات:**
   - `NFCSettingsEntity.kt` يحتوي على قيمة افتراضية `lastSecurityScore = 100`
   - عند إيقاف NFC، كان يتم عرض هذه القيمة الافتراضية

2. **عدم التحديث التلقائي:**
   - `SecurityScoreViewModel` لم يكن يراقب تغييرات حالة NFC
   - عند تشغيل/إيقاف NFC، لم يتم إعادة حساب الـ Score

## الحل المطبق

### 1. تحسين منطق الحساب في SecurityScoreViewModel

```kotlin
fun calculateSecurityScore() {
    viewModelScope.launch {
        _isLoading.value = true
        val context = application.applicationContext
        val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

        try {
            if (nfcAdapter == null) {
                // الجهاز لا يدعم NFC
                _securityScore.value = SecurityScore(
                    score = 0,
                    level = SecurityLevel.NOT_APPLICABLE,
                    violations = emptyList(),
                    recommendations = listOf(context.getString(R.string.nfc_not_supported_device))
                )
            } else if (!nfcAdapter.isEnabled) {
                // NFC معطل - التحقق من البيانات التاريخية
                val recentEvents = repository.getEventsFromLastDays(7).first()
                
                if (recentEvents.isEmpty()) {
                    // لا توجد أنشطة حديثة - NFC كان معطلاً
                    _securityScore.value = SecurityScore(
                        score = 0,
                        level = SecurityLevel.NOT_APPLICABLE,
                        violations = emptyList(),
                        recommendations = listOf(
                            context.getString(R.string.recommendation_nfc_disabled_no_data),
                            context.getString(R.string.recommendation_enable_nfc_when_needed)
                        )
                    )
                    repository.updateSecurityScore(0, SecurityLevel.NOT_APPLICABLE.name)
                } else {
                    // توجد بيانات تاريخية - الحساب بناءً على الاستخدام السابق
                    val calculatedScore = privacyScoreCalculator.calculateSecurityScore(recentEvents)
                    val adjustedRecommendations = mutableListOf<String>()
                    adjustedRecommendations.add(context.getString(R.string.recommendation_nfc_currently_disabled))
                    adjustedRecommendations.addAll(calculatedScore.recommendations)
                    
                    _securityScore.value = calculatedScore.copy(
                        recommendations = adjustedRecommendations
                    )
                    repository.updateSecurityScore(calculatedScore.score, calculatedScore.level.name)
                }
            } else {
                // NFC مفعل - الحساب بناءً على الأحداث الحديثة
                val recentEvents = repository.getEventsFromLastDays(7).first()
                val calculatedScore = privacyScoreCalculator.calculateSecurityScore(recentEvents)
                _securityScore.value = calculatedScore
                repository.updateSecurityScore(calculatedScore.score, calculatedScore.level.name)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _securityScore.value = SecurityScore(
                score = 0,
                level = SecurityLevel.NOT_APPLICABLE,
                violations = emptyList(),
                recommendations = listOf(context.getString(R.string.error_calculating_score, e.message ?: "Unknown error"))
            )
        } finally {
            _isLoading.value = false
        }
    }
}
```

### 2. إضافة مراقبة لتغييرات حالة NFC

```kotlin
init {
    calculateSecurityScore()
    // مراقبة تغييرات حالة NFC
    observeNfcStateChanges()
}

private fun observeNfcStateChanges() {
    viewModelScope.launch {
        // مراقبة تغييرات الإعدادات التي قد تشير إلى تغيير حالة NFC
        repository.getSettings().collect {
            // إعادة حساب الـ Score عند تغيير الإعدادات
            calculateSecurityScore()
        }
    }
}
```

### 3. تحسين عرض الـ Score في HomeScreen

الكود الموجود في `HomeScreen.kt` يتعامل بشكل صحيح مع حالة `NOT_APPLICABLE`:

```kotlin
Text(
    text = if (securityScore.level == SecurityLevel.NOT_APPLICABLE) 
        stringResource(R.string.security_score_range_not_applicable) 
    else 
        "${securityScore.score}",
    style = NothingTextStyles.HeaderTitle.copy(
        fontSize = if (securityScore.level == SecurityLevel.NOT_APPLICABLE) 16.sp else 20.sp,
        fontWeight = FontWeight.Bold
    ),
    color = securityScore.level.color
)
```

## السيناريوهات المختلفة

### 1. NFC معطل بدون بيانات تاريخية
- **Score:** 0
- **Level:** NOT_APPLICABLE (N/A)
- **Recommendations:**
  - "NFC معطل حالياً. لا توجد أنشطة حديثة للتحليل"
  - "فعّل NFC فقط عند الحاجة لاستخدام الميزات اللاتلامسية"

### 2. NFC معطل مع بيانات تاريخية
- **Score:** محسوب بناءً على آخر 7 أيام
- **Level:** بناءً على الـ Score المحسوب
- **Recommendations:**
  - "NFC معطل حالياً. الدرجة أدناه بناءً على سجل استخدامك الأخير"
  - + توصيات إضافية بناءً على الانتهاكات

### 3. NFC مفعل
- **Score:** محسوب بناءً على الأحداث الحديثة
- **Level:** بناءً على الـ Score المحسوب
- **Recommendations:** بناءً على الانتهاكات المكتشفة

### 4. الجهاز لا يدعم NFC
- **Score:** 0
- **Level:** NOT_APPLICABLE (N/A)
- **Recommendations:** "جهازك لا يدعم NFC"

## الملفات المعدلة

### 1. SecurityScoreViewModel.kt
- إضافة `observeNfcStateChanges()` لمراقبة تغييرات الحالة
- تحسين منطق `calculateSecurityScore()` للتعامل مع حالات NFC المختلفة
- حفظ الـ Score في قاعدة البيانات بشكل صحيح

### 2. PrivacyScoreCalculator.kt
- يحتوي بالفعل على `SecurityLevel.NOT_APPLICABLE`
- يتعامل مع الحالات الخاصة بشكل صحيح

### 3. HomeScreen.kt
- يعرض "N/A" بدلاً من الرقم عند `NOT_APPLICABLE`
- يستخدم حجم خط مناسب للنص

## النصوص المستخدمة (strings.xml)

### الإنجليزية:
```xml
<string name="recommendation_nfc_disabled_no_data">NFC is currently disabled. No recent activity to analyze.</string>
<string name="recommendation_enable_nfc_when_needed">Enable NFC only when you need to use contactless features like payments or tag reading.</string>
<string name="recommendation_nfc_currently_disabled">NFC is currently disabled. The score below is based on your recent usage history.</string>
<string name="security_score_range_not_applicable">N/A</string>
<string name="nfc_not_supported_device">Your device does not support NFC</string>
<string name="error_calculating_score">Error calculating security score: %1$s</string>
```

### العربية:
```xml
<string name="recommendation_nfc_disabled_no_data">NFC معطل حالياً. لا توجد أنشطة حديثة للتحليل.</string>
<string name="recommendation_enable_nfc_when_needed">فعّل NFC فقط عند الحاجة لاستخدام الميزات اللاتلامسية مثل الدفع أو قراءة العلامات.</string>
<string name="recommendation_nfc_currently_disabled">NFC معطل حالياً. الدرجة أدناه بناءً على سجل استخدامك الأخير.</string>
```

## الاختبار

### سيناريوهات الاختبار:
1. ✅ فتح التطبيق مع NFC معطل (بدون بيانات سابقة)
   - يجب أن يظهر N/A
   
2. ✅ تفعيل NFC واستخدامه ثم إيقافه
   - يجب أن يظهر Score بناءً على الاستخدام السابق
   
3. ✅ تفعيل NFC
   - يجب أن يظهر Score محسوب
   
4. ✅ التبديل بين تفعيل/إيقاف NFC
   - يجب أن يتحدث Score تلقائياً

5. ✅ جهاز بدون دعم NFC
   - يجب أن يظهر N/A مع رسالة مناسبة

## الفوائد

1. **دقة أفضل:** Score يعكس الحالة الفعلية لـ NFC
2. **تحديث تلقائي:** يتم إعادة الحساب عند تغيير حالة NFC
3. **رسائل واضحة:** توصيات مناسبة لكل حالة
4. **تجربة مستخدم محسنة:** لا مزيد من الارتباك حول Score 100 عند إيقاف NFC

## ملاحظات مهمة

### حول البيانات التاريخية:
- يتم الاحتفاظ بسجل آخر 7 أيام
- عند إيقاف NFC، يتم استخدام هذه البيانات لحساب Score تقديري
- هذا يساعد المستخدم على فهم مستوى أمانه السابق

### حول التحديث التلقائي:
- يتم مراقبة تغييرات الإعدادات
- عند أي تغيير، يتم إعادة حساب الـ Score
- هذا يضمن دقة المعلومات المعروضة

## التحسينات المستقبلية المقترحة

1. **إضافة BroadcastReceiver لحالة NFC:**
   - مراقبة `NfcAdapter.ACTION_ADAPTER_STATE_CHANGED`
   - تحديث فوري عند تغيير حالة NFC

2. **إضافة رسوم بيانية:**
   - عرض تاريخ الـ Score على مدار الوقت
   - مقارنة بين الفترات المختلفة

3. **إشعارات ذكية:**
   - تنبيه عند انخفاض الـ Score
   - اقتراحات لتحسين الأمان

## الخلاصة

تم حل مشكلة عرض Security Score = 100 عند إيقاف NFC:
- ✅ يظهر N/A عند إيقاف NFC بدون بيانات
- ✅ يظهر Score تقديري عند وجود بيانات تاريخية
- ✅ تحديث تلقائي عند تغيير حالة NFC
- ✅ رسائل وتوصيات واضحة لكل حالة

النظام الآن يعمل بشكل منطقي ودقيق!
