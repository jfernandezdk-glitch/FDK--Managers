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
 private javax.crypto.SecretKey authKey() throws Exception {
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
  javax.crypto.Cipher c=javax.crypto.Cipher.getInstance("AES/GCM/NoPadding");c.init(javax.crypto.Cipher.ENCRYPT_MODE,authKey());
  c.updateAAD(k.getBytes(java.nio.charset.StandardCharsets.UTF_8));byte[] iv=c.getIV();if(iv.length!=12)throw new IllegalStateException("Invalid IV");
  byte[] encrypted=c.doFinal(v.getBytes(java.nio.charset.StandardCharsets.UTF_8));byte[] raw=new byte[iv.length+encrypted.length];System.arraycopy(iv,0,raw,0,iv.length);System.arraycopy(encrypted,0,raw,iv.length,encrypted.length);
  if(!authPrefs().edit().putString(k,android.util.Base64.encodeToString(raw,android.util.Base64.NO_WRAP)).commit())throw new java.io.IOException("Secure storage write failed");call.resolve();
 }catch(Exception e){call.reject("No se pudo guardar sesión segura",e);}}}
 @PluginMethod public void authRemove(PluginCall call){synchronized(AUTH_LOCK){try{if(!authPrefs().edit().remove(storageKey(call)).commit())throw new java.io.IOException("Secure storage removal failed");call.resolve();}catch(Exception e){call.reject("No se pudo eliminar sesión segura",e);}}}
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
