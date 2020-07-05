# RippleMapView
[![](https://jitpack.io/v/serhatleventyavas/RippleMapView.svg)](https://jitpack.io/#serhatleventyavas/RippleMapView)

RippleMapView allows to implement ripple effect to google map easily.

### Setup

------------

### Step 1. Add the JitPack repository to your build file
Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
### Step 2. Add the dependency
```
dependencies {
	implementation 'com.github.serhatleventyavas:RippleMapView:1.0.0'
}
```
### Step 3. Implement RippleMapView

```
val rippleMapView = this.googleMap?.let {
    RippleMapView.Builder(this, it)
        .fillColor(resources.getColor(R.color.colorPrimaryDark))
        .strokeColor(resources.getColor(R.color.colorPrimaryDark))
        .latLng(LatLng(41.009146, 29.034022))
        .numberOfRipples(3)
        .build()
}
rippleMapView?.startRippleMapAnimation()
```

### Step 4. If you want to change the location
```
rippleMapView?.withLatLng(location);
```

## License

RippleMapView is released under the MIT license. [See LICENSE](https://github.com/serhatleventyavas/RippleMapView/blob/master/LICENSE) for details.