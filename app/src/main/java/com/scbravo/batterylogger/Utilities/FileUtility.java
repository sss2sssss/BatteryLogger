package com.scbravo.batterylogger.Utilities;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FileUtility extends Activity {

	private String currentDate;
	private File path;
	private Boolean storageState;
//	private Context context;
	@SuppressWarnings("unused")
	private static void IGNORE_RESULT(boolean b) {}

	//	SC_TODO: Check Internal or External Storage
	public boolean writeFileToSDCard(String content, String packageId) {

		//validate the mounted state
		storageState = Environment.isExternalStorageRemovable();
		if (!storageState){
			path = Environment.getExternalStorageDirectory();
			Log.i("EXPECTED PATH (int) ", path.toString());
		}
		else{
			path = Environment.getExternalStorageDirectory();
			Log.i("EXPECTED PATH  (ext) ", path.toString());
		}

		File destDir = new File(path , packageId);
		if (!destDir.exists()) {
			Log.i("Does the dir exists:", String.valueOf(destDir.exists()));
			IGNORE_RESULT(destDir.mkdir());
		}
		currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
		File txtFile = new File(path, packageId+"/Log-"+currentDate+".txt");
		BufferedWriter bufWriter;
		try {
			 bufWriter = new BufferedWriter(new FileWriter(txtFile,true));
			bufWriter.write(content);
			bufWriter.newLine();
			bufWriter.flush();
			Log.i("WRITER:","LINE ADDED BLIN..");
			bufWriter.close();
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

	}


	public String readFileFromSDCard(String fileName) {
		File fullPath = Environment.getExternalStoragePublicDirectory(fileName);
		File file = new File(fullPath, "TestFileAndroid.txt");

		if (!file.exists()) {
			throw new RuntimeException("File not found on sdcard");
		}
		BufferedReader reader = null;
		StringBuilder builder = new StringBuilder();
		try {
			reader = new BufferedReader(new FileReader(file));
			String line;
			while ((line = reader.readLine()) != null) {
				builder.append(line);
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return builder.toString();
	}
}
