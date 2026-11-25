package cl.transportefdk.manager;
import android.Manifest;
import android.content.Intent;
import androidx.core.content.ContextCompat;
import com.getcapacitor.*;
import com.getcapacitor.annotation.*;
@CapacitorPlugin(name="FdkGps",permissions={@Permission(alias="location",strings={Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION})})
public class FdkGpsPlugin extends Plugin {
 @PluginMethod public void start(PluginCall call){
  if(getPermissionState("location")!=PermissionState.GRANTED){requestPermissionForAlias("location",call,"locationResult");return;}
  launch(call);
 }
 @PermissionCallback private void locationResult(PluginCall call){if(getPermissionState("location")==PermissionState.GRANTED)launch(call);else call.reject("Permiso de ubicación precisa requerido");}
 private void launch(PluginCall call){
  String shift=call.getString("shift");
  if(shift==null||!shift.matches("[0-9a-fA-F-]{36}")){call.reject("Jornada inválida");return;}
  try{Intent i=new Intent(getContext(),FdkLocationService.class).putExtra("shift",shift);ContextCompat.startForegroundService(getContext(),i);call.resolve();}catch(Exception e){call.reject("No se pudo iniciar GPS",e);}
 }
 @PluginMethod public void stop(PluginCall call){getContext().stopService(new Intent(getContext(),FdkLocationService.class));call.resolve();}
}
