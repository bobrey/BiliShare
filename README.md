# BiliShare
[ ![Download](https://api.bintray.com/packages/jungerr/maven/biliShare/images/download.svg)](https://bintray.com/jungerr/maven/biliShare/_latestVersion)

An android socializing SDK that supports to share text/image/web page/video/audio to qq/qzone/wechat/moment/sina platforms.

开发交流群：397462257
## Quick Overview
 - Download [bilibili][1]
 - screenshot 
 
 ![screenshot][2]
 
## Getting Started
The library "biliShare" is indispensable. But "biliShare-util" is not indispensable which  providers three ways to create platform selectors.
 - Add the dependency to your build.gradle.
```
dependencies {
    compile 'com.jungly.socialize:biliShare:0.13'
    compile 'com.jungly.socialize:biliShare-util:0.13@aar'
}
```

 - or Maven:
```
<dependency>
  <groupId>com.jungly.socialize</groupId>
  <artifactId>biliShare</artifactId>
  <version>0.13</version>
  <type>pom</type>
</dependency>
<dependency>
  <groupId>com.jungly.socialize</groupId>
  <artifactId>biliShare-util</artifactId>
  <version>0.13</version>
  <type>pom</type>
</dependency>
```

## How to use
  - Add .wxapi/WXEntryActivity to root package directory and config it in AndroidManifest.xml.
```java
public class WXEntryActivity extends BaseWXEntryActivity {
    @Override
    protected String getAppId() {
        return .....;
    }
}
```
```xml
<activity
    android:name=".wxapi.WXEntryActivity"
    android:configChanges="keyboardHidden|orientation|screenSize"
    android:exported="true"
    android:screenOrientation="portrait"
    android:theme="@android:style/Theme.Translucent.NoTitleBar"/>
```
- Add following code in AndroidManifest.xml.
```xml
<activity
    android:name="com.tencent.tauth.AuthActivity"
    android:launchMode="singleTask"
    android:noHistory="true">
    <intent-filter>
        <action android:name="android.intent.action.VIEW" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.BROWSABLE" />
        <data android:scheme="tencent---YourAppId" />
   </intent-filter>
</activity>
```
 - /sample/src/main/java/com/bilibili/socialize/sample/MainActivity.class

License
---

    Copyright 2015 Bilibili

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.


  [1]: http://wsdownload.hdslb.net/app/BiliPlayer3.apk
  [2]: http://7qnau5.com1.z0.glb.clouddn.com/Screenshot_2016-04-26-00-13-35.png?imageView2/1/w/360/h/640

