package com.scbravo.batterylogger;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;

import androidx.core.app.ActivityCompat;

import com.scbravo.batterylogger.services.BatteryService;
import com.scbravo.batterylogger.services.MyService;
import com.tud.cn.datalogger.R;

public class MainActivity extends Activity {
	Chronometer chronometer;
	public long pausedOffset;
	Button serviceBtn, serviceBruteBtn, serviceStopBtn;

	public void isStoragePermissionGranted() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED) {
				Log.v("PERMISSION:","Permission is granted");
			} else {

				Log.v("PERMISSION","Permission is revoked");
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
			}
		}
		else { //permission is automatically granted on sdk<23 upon installation
			Log.v("PERMISSION","Permission is granted");
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		chronometer = findViewById(R.id.service_status);
		serviceBtn = findViewById(R.id.button1);
		serviceBruteBtn = findViewById(R.id.button);
		serviceStopBtn = findViewById(R.id.button2);
		chronometer.setBase(SystemClock.elapsedRealtime() + pausedOffset);
		chronometer.setTextColor(getResources().getColor(R.color.orangeHex));
		}

	public void startNewService(View view) {
		isStoragePermissionGranted();
		startService(new Intent(this, MyService.class));

		chronometer.start();
		chronometer.setTextColor(getResources().getColor(R.color.greenHex));
		serviceBruteBtn.setEnabled(false);
		serviceBtn.setEnabled(false);
		serviceStopBtn.setEnabled(true);
	}


	public void startNewServiceBrute(View view) {
		isStoragePermissionGranted();
		startService(new Intent(this, BatteryService.class));
		chronometer.setTextColor(getResources().getColor(R.color.greenHex));
		chronometer.start();
		serviceBruteBtn.setEnabled(false);
		serviceBtn.setEnabled(false);
		serviceStopBtn.setEnabled(true);
	}

	// Stop the service
	public void stopNewService(View view) {
		/*
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("Email:");
		alert.setMessage("The data logger file is stored at the path : /storage/sdcard/TestDir/TestingFileWriter.txt \n If the file has to be mailed ,  Enter the email Id for the file to be sent");

		// Set an EditText view to get user input
		final EditText input = new EditText(this);
		alert.setView(input);
		
		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				Editable value = input.getText();
				System.out.println(value.toString());
				Intent email = new Intent(Intent.ACTION_SEND);
				email.putExtra(Intent.EXTRA_EMAIL, value.toString());
				email.putExtra(Intent.EXTRA_SUBJECT, "Data logger");
				File sd = Environment.getExternalStorageDirectory();
				Log.i("sd:",sd.toString());
				File logFile = new File(sd, new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date())+".txt");

				email.putExtra(
						Intent.EXTRA_STREAM,
						Uri.fromFile(logFile));
				email.putExtra(Intent.EXTRA_TEXT,
						"Here is the data logger file containing your mobile log info");
				email.setType("text/plain");
				startActivity(Intent.createChooser(email,
						"Choose an Email client :"));
				// Write a code to send Email
			}
		});

		alert.setNegativeButton("Cancel",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Simple close it without doing anything
					}
				});
		alert.show();*/

		stopService(new Intent(this, MyService.class));
		serviceBtn.setEnabled(true);
		stopService(new Intent(this, BatteryService.class));
		serviceBruteBtn.setEnabled(true);
		serviceStopBtn.setEnabled(false);
		pausedOffset =   chronometer.getBase() - SystemClock.elapsedRealtime();
		Log.i("DIFFERENCE TIME", String.valueOf(pausedOffset));
		chronometer.stop();
		chronometer.setBase(SystemClock.elapsedRealtime());
		pausedOffset = 0;
		chronometer.setTextColor(getResources().getColor(R.color.redHex));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		if (isMyServiceRunning(BatteryService.class) || isMyServiceRunning(MyService.class)) {
			//GO HOME, SharedPref and Instance doesn't work with Chronometer lul
				Intent intent = new Intent(Intent.ACTION_MAIN);
				intent.addCategory(Intent.CATEGORY_HOME);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				startActivity(intent);
			return;
		} else {
			super.onBackPressed();
		}
	}
	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
