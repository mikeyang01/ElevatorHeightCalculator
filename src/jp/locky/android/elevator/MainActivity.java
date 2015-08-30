package jp.locky.android.elevator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.UUID;

import jp.locky.android.elevator.sensing.SensingActivity;
import jp.locky.android.elevator.tutorial.TutorialActivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static String AppDir = "/data/data/jp.locky.android.elevator/";
	public static String serverIP = "http://elv.locky.jp:9000/";
	
	private static final String TAG = "[HelloWorld]";
	private String fileName;
	String UUID_ = null;
	private TextView usernameTV;
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.mainactivity);
		Log.d("[helloworld]"," main on create starts");
		/**
		 * check if is the first time start, if is the first time, create UUID,
		 * show the tutorial
		 */
		File storeUuid = new File(AppDir+"files/UUID.txt");
		if (storeUuid.exists()) {
			// not first time start
		} else {
			// ファイルにデータ保存
			try {
				fileName = "UUID.txt";
				FileOutputStream stream = openFileOutput(fileName, MODE_APPEND);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						stream));
				UUID uuid = UUID.randomUUID();
				out.write(uuid + "");
				out.close();
				//create unupload folder
				File fl = new File(AppDir+"/files/unupload");
				  /* このインスタンスからディレクトリを生成します。*/
				  fl.mkdir();
				
			} catch (Exception e) {
			}
			Intent intent = new Intent(MainActivity.this,TutorialActivity.class);
			startActivity(intent);
			finish();
		}
		/** read UUID */
		try {
			File read = new File(AppDir+"files/UUID.txt");
			Log.d("elevatorLog", "file exist? " + read.exists());
			BufferedReader br = new BufferedReader(new FileReader(read));
			UUID_ = br.readLine();
			Log.d(TAG,UUID_);
			br.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
			Log.d(TAG,"error: no uuid file");
		}
		/**read user name*/
		usernameTV= (TextView)findViewById(R.id.username_tv_firstactivity);
		try {
			File read = new File(AppDir+"files/username.txt");
			BufferedReader br = new BufferedReader(new FileReader(read));
			String username_ = br.readLine();			
			usernameTV.setText(username_+"さん");
			br.close();
			Log.d(TAG,"user name file is found"+username_);
		} catch (IOException e) {
			// e.printStackTrace();
			Log.d(TAG,"no username file");
			usernameTV.setVisibility(View.GONE);
		}
		
		//read user total distance
		int totalDistance =0;
		try{
		totalDistance = (int)Double.parseDouble(getTotalDistance());
		TextView totaldistanceTV = (TextView)findViewById(R.id.totalDistanceTV_firstactivity);
		totaldistanceTV.setText(totalDistance+getString(R.string.main_meter));
		}
		catch(Exception e){//ファイルにエラーがある場合、距離を０に変わる
			TextView totaldistanceTV = (TextView)findViewById(R.id.totalDistanceTV_firstactivity);
			totaldistanceTV.setText(totalDistance+"メートル");
		}
		
		Button buttonViewLocal = (Button) findViewById(R.id.buttonViewLocal);
		buttonViewLocal.setOnClickListener(new ViewLocal_ClickListener());
		
		Button buttonStart = (Button) findViewById(R.id.buttonStart);// button1
		buttonStart.setOnClickListener(new Start_ClickListener());
		
		Button buttonSettings = (Button) findViewById(R.id.buttonSettings);
		buttonSettings.setOnClickListener(new Settings_ClickListener());

		HeightRank hr = new HeightRank();
		TextView awardTV = (TextView)findViewById(R.id.awardTV_firstactivity);		
		
		awardTV.setText(hr.heightRank(totalDistance));		
		
	}

	class Start_ClickListener implements OnClickListener {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(MainActivity.this, SensingActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("uuid", UUID_);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
		}
	}	
	
	class Settings_ClickListener implements OnClickListener {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(MainActivity.this, Settings.class);
			Bundle bundle = new Bundle();
			bundle.putString("uuid", UUID_);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
		}
	}
	class ViewLocal_ClickListener implements OnClickListener {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(MainActivity.this,FileViewerActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("uuid", UUID_);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
		}
	}
	private String getTotalDistance(){
		String totalDistance = "0";
		File totalDistanceFile = new File(AppDir+"files/totaldistance.txt");
		
		if (totalDistanceFile.exists()) {
			//update user name 上書き
			try {								
				FileReader filereader = new FileReader(AppDir+"files/totaldistance.txt");
				BufferedReader bufferedreader = new BufferedReader(filereader);		
				totalDistance = bufferedreader.readLine();
				System.out.println("total distance :"+totalDistance);
				return totalDistance;
				
			} catch (Exception e) {
				System.out.println("get distance file error read");
			}
		} else {
			// ファイルにデータ保存
			try {				
				String fileName = "totaldistance.txt";
				FileOutputStream stream = openFileOutput(fileName, 0);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
						stream));
				out.write(totalDistance);
				out.close();
				System.out.println("total distance :"+totalDistance);
				return totalDistance;
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return totalDistance;
	}	
}