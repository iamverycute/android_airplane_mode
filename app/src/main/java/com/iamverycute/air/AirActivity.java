package com.iamverycute.air;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import rikka.shizuku.Shizuku;
import rikka.shizuku.ShizukuBinderWrapper;
import rikka.shizuku.SystemServiceHelper;

public class AirActivity extends Activity implements CompoundButton.OnCheckedChangeListener, Shizuku.OnRequestPermissionResultListener {
    private Object IConnectivityManager;
    private Method setAirplaneMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Switch switcher = (Switch) findViewById(R.id.switcher);
        switcher.setOnCheckedChangeListener(this);
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.addRequestPermissionResultListener(this);
                Shizuku.requestPermission(0);
            }
        }
        try {
            IConnectivityManager = Class.forName("android.net.IConnectivityManager$Stub").getMethod("asInterface", IBinder.class).invoke(null, new ShizukuBinderWrapper(SystemServiceHelper.getSystemService(Context.CONNECTIVITY_SERVICE)));
            //same to IConnectivityManager.Stub.asInterface(SystemService)
            setAirplaneMode = IConnectivityManager.getClass().getDeclaredMethod("setAirplaneMode", boolean.class);
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException |
                 IllegalAccessException ignored) {
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
        if (IConnectivityManager != null && setAirplaneMode != null)
            try {
                setAirplaneMode.invoke(IConnectivityManager, isChecked);
            } catch (IllegalAccessException | InvocationTargetException ignored) {
                Toast.makeText(this, "Unknown Error", Toast.LENGTH_SHORT).show();
            }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
    }
}