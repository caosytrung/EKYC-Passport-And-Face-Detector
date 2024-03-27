# Fast EKYC

The Android SDK are mainly used for verifying EKYC purpose. This sdk inludes these features:
1. Detect type of cards: id-card, passport, driving license.
2. Detect straigh-face,left-face, right face.
3. Support `auto-capture modes`: If the camera detects that the face is valid or the card is valid, SDK will capture images automatically.
4. Support customizing UI: button colors, font-size, text-color
5. Support customizing common configurations: flash,  zoom, camera side, debug-mode.
6. Support `tracking` to track user behaviors.

<img src="https://github.com/caosytrung/Fast-EKYC-Android-SDK/assets/17381611/154f6686-9760-4d2f-bac8-2ea2c2a52938" width="300">
<img src="https://github.com/caosytrung/Fast-EKYC-Android-SDK/assets/17381611/47e4c0fe-9371-4453-8297-0453ab382800" width="300">

### Prerequisites
Min Android-SDK version: 19.

Enable dataBinding. From build.gradle, add this code
```groovy
android {
   buildFeatures {
       dataBinding true
   }
}
```

### Start Ekyc

#### Build Config to open EKYC

```dart
val config = EkycConfigBuilder()
    .setIdCardTypes(mutableSetOf(EkycConfigmutableSetOf(EkycConfig.IdCardType.PASSPORT))
    .setUiFlowType(EkycConfig.UiFlowType.ID_CARD_FRONT)
    .isCacheImage(true)
    .setShowHelp(true)
    .setShowAutoCaptureButton(true)
    .setAutoCaptureMode(true)
    .setPopupBackgroundColor("#EFEFEE")
    .setTextColor("#000000")
    .setButtonColor("#EE0033")
    .setButtonCornerRadius(R.dimen.kyc_radius_extra_large) // 30dp
    .setFonts(regularFont, boldFont) // use your custom font
    .setShowFlashButton(false)
    .setZoom(1)
    .setIdCardCameraMode(CameraMode.BACK)
    .setSelfieCameraMode(CameraMode.FRONT)
    .setDebug(false)
    .build()
```

#### Open Ekyc from Activities.
```dart
ViettelEkycSDK.startEkyc(
   activity = activity,
   config = config
)
```
#### Open Ekyc from Fragments.
```dart
ViettelEkycSDK.startEkyc(
   fragment = fragment,
   config = config
)
```

#### Retrieve EKYC Result.
Using onActivityResult to get result. If resul† is null, it means that there is user-cancelled or there are some errors happened.
```dart
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    val result = FastEkycSDK.getResult(requestCode, resultCode, data)
    Log.d("Result", result?.toString() ?: "")
}
```
Ekyc Result Data
```dart
@Parcelize
data class KycUIFlowResult(
    override val resultState: ResultState,
    val imageId: String = "",
    var localCroppedImage: Bitmap? = null,
    var localFullImage: Bitmap? = null,
    var advanceImageDataList: List<AdvanceImageData>? = null,
) : KycResult(resultState), Parcelable

@Parcelize
class AdvanceImageData(
    val imageId: String? = null,
    val localImage: Bitmap? = null,
    val labelPose: String? = null
) : Parcelable
```

#### Setup tracking
Fast-EKYC provide an interface to help main-app can receive the tracks of user behaviours

Define your tracking interface.
``` dart
val tracker = object : EkycTracking {
    override fun createEventAndTrack(
        objectName: String,
        eventSrc: EventSrc,
        objectType: ObjectType,
        action: EventAction,
        eventValue: Map<String, Any>?,
    ) {
       // tracking data
    }
    
FastEkycSDK.setTracker(tracker)
```

==Contact me (caotrung.kk@gmail.com) to get the AI models before running apps.==
