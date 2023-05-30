package com.iamverycute.air;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class AirActivity extends Activity implements View.OnClickListener, Shizuku.OnRequestPermissionResultListener {
    private boolean permissionIsGranted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }

    @Override
    protected void onResume() {
        Intent shizukuIntent = getPackageManager().getLaunchIntentForPackage("moe.shizuku.privileged.api");
        if (shizukuIntent != null) {
            if (Shizuku.pingBinder()) {
                permissionIsGranted = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED;
                if (permissionIsGranted) {
                    findViewById(R.id.tips).setAlpha(0F);
                } else {
                    Shizuku.addRequestPermissionResultListener(this);
                    Shizuku.requestPermission(0);
                }
            }
        }
        super.onResume();
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        permissionIsGranted = grantResult == 0;
    }

    private Object IConnectivityManager;
    private Method setAirplaneMode;

    @Override
    public void onClick(View view) {
        if (!permissionIsGranted) return;
        if (IConnectivityManager == null) {
            try {
                IConnectivityManager = Class.forName("android.net.IConnectivityManager$Stub").getMethod("asInterface", IBinder.class).invoke(null, new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.CONNECTIVITY_SERVICE)));
                //same to IConnectivityManager.Stub.asInterface(SystemService)
            } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | IllegalAccessException ignored) {
            }
        }
        if (setAirplaneMode == null) {
            try {
                setAirplaneMode = IConnectivityManager.getClass().getDeclaredMethod("setAirplaneMode", boolean.class);
            } catch (NoSuchMethodException ignored) {
            }
        }
        try {
            setAirplaneMode.invoke(IConnectivityManager, ((Switch) view).isChecked());
        } catch (IllegalAccessException | InvocationTargetException ignored) {
            Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show();
        }
    }
}