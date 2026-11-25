import {readFile,writeFile,copyFile} from 'node:fs/promises';
const root='android/app/src/main/';
await copyFile('native/android/FdkLocationService.java',root+'java/cl/transportefdk/manager/FdkLocationService.java');
const p=root+'AndroidManifest.xml';
let s=await readFile(p,'utf8');
s=s.replace('</manifest>', '<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/><uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/><uses-permission android:name="android.permission.FOREGROUND_SERVICE"/><uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION"/><uses-permission android:name="android.permission.POST_NOTIFICATIONS"/></manifest>');
s=s.replace('</application>','<service android:name=".FdkLocationService" android:exported="false" android:foregroundServiceType="location"/></application>');
await writeFile(p,s);
