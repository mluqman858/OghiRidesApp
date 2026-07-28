# Oghi Rides - Android App

## Kaise Setup Karein

### Step 1: Google Services JSON
1. Firebase Console jao: https://console.firebase.google.com
2. Project: **bykea-oghi-f8fed** select karo
3. ⚙️ Project Settings → General tab
4. "Your apps" section mein **Android app add** karo:
   - Package name: `com.oghrides.app`
   - App nickname: `Oghi Rides`
   - SHA-1 certificate (debug): Android Studio se le lo
5. **Download google-services.json**
6. File ko `app/` folder mein daalo: `D:\OghiRidesApp\app\google-services.json`

### Step 2: Android Studio mein Open karo
1. Android Studio kholo
2. File → Open → `D:\OghiRidesApp` select karo
3. Gradle sync hone do
4. Run button dabao → Phone pe install ho jayega

---

## Firebase Console Se Notification Kaise Bhejein

### Ride Request Notification (LOUD):
1. Firebase Console → **Messaging** (ya Cloud Messaging)
2. **New Campaign** → **Notifications**
3. Compose karo:
   - **Notification title**: `Naya Ride Request`
   - **Notification text**: `Ahmed se sawari chahiye - Oghi Bazaar se Mansehra`
4. **Advanced options** mein jaao:
   - **Custom data** add karo:
     - Key: `type` | Value: `ride_request`
     - Key: `customerId` | Value: `user_uid_here`
     - Key: `pickup` | Value: `Oghi Bazaar`
     - Key: `destination` | Value: `Mansehra`
5. **Send** karo

### Important: Data payload mein `type: ride_request` hona chahiye!
Tabhi driver ke phone pe LOUD alarm bajega.

---

## Notification Kaise Kaam Karti Hai

### Ride Request (type = "ride_request"):
- Screen unlock ho jati hai (Full Wake Lock)
- Alarm sound bajta hai (5 baar repeat)
- Heavy vibration pattern
- Notification hatt nahi sakti (ongoing)
- Screen lock pe bhi dikhti hai
- Do Not Disturb mode bhi override karta hai

### General Notification:
- Normal notification sound
- Swipe se hat sakti hai

---

## Custom Alarm Sound Daalne Ke Liye

Agar apni custom sound chahte ho:

1. Ek **loud alarm MP3** file download karo (internet se)
2. File ka naam rakho: `ride_alarm.mp3`
3. File ko yahan daalo: `app/src/main/res/raw/ride_alarm.mp3`
4. MyFirebaseMessagingService.kt mein yeh line change karo:

```kotlin
// Purani line:
val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

// Nayi line:
val alarmSound = Uri.parse("android.resource://" + packageName + "/" + R.raw.ride_alarm)
```

5. Same change `createNotificationChannel()` mein bhi karo:
```kotlin
// Purani line:
setSound(alarmUri, ...)

// Nayi line:
setSound(Uri.parse("android.resource://" + packageName + "/" + R.raw.ride_alarm), ...)
```

---

## App Features
- WebView wrapper - website load hoti hai
- Swipe refresh
- Camera + Gallery (CNIC upload)
- Offline screen
- Push notifications (LOUD alarm for rides)
- Back button navigation
- Auto-update - web pe change toh app mein auto aa jayega
