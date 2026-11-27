import {readFile,writeFile,copyFile} from 'node:fs/promises';
const root='android/app/src/main/';
await copyFile('native/android/FdkLocationService.java',root+'java/cl/transportefdk/manager/FdkLocationService.java');
const p=root+'AndroidManifest.xml';
let s=await readFile(p,'utf8');
s=s.replace('</manifest>', '<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE"/><uses-permission android:name="android.permission.ACCESS_FINE_LOCATION"/><uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION"/><uses-permission android:name="android.permission.FOREGROUND_SERVICE"/><uses-permission android:name="android.permission.FOREGROUND_SERVICE_LOCATION"/><uses-permission android:name="android.permission.POST_NOTIFICATIONS"/></manifest>');
s=s.replace('</application>','<service android:name=".FdkLocationService" android:exported="false" android:foregroundServiceType="location"/></application>');
s=s.replace('<application','<application android:enableOnBackInvokedCallback="false"');
await writeFile(p,s);

const java=root+'java/cl/transportefdk/manager/';
await copyFile('native/android/FdkGpsPlugin.java',java+'FdkGpsPlugin.java');
await writeFile(java+'MainActivity.java', 'package cl.transportefdk.manager;\nimport android.os.Bundle;\nimport com.getcapacitor.BridgeActivity;\npublic class MainActivity extends BridgeActivity { @Override public void onCreate(Bundle b){registerPlugin(FdkGpsPlugin.class);super.onCreate(b);} @Override public void onBackPressed(){if(getBridge()==null||getBridge().getWebView()==null)return;getBridge().getWebView().evaluateJavascript("typeof window.fdkBack === \\\"function\\\" ? window.fdkBack(true) : false", result -> {if("true".equals(result))moveTaskToBack(true);});} }\n');

// Traccar SDK pinned for compile compatibility; activation requires secure receiver.
if(process.env.FDK_TRACCAR_EXPERIMENT==='1'){
 const gradle='android/app/build.gradle';
 let g=await readFile(gradle,'utf8');
 if(!g.includes('org.traccar:traccar-client-sdk'))g+='\ndependencies { implementation "org.traccar:traccar-client-sdk:1.1.4" }\n';
 await writeFile(gradle,g);
}
