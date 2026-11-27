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
 private volatile String shift;
 private long last;
 private volatile String token,staff,vehicle;
 private final java.util.concurrent.ScheduledExecutorService uploader=java.util.concurrent.Executors.newSingleThreadScheduledExecutor();
 private boolean scheduled=false;
 private android.net.ConnectivityManager connectivity;
 private android.net.ConnectivityManager.NetworkCallback networkCallback;
 private void watchNetwork(){
  if(networkCallback!=null)return;
  connectivity=getSystemService(android.net.ConnectivityManager.class);
  if(connectivity==null)return;
  networkCallback=new android.net.ConnectivityManager.NetworkCallback(){@Override public void onAvailable(android.net.Network network){if(!uploader.isShutdown())try{uploader.execute(()->upload());}catch(java.util.concurrent.RejectedExecutionException ignored){}}};
  try{connectivity.registerDefaultNetworkCallback(networkCallback);}catch(Exception e){networkCallback=null;}
 }
 private void upload(){
  final String sh=shift, st=staff, v=vehicle;
  if(sh==null||st==null||v==null)return;
  final String jwt;
  try{jwt=FdkGpsPlugin.refreshSession(this,null,false).getString("access_token");}catch(Exception e){return;}
  if(!sh.equals(shift))return;
  java.io.File dir=new java.io.File(getFilesDir(),"gps-pending");
  java.io.File[] files=dir.listFiles();if(files==null)return;
  java.util.Arrays.sort(files,java.util.Comparator.comparingLong(f->{try{return Long.parseLong(f.getName().substring(37,50));}catch(Exception e){return Long.MAX_VALUE;}}));
  for(java.io.File f:files){
   if(!sh.equals(shift)||Thread.currentThread().isInterrupted())return;
   if(!f.getName().endsWith(".json"))continue;
   try{
    String raw;try(java.io.FileInputStream in=new java.io.FileInputStream(f);java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);raw=new String(out.toByteArray(),StandardCharsets.UTF_8); }
    JSONObject j=new JSONObject(raw);
    // Never reattribute queued positions to a different driver or vehicle.
    if(!st.equals(j.optString("staff_id")))continue;
    String queuedShift=j.getString("shift_id");
    if(!f.getName().startsWith(queuedShift+"-"))continue;
    long ms=j.getLong("recorded_at_ms");
    JSONObject payload=new JSONObject();
    payload.put("p_shift",queuedShift);
    payload.put("p_event",f.getName().substring(f.getName().length()-41,f.getName().length()-5));
    payload.put("p_lat",j.getDouble("latitude"));payload.put("p_lon",j.getDouble("longitude"));
    payload.put("p_speed",j.has("speed_kmh")?j.getDouble("speed_kmh"):JSONObject.NULL);
    java.text.SimpleDateFormat fmt=new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",java.util.Locale.US);
    fmt.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
    payload.put("p_recorded",fmt.format(new java.util.Date(ms)));
    java.net.HttpURLConnection c=(java.net.HttpURLConnection)new java.net.URL("https://jwzgtzgmikkreecjuiey.supabase.co/rest/v1/rpc/receive_gps_point").openConnection();
    try{c.setConnectTimeout(10000);c.setReadTimeout(10000);c.setRequestMethod("POST");c.setDoOutput(true);
    c.setRequestProperty("apikey","sb_publishable_NS-C2C9bB934aGZICKWSog_woHtu-IM");
    c.setRequestProperty("Authorization","Bearer "+jwt);c.setRequestProperty("Content-Type","application/json");c.setRequestProperty("Prefer","return=minimal");
    try(java.io.OutputStream out=c.getOutputStream()){out.write(payload.toString().getBytes(StandardCharsets.UTF_8));}
    int status=c.getResponseCode();if(status>=200&&status<300)f.delete();else {if(status==401&&sh.equals(shift))FdkGpsPlugin.refreshSession(this,null,true);return;}
    }finally{c.disconnect();}
   }catch(Exception e){return;}
  }
 }

 @Override public IBinder onBind(Intent i){return null;}
 @Override public int onStartCommand(Intent i,int flags,int id){
  if(i==null || "stop".equals(i.getAction())){stopSelf();return START_NOT_STICKY;}
  if(!java.util.Objects.equals(shift,i.getStringExtra("shift")))last=0;
  shift=i.getStringExtra("shift");token=i.getStringExtra("token");staff=i.getStringExtra("staff");vehicle=i.getStringExtra("vehicle");
  if(shift==null || token==null || staff==null || vehicle==null || !shift.matches("[0-9a-fA-F-]{36}") || checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)!=PackageManager.PERMISSION_GRANTED){stopSelf();return START_NOT_STICKY;}
  NotificationManager nm=getSystemService(NotificationManager.class);
  if(Build.VERSION.SDK_INT>=26)nm.createNotificationChannel(new NotificationChannel("fdk_gps","GPS durante jornada",NotificationManager.IMPORTANCE_LOW));
  Intent stop=new Intent(this,FdkLocationService.class).setAction("stop");
  PendingIntent pi=PendingIntent.getService(this,1,stop,PendingIntent.FLAG_IMMUTABLE|PendingIntent.FLAG_UPDATE_CURRENT);
  Notification n=(Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"fdk_gps"):new Notification.Builder(this)).setContentTitle("FDK Manager").setContentText("Ubicación activa durante jornada").setSmallIcon(android.R.drawable.ic_menu_mylocation).setOngoing(true).addAction(new Notification.Action.Builder(null,"Detener GPS",pi).build()).build();
  startForeground(310,n);
  manager=getSystemService(LocationManager.class);
  try{manager.removeUpdates(this);manager.requestLocationUpdates(LocationManager.GPS_PROVIDER,5000,0,this,Looper.getMainLooper());}catch(Exception e){stopSelf();}
  watchNetwork();
  if(!scheduled){scheduled=true;uploader.scheduleWithFixedDelay(()->upload(),0,15,java.util.concurrent.TimeUnit.SECONDS);}
  return START_NOT_STICKY;
 }
 @Override public void onLocationChanged(Location p){
  if(shift==null || staff==null || vehicle==null || !p.hasAccuracy() || p.getAccuracy()>35 || System.currentTimeMillis()-p.getTime()>30000)return;
  long now=SystemClock.elapsedRealtime(); if(now-last<(p.hasSpeed()&&p.getSpeed()<0.83?60000:30000))return;
  try{JSONObject j=new JSONObject();j.put("shift_id",shift);j.put("staff_id",staff);j.put("vehicle_id",vehicle);j.put("latitude",p.getLatitude());j.put("longitude",p.getLongitude());j.put("accuracy",p.getAccuracy());j.put("recorded_at_ms",p.getTime());if(p.hasSpeed())j.put("speed_kmh",p.getSpeed()*3.6);
   java.io.File dir=new java.io.File(getFilesDir(),"gps-pending");dir.mkdirs();java.io.File dest=new java.io.File(dir,shift+"-"+p.getTime()+"-"+java.util.UUID.randomUUID()+".json");java.io.File tmp=new java.io.File(dir,dest.getName()+".tmp");try(FileOutputStream f=new FileOutputStream(tmp)){f.write(j.toString().getBytes(StandardCharsets.UTF_8));f.getFD().sync();}if(!tmp.renameTo(dest))throw new java.io.IOException("GPS queue commit failed");last=now;
  }catch(Exception e){android.util.Log.e("FDK","GPS local write failed",e);stopSelf();}
 }
 @Override public void onDestroy(){shift=null;if(connectivity!=null&&networkCallback!=null)try{connectivity.unregisterNetworkCallback(networkCallback);}catch(Exception ignored){}networkCallback=null;uploader.shutdownNow();if(manager!=null)manager.removeUpdates(this);stopForeground(STOP_FOREGROUND_REMOVE);super.onDestroy();}
 @Override public void onProviderEnabled(String p){}
 @Override public void onProviderDisabled(String p){}
 @Override public void onStatusChanged(String p,int s,Bundle b){}
}
