package com.scbravo.batterylogger.services;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.scbravo.batterylogger.Utilities.FileUtility;
import com.scbravo.batterylogger.entity.Battery;
import com.scbravo.batterylogger.entity.NetworkState;
import com.scbravo.batterylogger.helpers.BatteryHelper;
import com.scbravo.batterylogger.helpers.CPUUsageHelper;
import com.scbravo.batterylogger.helpers.NetworkHelper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MyService extends Service {

	TelephonyManager telephonyManager;

	public MyService() {
		Log.d("Service", "Constructor");
	}

	@Override
	public IBinder onBind(Intent arg0) {
		throw new UnsupportedOperationException("Not yet implemented");

	}

	@Override
	public void onCreate() {
		Log.d("Service", "onCreate");
		/*
		 * TODO: To move this to Activity later, providing option for user to
		 * choose what data to log
		 */
		/*************** Battery Receiver Registration ********************/
		this.registerReceiver(this.batteryReceiver, new IntentFilter(
				Intent.ACTION_BATTERY_CHANGED));

		/*************** Network Connectivity Receiver Registration *******************************/
		this.registerReceiver(this.networkStateReceiver, new IntentFilter(
				ConnectivityManager.CONNECTIVITY_ACTION));

		telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

		Toast.makeText(this, "Receiver was created", Toast.LENGTH_LONG).show();

	}

	/*
	 * Receives a battery change intent and updates the battery value in the
	 * file
	 */
	/* TODO: To move this to custom Broadcast Receivers later */
	private BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context arg0, Intent intent) {
			
			Log.d("Battery", "IntentRecieved!");
			BatteryHelper batteryHelper = new BatteryHelper();
			Battery batteryValue = batteryHelper.readBattery(intent);
			Log.d("Battery:", batteryValue.toString());
			FileUtility fileUtility = new FileUtility();
			String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
			String currentTime = new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(new Date());
			fileUtility.writeFileToSDCard(currentDate+'-'+currentTime+'-'+batteryValue.toString(), getApplicationContext().getPackageName());
			boolean writeStatus;
			ActivityManager mgr = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//			TaskListHelper taskListHelper = new TaskListHelper(20);
//			List<ActivityManager.RunningTaskInfo> allTasks = taskListHelper
//					.getRunningTaskInfo(mgr);
//			for (ActivityManager.RunningTaskInfo aTask : allTasks) {
//				Log.d("TaskList", "Task: " + aTask.baseActivity.getClassName());
//				fileUtility.writeFileToSDCard("\r\n" + aTask.baseActivity.getClassName(), getApplicationContext().getPackageName());
//
//			}

			CPUUsageHelper cpuUsageHelper = new CPUUsageHelper();
			float usage = cpuUsageHelper.getUsage();
			int cores = cpuUsageHelper.getNumCores();
			Log.d("CPU", "Usage : " + usage);
			Log.d("CPU", "Cores : " + cores);
			writeStatus = fileUtility.writeFileToSDCard("\r\n" + "Usage : " + usage + " Cores : " + cores, getApplicationContext().getPackageName());

			/****************************************************/

			/*fileUtility.writeFileToSDCard("\r\n\r\n\r\n\r\n", getApplicationContext().getPackageName());*/
			Log.d("FileWriterService", "File is written into " + writeStatus);

		}
	};

	/*
	 * Receives a Network change intent and updates the network state into a
	 * file
	 */
	/* TODO: To move this to custom Broadcast Receivers later */
	BroadcastReceiver networkStateReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			FileUtility fileUtility = new FileUtility();
			Log.d("Network", "Network Type Changed");
			NetworkHelper networkHelper = new NetworkHelper();
			NetworkState networkState = networkHelper.readNetworkState(context);
			Log.d("Network", networkState.toString());
			fileUtility.writeFileToSDCard("\r\n" + "Network : " + networkState.toString(), getApplication().getPackageName());
		}
	};

	PhoneStateListener phoneStateListener = new PhoneStateListener() {
		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			FileUtility fileUtility = new FileUtility();
			super.onSignalStrengthsChanged(signalStrength);
			Log.d("Signal",
					"GSM Signal Strength = "
							+ signalStrength
							.getGsmSignalStrength());
			fileUtility.writeFileToSDCard("\r\n" + "GSM Signal Strength : " + signalStrength
							.getGsmSignalStrength(),getApplicationContext().getPackageName());
		}
	};

	@Override
	public void onStart(Intent intent, int startId) {
		// For time consuming an long tasks you can launch a new thread here...
		Log.d("Service", "onStart");
		Toast.makeText(this, " Service Started", Toast.LENGTH_LONG).show();

	}

	@Override
	public void onDestroy() {
		Log.d("Service", "onDestroy");
		this.unregisterReceiver(this.batteryReceiver);
		this.unregisterReceiver(this.networkStateReceiver);
		telephonyManager.listen(phoneStateListener,
				PhoneStateListener.LISTEN_NONE);
		Toast.makeText(this, "Service Destroyed", Toast.LENGTH_LONG).show();

	}

}
