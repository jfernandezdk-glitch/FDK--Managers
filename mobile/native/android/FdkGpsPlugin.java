package cl.transportefdk.manager;
import android.Manifest;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import com.getcapacitor.*;
import com.getcapacitor.annotation.*;
@CapacitorPlugin(name="FdkGps",permissions={@Permission(alias="location",strings={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})})
public class FdkGpsPlugin extends Plugin {
 private static final String KEY_ALIAS="fdk.auth.v1";
 private static final Object AUTH_LOCK=new Object();
 private static final java.util.LinkedHashSet<String> priorRefreshTokens=new java.util.LinkedHashSet<>();
 private static javax.crypto.SecretKey authKey() throws Exception {
  java.security.KeyStore ks=java.security.KeyStore.getInstance("AndroidKeyStore");ks.load(null);
  if(ks.containsAlias(KEY_ALIAS))return (javax.crypto.SecretKey)ks.getKey(KEY_ALIAS,null);
  javax.crypto.KeyGenerator g=javax.crypto.KeyGenerator.getInstance("AES","AndroidKeyStore");
  g.init(new android.security.keystore.KeyGenParameterSpec.Builder(KEY_ALIAS,android.security.keystore.KeyProperties.PURPOSE_ENCRYPT|android.security.keystore.KeyProperties.PURPOSE_DECRYPT).setBlockModes("GCM").setEncryptionPaddings("NoPadding").setKeySize(256).build());return g.generateKey();
 }
 private android.content.SharedPreferences authPrefs(){return getContext().getSharedPreferences("fdk_secure_auth",0);}
 private String storageKey(PluginCall call){String k=call.getString("key");if(k==null||!k.matches("sb-[a-zA-Z0-9_-]{1,150}"))throw new IllegalArgumentException("Invalid auth storage key");return k;}
 @PluginMethod public void authGet(PluginCall call){synchronized(AUTH_LOCK){try{
  String k=storageKey(call),v=authPrefs().getString(k,null);JSObject result=new JSObject();
  if(v==null){result.put("value",org.json.JSONObject.NULL);}else{
   byte[] raw=android.util.Base64.decode(v,android.util.Base64.NO_WRAP);
   javax.crypto.Cipher c=javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
   c.init(javax.crypto.Cipher.DECRYPT_MODE,authKey(),new javax.crypto.spec.GCMParameterSpec(128,raw,0,12));
   c.updateAAD(k.getBytes(java.nio.charset.StandardCharsets.UTF_8));
   result.put("value",new String(c.doFinal(raw,12,raw.length-12),java.nio.charset.StandardCharsets.UTF_8));
  }call.resolve(result);
 }catch(Exception e){call.reject("No se pudo leer sesión segura",e);}}}
 @PluginMethod public void authSet(PluginCall call){synchronized(AUTH_LOCK){try{
  String k=storageKey(call),v=call.getString("value");if(v==null)throw new IllegalArgumentException("Missing value");
  if(SESSION_KEY.equals(k)){String current=readSession(getContext());String incoming=new org.json.JSONObject(v).getString("refresh_token");if(priorRefreshTokens.contains(incoming)){call.resolve();return;}if(current==null||!new org.json.JSONObject(current).getJSONObject("user").getString("id").equals(new org.json.JSONObject(v).getJSONObject("user").getString("id")))priorRefreshTokens.clear();}
  javax.crypto.Cipher c=javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");c.init(javax.crypto.Cipher.ENCRYPT_MODE,authKey());
  c.updateAAD(k.getBytes(java.nio.charset.StandardCharsets.UTF_8));byte[] iv=c.getIV();if(iv.length!=12)throw new IllegalStateException("Invalid IV");
  byte[] encrypted=c.doFinal(v.getBytes(java.nio.charset.StandardCharsets.UTF_8));byte[] raw=new byte[iv.length+encrypted.length];System.arraycopy(iv,0,raw,0,iv.length);System.arraycopy(encrypted,0,raw,iv.length,encrypted.length);
  if(!authPrefs().edit().putString(k,android.util.Base64.encodeToString(raw,android.util.Base64.NO_WRAP)).commit())throw new java.io.IOException("Secure storage write failed");call.resolve();
 }catch(Exception e){call.reject("No se pudo guardar sesión segura",e);}}}
 @PluginMethod public void authRemove(PluginCall call){synchronized(AUTH_LOCK){try{String k=storageKey(call);if(SESSION_KEY.equals(k)){String current=readSession(getContext());if(current!=null)priorRefreshTokens.add(new org.json.JSONObject(current).getString("refresh_token"));}if(!authPrefs().edit().remove(k).commit())throw new java.io.IOException("Secure storage removal failed");call.resolve();}catch(Exception e){call.reject("No se pudo eliminar sesión segura",e);}}}
 private static final String SESSION_KEY="sb-jwzgtzgmikkreecjuiey-auth-token";
 static String readSession(android.content.Context ctx) throws Exception {
  String v=ctx.getSharedPreferences("fdk_secure_auth",0).getString(SESSION_KEY,null);
  if(v==null)return null;
  byte[] raw=android.util.Base64.decode(v,android.util.Base64.NO_WRAP);
  javax.crypto.Cipher c=javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");
  c.init(javax.crypto.Cipher.DECRYPT_MODE,authKey(),new javax.crypto.spec.GCMParameterSpec(128,raw,0,12));
  c.updateAAD(SESSION_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  return new String(c.doFinal(raw,12,raw.length-12),java.nio.charset.StandardCharsets.UTF_8);
 }
 static void writeSession(android.content.Context ctx,String value) throws Exception {
  javax.crypto.Cipher c=javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");c.init(javax.crypto.Cipher.ENCRYPT_MODE,authKey());
  c.updateAAD(SESSION_KEY.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  byte[] iv=c.getIV(),enc=c.doFinal(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
  byte[] raw=new byte[iv.length+enc.length];System.arraycopy(iv,0,raw,0,iv.length);System.arraycopy(enc,0,raw,iv.length,enc.length);
  if(!ctx.getSharedPreferences("fdk_secure_auth",0).edit().putString(SESSION_KEY,android.util.Base64.encodeToString(raw,android.util.Base64.NO_WRAP)).commit())throw new java.io.IOException("Session write failed");
 }
 interface SessionStore {String read() throws Exception;void write(String value) throws Exception;}
 interface RefreshConnection {java.net.HttpURLConnection open() throws Exception;}
 static org.json.JSONObject refreshSession(android.content.Context ctx,String expected,boolean force) throws Exception {
  return refreshSession(new SessionStore(){public String read() throws Exception{return readSession(ctx);}public void write(String value) throws Exception{writeSession(ctx,value);}},()->(java.net.HttpURLConnection)new java.net.URL("https://jwzgtzgmikkreecjuiey.supabase.co/auth/v1/token?grant_type=refresh_token").openConnection(),expected,force);
 }
 static org.json.JSONObject refreshSession(SessionStore store,RefreshConnection connection,String expected,boolean force) throws Exception {
  synchronized(AUTH_LOCK){
   String raw=store.read();if(raw==null)throw new java.io.IOException("No session");
   org.json.JSONObject old=new org.json.JSONObject(raw);
   if(expected!=null&&!expected.equals(old.getString("refresh_token"))){if(priorRefreshTokens.contains(expected))return old;throw new java.io.IOException("Session changed");}
   if(!force&&old.optLong("expires_at")>System.currentTimeMillis()/1000+120)return old;
   java.net.HttpURLConnection c=connection.open();
   try{
    c.setConnectTimeout(10000);c.setReadTimeout(10000);c.setRequestMethod("POST");c.setDoOutput(true);
    c.setRequestProperty("apikey","sb_publishable_NS-C2C9bB934aGZICKWSog_woHtu-IM");c.setRequestProperty("Content-Type","application/json");
    org.json.JSONObject body=new org.json.JSONObject().put("refresh_token",old.getString("refresh_token"));
    try(java.io.OutputStream out=c.getOutputStream()){out.write(body.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));}
    int status=c.getResponseCode();if(status!=200)throw new java.io.IOException("Refresh HTTP "+status);
    java.io.ByteArrayOutputStream out=new java.io.ByteArrayOutputStream();try(java.io.InputStream in=c.getInputStream()){byte[] b=new byte[4096];int n;while((n=in.read(b))!=-1)out.write(b,0,n);}
    org.json.JSONObject next=new org.json.JSONObject(new String(out.toByteArray(),java.nio.charset.StandardCharsets.UTF_8));
    next.getString("access_token");next.getString("refresh_token");
    if(!old.getJSONObject("user").getString("id").equals(next.getJSONObject("user").getString("id")))throw new java.io.IOException("Session identity mismatch");
    if(!next.has("expires_at"))next.put("expires_at",System.currentTimeMillis()/1000+next.getLong("expires_in"));
    store.write(next.toString());priorRefreshTokens.add(old.getString("refresh_token"));while(priorRefreshTokens.size()>8)priorRefreshTokens.remove(priorRefreshTokens.iterator().next());return next;
   }finally{c.disconnect();}
  }
 }
 @PluginMethod public void authRefresh(PluginCall call){
  getBridge().execute(()->{try{org.json.JSONObject session=refreshSession(getContext(),call.getString("refreshToken"),true);JSObject result=new JSObject();result.put("session",session.toString());call.resolve(result);}catch(Exception e){call.reject("Session refresh failed",e);}});
 }
 @PluginMethod public void start(PluginCall call){
  if(getPermissionState("location")!=PermissionState.GRANTED){requestPermissionForAlias("location",call,"locationResult");return;}
  launch(call);
 }
 @PermissionCallback private void locationResult(PluginCall call){if(getPermissionState("location")==PermissionState.GRANTED)launch(call);else call.reject("Permiso de ubicación precisa requerido");}
 private void launch(PluginCall call){
  String shift=call.getString("shift");
  if(shift==null||!shift.matches("[0-9a-fA-F-]{36}")){call.reject("Jornada inválida");return;}
  try{Intent i=new Intent(getContext(),FdkLocationService.class).putExtra("shift",shift).putExtra("token",call.getString("token")).putExtra("staff",call.getString("staff")).putExtra("vehicle",call.getString("vehicle"));ContextCompat.startForegroundService(getContext(),i);call.resolve();}catch(Exception e){call.reject("No se pudo iniciar GPS",e);}
 }
 @PluginMethod public void stop(PluginCall call){getContext().stopService(new Intent(getContext(),FdkLocationService.class));call.resolve();}
}
