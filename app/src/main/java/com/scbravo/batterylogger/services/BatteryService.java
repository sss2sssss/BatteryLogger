package com.scbravo.batterylogger.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Chronometer;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.scbravo.batterylogger.Utilities.FileUtility;
import com.tud.cn.datalogger.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BatteryService extends Service {
    private static final String CHANNEL_ID = "BatteryServiceChannel";
    private Boolean ThreadStart;
    private Thread repeatTaskThread;
    public IntentFilter ifilter;
    public Intent batteryStatus;
    public boolean writeStatus;
    public String currentDate,currentTime;
    FileUtility fileUtility;
    float batteryPct;
    Chronometer chronometer;
    long timeWhenStopped;
    private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // do something
//            boolean writeStatus;
//            BatteryHelper batteryHelper = new BatteryHelper();
//            Battery batteryValue = batteryHelper.readBattery(intent);
//            Log.d("Battery:", String.valueOf(batteryValue));
//            String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
//            String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
//            FileUtility fileUtility = new FileUtility();
//            writeStatus = fileUtility.writeFileToSDCard(currentDate+'-'+currentTime+':'+batteryValue.toString(), getApplicationContext().getPackageName());
//            Log.d("BruteForcewriter", "File is written into " + writeStatus);
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        Log.d(String.valueOf(R.string.SERVICE_TAG), String.valueOf(R.string.SERVICE_ONCREATE));
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View view = inflater.inflate(R.layout.activity_main, null);
        chronometer = view.findViewById(R.id.service_status);
        Toast.makeText(this, R.string.SERVICE_START_BRUTE,Toast.LENGTH_LONG).show();
        ThreadStart = true;
//        BruteFetching(this);
    }
    @Override
    public void onDestroy() {
        Log.d("Service", "onDestroy");
        ThreadStart = false;
        Toast.makeText(this, R.string.BRUTE_SERVICE_END, Toast.LENGTH_LONG).show();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String input = intent.getStringExtra("inputExtra");
        createNotificationChannel();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0,
                getPackageManager().getLaunchIntentForPackage("com.scbravo.batterylogger"), 0);
        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Service BRUTE is running")
                .setContentText(input)
                .setSmallIcon(R.drawable.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();
        startForeground(1, notification);
        BruteFetching(this);
        return START_NOT_STICKY;
   }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel serviceChannel = new NotificationChannel(
                CHANNEL_ID,
                "Foreground Service Channel",
                NotificationManager.IMPORTANCE_DEFAULT);

        getSystemService(NotificationManager.class).createNotificationChannel(serviceChannel);
    }
    private void BruteFetching(final Context context)
    {
        /*Thread Running Per Duration (milliseconds)*/
        repeatTaskThread = new Thread()
        {
            public void run()
            {
                //ThreadStart as Thread escape alternative, since stop() method for Thread is now deprecated.
                while (ThreadStart)
                {
                    //Get Battery Status from IntentFilter from Android Intent
                    ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
                    //Register Receiver
                    batteryStatus = context.registerReceiver(null, ifilter);
                    int level = batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
                    int scale = batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
                    // Self-Define battery lvl, due to limitations in Battery/BatteryManager APIs
                    batteryPct = level * 100 / (float)scale;
                    Log.i("Current Battery %",String.valueOf(batteryPct));
                    // Assign Current Date & Time
                    currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                    currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
                    //Handler for Looper in Thread, prevent Exception.
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            fileUtility = new FileUtility();
                            fileUtility.writeFileToSDCard(currentDate+ "---" +currentTime+ "---" +batteryPct+'%', getApplicationContext().getPackageName());
                            Log.d("BruteForcewriter", "File is written into " + writeStatus);
                        }
                    });
                    try
                    {
                        /* Sleep for 1 minute:60000 millisecs, now test 10 sec : 10000secs
                        AlarmManager won't work for Battery because only fetch change % in phone., infact bad practice for start&kill service lifecycle*/
//                        Thread.sleep(10000);
                        Thread.sleep(60000);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                        Log.i("Thread", "Thread Is Halted for now.");
                    }
                }
            };
        };
        repeatTaskThread.start();
    }
}
