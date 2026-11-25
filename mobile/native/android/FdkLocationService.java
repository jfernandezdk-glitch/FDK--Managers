package cl.transportefdk.manager;
import android.app.*;
import android.content.*;
import android.location.*;
import android.os.*;
import android.Manifest;
import android.content.pm.PackageManager;
import org.json.JSONObject;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;

public class FdkLocationService extends Service implements LocationListener {
 private LocationManager manager;
 private String shift;
 private long last;
 @Override public IBinder onBind(Intent i){return null;}
 @Override public int onStartCommand(Intent i,int flags,int id){
  if(i==null || "stop".equals(i.getAction())){stopSelf();return START_NOT_STICKY;}
  shift=i.getStringExtra("shift");
  if(shift==null || !shift.matches("[0-9a-fA-F-]{36}") || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){stopSelf();return START_NOT_STICKY;}
  NotificationManager nm=getSystemService(NotificationManager.class);
  nm.createNotificationChannel(new NotificationChannel("fdk_gps","GPS durante jornada",NotificationManager.IMPORTANCE_LOW));
  Intent stop=new Intent(this,FdkLocationService.class).setAction("stop");
  PendingIntent pi=PendingIntent.getService(this,1,stop,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
  Notification n=new Notification.Builder(this,"fdk_gps").setContentTitle("FDK Manager").setContentText("Ubicación activa durante jornada").setSmallIcon(android.R.drawable.ic_menu_mylocation).setOngoing(true).addAction(new Notification.Action.Builder(null,"Detener GPS",pi).build()).build();
  startForeground(310,n);
  manager=getSystemService(LocationManager.class);
  try{manager.removeUpdates(this);manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,10000,0,this,Looper.getMainLooper());}catch(Exception e){stopSelf();}
  return START_NOT_STICKY;
 }
 @Override public void onLocationChanged(Location p){
  if(shift==null || p.getAccuracy()>35 || System.currentTimeMillis()-p.getTime()>30000)return;
  long now=SystemClock.elapsedRealtime(); if(now-last<(p.hasSpeed()&&p.getSpeed()<0.83?60000:10000))return;
  try{JSONObject j=new JSONObject();j.put("shift_id",shift);j.put("latitude",p.getLatitude());j.put("longitude",p.getLongitude());j.put("accuracy",p.getAccuracy());j.put("recorded_at_ms",p.getTime());if(p.hasSpeed())j.put("speed_kmh",p.getSpeed()*3.6);
   try(FileOutputStream f=openFileOutput("fdk-gps-"+shift+".jsonl",MODE_APPEND)){f.write((j.toString()+"\n").getBytes(StandardCharsets.UTF_8));}last=now;
  }catch(Exception e){android.util.Log.e("FDK","GPS local write failed",e);stopSelf();}
 }
 @Override public void onDestroy(){shift=null;if(manager!=null)manager.removeUpdates(this);stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
 @Override public void onProviderEnabled(String p){}
 @Override public void onProviderDisabled(String p){}
 @Override public void onStatusChanged(String p,int s,Bundle b){}
}
