package com.example.wifirss;

import static androidx.constraintlayout.widget.ConstraintLayoutStates.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class GetLocRssi extends AppCompatActivity {
    TextView textView;
    WifiManager wifi;
    private static final int REQUEST_PERMISSION = 1001;

    private String[] permissions = new String[]{
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    /**
     * 被用户拒绝的权限列表
     */
    private List<String> mPermissionList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_get_loc_rssi);

        textView = (TextView) findViewById(R.id.text1);


    }

    @Override
    protected void onStart() {
        Log.i(TAG, "onStart");
        checkPermissions();
        super.onStart();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PERMISSION) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Necessary permission: " + permissions[i], Toast.LENGTH_LONG).show();
                    Log.i(TAG, permissions[i] + " 权限被用户禁止！");
                }
            }
        }
    }

    /***
     * 检查权限
     */
    private boolean checkPermissions() {
        boolean checked = false;
        mPermissionList.clear();
        //6.0 动态权限判断
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(getApplicationContext(), permissions[i])
                        != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions[i]);
                }
            }
            if (!mPermissionList.isEmpty()) {
                String[] permissions = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSION);
                checked = false;
            } else {
                checked = true;
            }
        }
        return checked;
    }


    private Timer autoUpdate;
    @Override
    public void onResume() {
        super.onResume();
        autoUpdate = new Timer();
        autoUpdate.schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    public void run() {
                        updateHTML();
                    }
                });
            }
        }, 0, 2000);
    }


    private void updateHTML(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Context context = this;
        wifi = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
        BroadcastReceiver wifiScanReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                boolean success = intent.getBooleanExtra(
                        WifiManager.EXTRA_RESULTS_UPDATED, false);
                if (success) {
                    scanSuccess();
                } else {
                    // scan failure handling
                    scanFailure();
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(wifiScanReceiver, intentFilter);

        boolean success = wifi.startScan();
        if (!success) {
            // scan failure handling
            scanFailure();
        }




        List<ScanResult> results = wifi.getScanResults();
        int s = results.size();
        String names="";
        String rs1="";
        String rs2="";
        String rs3="";
        String rs4="";
        String rs5="";
//        for (ScanResult scanResult : results) {
//            if(scanResult.SSID.equals("AP1")) {
//                rs1 = "" + scanResult.level;
//                names = names + "AP1:" + rs1 + "    ";
//            }
//            else if(scanResult.SSID.equals("AP2")) {
//                rs2 = "" + scanResult.level;
//                names = names + "AP2: " + rs2 + "    ";
//            }
//            else if(scanResult.SSID.equals("AP3")) {
//                rs2 = "" + scanResult.level;
//                names = names + "AP3: " + rs3 + "    ";
//            }
//            else if(scanResult.SSID.equals("AP4")) {
//                rs2 = "" + scanResult.level;
//                names = names + "AP4: " + rs4 + "    ";
//            }
//            else if(scanResult.SSID.equals("AP5")) {
//                rs2 = "" + scanResult.level;
//                names = names + "AP5: " + rs5 + "    ";
//            }
//            System.out.println("inside for");
//
//        }
//        Toast.makeText(getApplicationContext(), "scan result is \n" +s, Toast.LENGTH_SHORT).show();
        textView = (TextView) findViewById(R.id.text1);
//        textView.setText("scan result is \n" +names);
        textView.setText("scan result is \n" +s);



//        try{
//
//            URL link = new URL("IPV4:port/?names="+names);
//            System.out.println("IPV4:port/?names="+names);
//            URLConnection conn = link.openConnection();
//            conn.setDoOutput(true);
//
//            BufferedReader in = new BufferedReader(new InputStreamReader(link.openStream()));
//
//            in.close();
//
//
//        }
//        catch(Exception e)
//        {
//            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_SHORT).show();
//        }


    }

    private void scanSuccess() {
        List<ScanResult> results = wifi.getScanResults();
        int s = results.size();
        textView.setText("Number of scan result is \n" +s);
    }

    private void scanFailure() {
        // handle failure: new scan did NOT succeed
        // consider using old scan results: these are the OLD results!
        List<ScanResult> results = wifi.getScanResults();
        Toast.makeText(getApplicationContext(), "Failed", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onPause() {
        autoUpdate.cancel();
        super.onPause();
    }
//    public void gotoStop(View view)
//    {
//        Intent i = new Intent(this, MainActivity.class);
//        startActivity(i);
//    }

}