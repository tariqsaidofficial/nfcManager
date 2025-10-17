# Security Score System Update

## Overview
تم تحديث نظام Security Score لتوفير تقييم أكثر دقة وواقعية لحالة الأمان في التطبيق.

## المشكلة السابقة
كان النظام السابق يعرض درجة أمان 100 (EXCELLENT) عندما يكون NFC معطلاً، مما يعطي انطباعاً خاطئاً بأن التطبيق يقيّم الأمان بشكل فعلي.

## الحل الجديد

### 1. آلية الحساب المحدثة

#### عندما يكون NFC غير مدعوم
- **Score**: 0
- **Level**: NOT_APPLICABLE
- **Message**: "NFC is not supported on this device"

#### عندما يكون NFC معطلاً

##### حالة 1: لا يوجد نشاط سابق (No Historical Data)
- **Score**: 0
- **Level**: NOT_APPLICABLE
- **Recommendations**:
  - "NFC is currently disabled. No recent activity to analyze."
  - "Enable NFC only when you need to use contactless features like payments or tag reading."

##### حالة 2: يوجد نشاط سابق (Has Historical Data)
- **Score**: محسوب بناءً على آخر 7 أيام من النشاط
- **Level**: يعتمد على النتيجة المحسوبة
- **Recommendations**: 
  - "NFC is currently disabled. The score below is based on your recent usage history."
  - + التوصيات المحسوبة من النشاط السابق

#### عندما يكون NFC مفعلاً
- **Score**: محسوب بناءً على الأحداث الأخيرة
- **Level**: يعتمد على النتيجة
- **Recommendations**: توصيات مخصصة بناءً على الانتهاكات المكتشفة

### 2. معايير الحساب

#### نقاط الخصم من الدرجة (تبدأ من 100)
- `NFC_ENABLED_LONG`: -15 نقطة
- `UNKNOWN_TAG`: -10 نقاط
- `MULTIPLE_TAGS_RAPID`: -20 نقطة
- `SUSPICIOUS_ACTIVITY`: -25 نقطة
- `TAG_DISCOVERED`: -2 نقطة

#### مستويات الأمان
- **90-100**: EXCELLENT (ممتاز)
- **75-89**: GOOD (جيد)
- **60-74**: MODERATE (متوسط)
- **40-59**: POOR (ضعيف)
- **0-39**: CRITICAL (حرج)
- **N/A**: NOT_APPLICABLE (غير قابل للتطبيق)

### 3. التحديثات في الواجهة

#### Dashboard (HomeScreen)
- عرض "N/A" بدلاً من رقم عندما يكون المستوى NOT_APPLICABLE
- تصغير حجم النص للـ "N/A" ليتناسب مع الدائرة
- عرض اسم المستوى بشكل واضح

#### Security Score Detail Screen
- عرض "N/A" في الدائرة الكبيرة عندما يكون المستوى NOT_APPLICABLE
- عرض التوصيات المناسبة لكل حالة
- توضيح أن النتيجة مبنية على البيانات التاريخية عند تعطيل NFC

### 4. النصوص المضافة

#### English (values/strings.xml)
```xml
<string name="recommendation_nfc_disabled_no_data">NFC is currently disabled. No recent activity to analyze.</string>
<string name="recommendation_enable_nfc_when_needed">Enable NFC only when you need to use contactless features like payments or tag reading.</string>
<string name="recommendation_nfc_currently_disabled">NFC is currently disabled. The score below is based on your recent usage history.</string>
```

#### Arabic (values-ar/strings.xml)
```xml
<string name="recommendation_nfc_disabled_no_data">NFC معطل حالياً. لا يوجد نشاط حديث لتحليله.</string>
<string name="recommendation_enable_nfc_when_needed">قم بتفعيل NFC فقط عند الحاجة لاستخدام ميزات اللاتلامس مثل المدفوعات أو قراءة العلامات.</string>
<string name="recommendation_nfc_currently_disabled">NFC معطل حالياً. النتيجة أدناه مبنية على سجل استخدامك الأخير.</string>
```

## الملفات المعدلة

1. **SecurityScoreViewModel.kt**
   - تحديث منطق `calculateSecurityScore()`
   - إضافة فحص للبيانات التاريخية
   - تحسين التوصيات بناءً على الحالة

2. **HomeScreen.kt**
   - تحديث `SecurityScoreQuickCard` لعرض "N/A"
   - تعديل حجم النص ديناميكياً

3. **strings.xml (English & Arabic)**
   - إضافة نصوص جديدة للتوصيات

## الفوائد

1. **دقة أكبر**: النظام الآن يعكس الحالة الفعلية للأمان
2. **شفافية أفضل**: المستخدم يفهم أن النتيجة مبنية على بيانات فعلية
3. **توصيات مفيدة**: التوصيات الآن تتناسب مع الحالة الفعلية
4. **تجربة مستخدم محسنة**: عرض واضح للحالات المختلفة

## الاختبار المطلوب

1. ✅ اختبار عندما يكون NFC معطلاً بدون بيانات سابقة
2. ✅ اختبار عندما يكون NFC معطلاً مع وجود بيانات سابقة
3. ✅ اختبار عندما يكون NFC مفعلاً
4. ✅ اختبار على جهاز لا يدعم NFC
5. ✅ التحقق من الترجمة العربية والإنجليزية

## ملاحظات للمطور

- النظام يعتمد على آخر 7 أيام من البيانات
- يمكن تعديل فترة التحليل من خلال `repository.getEventsFromLastDays(7)`
- يمكن تخصيص نقاط الخصم في `PrivacyScoreCalculator.kt`
- جميع النصوص قابلة للترجمة ومخزنة في ملفات strings.xml
