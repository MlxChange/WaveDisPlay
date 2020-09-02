# WaveDisPlayView
![](https://img.shields.io/badge/Platform-Android-brightgreen.svg)
![](https://img.shields.io/github/v/release/mlxchange/WaveDisPlay)
![](https://img.shields.io/badge/mlxchange-WaveDisPlay-brightgreen)
![](https://img.shields.io/badge/jitpack-2.0-blue)

WaveDisPlayView is an Android list view that allows you to preview the next view or the previous view by dragging left and right.


## Screenshot

<img src="/screenshot/screenshot.gif" width="360" height="640" alt="screenshot"/>

## DownLoad
[apk](/app/release/app-release.apk)

## Installation

Add it to your root build.gradle with:
```gradle
allprojects {
    repositories {
        maven { url "https://jitpack.io" }
    }
}
```
and app build.gradle:

```gradle
dependencies {
   implementation 'com.github.MlxChange:WaveDisPlay:0.1.1'
}
```

## Usage

```xml
<com.mlx.widget.WaveDisplayView
     android:layout_width="match_parent"
     android:layout_height="match_parent">
</com.mlx.widget.WaveDisplayView>
```
```kotlin
waveDisplayView = findViewById(R.id.wave)
waveAdapter = MyAdapter(this, mData)
waveDisplayView.setAdapter(waveAdapter)
```


## Attribute

```
dragColor //default white
dragWidth //drag button width
dragLocation //the starting position of the drag, the default is half of the height
```

## About Me

[My Blog](https://mlxchange.xyz "Mlx")

[juejin](https://juejin.im/user/1345457961046055/ "Mlx")



## License

<pre>
Copyright 2020 MlxChange

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
</pre>