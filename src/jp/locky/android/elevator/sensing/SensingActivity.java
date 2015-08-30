package jp.locky.android.elevator.sensing;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import jp.locky.android.elevator.MainActivity;
import jp.locky.android.elevator.calculate.CalculatorActivity;

import jp.locky.android.elevator.R;


import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class SensingActivity extends Activity implements SensorEventListener {

	Timer timer = new Timer();
	private int recLen = 5;
	
	private WakeLock wakeLock;
	
	public static float G = SensorManager.GRAVITY_EARTH; // 9.80665;
	private Button mButton1;
	final String tag = "[HelloWorld]";
	// SensorManagerのインスタンス
	private SensorManager sensorManager;
	private String fileName;
	private String deviceId ="";
	private volatile boolean active_ = true;//this value is trying to stop the writing before delete the file.
	private GestureDetector detectorFinish;
	private GestureDetector detectorCancel;
	double[] varianceArray = new double[10];
	float gravityNorm=SensorManager.GRAVITY_EARTH;
	float delta = 0.0f;
	static final float alpha=0.95f;
	TextView xViewA,yViewA,zViewA, gViewA;
	TextView timeView;
	
	long startTime; 
	
	//FileOutputStream stream;
	BufferedWriter out=null;
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//画面がセンシング中で常にオンにする

		// initialize receiver
		//IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
		//filter.addAction(Intent.ACTION_SCREEN_OFF);
		//BroadcastReceiver mReceiver = new ScreenReceiver();
		//registerReceiver(mReceiver, filter);
		detectorFinish = new GestureDetector(this, new TapDetectorFinish());
		detectorCancel = new GestureDetector(this, new TapDetectorCancel());
		/** get device Id */
		Bundle bundle = getIntent().getExtras();
		deviceId = bundle.getString("uuid");
		String[] deviceIdChunks = deviceId.split("-");

		/**get system time and create file name*/
		long time = System.currentTimeMillis();
		Date date = new Date(time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		fileName = sdf.format(date) + "_" + deviceIdChunks[4] + ".csv";
		Log.d("elevatorLog",fileName);
		//fileName = "tmpFile.csv";
		sensorManager = (SensorManager) this.getSystemService(SENSOR_SERVICE);
		setContentView(R.layout.sensing);

		// return button
		mButton1 = (Button) findViewById(R.id.myButton1);
		mButton1.setOnTouchListener(new ReturnButtonClickListener());
		
		xViewA = (TextView) findViewById(R.id.xbox);// 利用する View は1つだけにする
//		yViewA = (TextView) findViewById(R.id.ybox);
//		zViewA = (TextView) findViewById(R.id.zbox);

		gViewA = (TextView) findViewById(R.id.gbox);		
		gViewA.setGravity(Gravity.RIGHT);

		/**after 5 seconds, a dialog alert will appear, you can choose save or not by double click */
		timer.schedule(task, 1000, 1000); // timeTask				
	}

	TimerTask task = new TimerTask() {
		@Override
		public void run() {

			runOnUiThread(new Runnable() { // UI thread
				public void run() {
					recLen--;
					// txtView.setText(""+recLen);
					if (recLen < 0) {
						timer.cancel();
						System.out.println("timer cancelled");
							
						TextView tv_nodata1 = new TextView(SensingActivity.this);
						TextView tv_nodata2 = new TextView(SensingActivity.this);							
						tv_nodata1.setText(" ");
						tv_nodata2.setText(R.string.sensing_getofftheelevator);
						tv_nodata2.setTextSize(20);
						Button finishElevatorBtn = new Button(SensingActivity.this);
						finishElevatorBtn.setText(R.string.sensing_yes);
						//finishElevatorBtn.setHeight(60);
						finishElevatorBtn.setOnTouchListener(new OnTouchListener() {
							public boolean onTouch(View v, MotionEvent event) {
								System.out.println("************* onTouch *************");
								detectorFinish.onTouchEvent(event);
								return true;
							}
						});
						
						LinearLayout layoutMain = (LinearLayout) findViewById(R.id.sensing_finishElevatorLayout);
						layoutMain.removeAllViews();				
						layoutMain.addView(tv_nodata1);
						layoutMain.addView(tv_nodata2);
						layoutMain.addView(finishElevatorBtn);// Show the page first				
					}
				}
			});
		}
	};
	
	// button クリックリスナー定義
	class ReturnButtonClickListener implements OnTouchListener {
		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			detectorCancel.onTouchEvent(event);
			return true;
		}
	}

	public void onSensorChanged(SensorEvent event) {
		synchronized (this) {
			if(out == null){ //initial call
				try{
					FileOutputStream stream = openFileOutput(fileName, MODE_APPEND);
					out = new BufferedWriter(new OutputStreamWriter(stream));
				}catch(Exception e){
					Log.d("Sense", "file open error");
				}
				startTime = event.timestamp;
			}
			
			float x, y, z;
			x = event.values[0];
			y = event.values[1];
			z = event.values[2];
		    float norm = (float) Math.sqrt((double) (x*x + y*y + z*z));
				
			String dataX = String.valueOf(x/ G);// 単位をiphoneに合わせる(G)
			String dataY = String.valueOf(y/ G);
			String dataZ = String.valueOf(z/ G);
			
			double timeSecond = event.timestamp;
			double prSecond = ((int)((event.timestamp-startTime)/100000000)/10.0);
			try{
				xViewA.setText("Time: " + prSecond+"   x:" + dataX.substring(0,5)+"  y:"+dataY.substring(0,5)+"  z:"+dataZ.substring(0,5));
			}catch(Exception e){}
//			yViewA.setText("y:" + dataY);
//			zViewA.setText("z:" + dataZ);
			float ldelta = gravityNorm-norm;
			delta = delta * alpha + ldelta*(1.0f-alpha);
			gravityNorm = gravityNorm*alpha+norm*(1.0f-alpha);
			String g = String.valueOf((int)(-delta*20.0)/20.0)+"00";
			if(((int)(-delta*20.0)/20.0) < 0) g = g.substring(0,5)+"  ";
			else g = g.substring(0,4)+"  ";
			gViewA.setText(g);
			

			// ファイルにデータ保存
			if (active_ && out != null) {
				try {
					out.write(timeSecond / 1000000000 + "," + dataX + "," + dataY + "," + dataZ);
					out.newLine();
					//System.out.println("file is writing..");
				} catch (Exception e) {
				}					
			}
		}
	}

	public void onAccuracyChanged(int sensor, int accuracy) {
		Log.d(tag, "onAccuracyChanged: " + sensor + ", accuracy: " + accuracy);
	}

	@Override
	protected void onResume() {
		acquireWakeLock();
		super.onResume();
		List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		// リスナーの登録
		for (Sensor s : sensors) {
			sensorManager.registerListener(this, s,SensorManager.SENSOR_DELAY_FASTEST);
		}
		// onSensorChanged 使用方法？？？
	}
	
	@Override
	protected void onPause() {		
		super.onPause();
		releaseWakeLock();
		// super.onDestroy();
	}

	@Override
	protected void onStop() {
		sensorManager.unregisterListener(this);
		if(out != null){
			try{
				out.close();
				Log.d("sense","out closed");
			}catch(Exception e){}
		}
		super.onStop();
	}

	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}
	/*
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	// TODO Auto-generated method stub
    	if(requestCode == 0 && resultCode == RESULT_OK) {
    		finish();
    	}
    }
    */
    @Override
    protected void onDestroy() {
    	// TODO Auto-generated method stub
    	super.onDestroy();
    	System.out.println("activity sensing onDestory");
    }
    
    //戻りボタン
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog alertDialog;
			alertDialog  = new AlertDialog.Builder(SensingActivity.this).create();
			alertDialog.setTitle(R.string.finishbutton_sensing);
			alertDialog	.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.finishbuttonyes_sensing),new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int whichButton) {
									timer.cancel();
									active_ = false;	
									File tmp = new File(MainActivity.AppDir+"files/"+fileName);
									/* ファイル(ディレクトリ)が存在するかどうか判定します。 */
									if (tmp.exists()) {
										Log.d("[helloworld]", "Sensing Activity: the file exists:"+fileName);
										System.out.println("file delete results: "+tmp.delete());
									} else {
										Log.d("[helloworld]", "Sensing Activity: no file exist:"+fileName);
									}			
									Intent intent = new Intent(SensingActivity.this,MainActivity.class);
									startActivity(intent);
									finish();
								}
							});
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,getString(R.string.finishbuttonno_sensing),new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int whichButton) {
									//do nothing
								}
							});			
			alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
		        public void onCancel(DialogInterface dialog) {
		        	//do nothing		
		        }  
		    });
			alertDialog.setCanceledOnTouchOutside(false);
			alertDialog.show();			
			return true;
		}
		return false;
	}
	
	private void acquireWakeLock() {  
	     if (wakeLock == null) {  
	            System.out.println("Acquiring wake lock");  
	            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);  
	            wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, this.getClass().getCanonicalName());  
	            wakeLock.acquire();  
	        }  	      
	}  
	  	  
	private void releaseWakeLock() {  
	    if (wakeLock != null && wakeLock.isHeld()) {  
	        wakeLock.release();  
	        wakeLock = null;  
	        System.out.println("release wake lock");
	    }   
	}  
	class TapDetectorFinish extends GestureDetector.SimpleOnGestureListener {
		@Override
		public void onLongPress(MotionEvent e) {
			System.out.println("************* onLongPress *************");
		}
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			System.out.println("************* onDoubleTap *************");
			sensorManager.unregisterListener(SensingActivity.this);
			if(out != null){
				try{
					out.close();
				}catch(Exception e0){}
			}
				
			Intent intent = new Intent(SensingActivity.this,CalculatorActivity.class);
			Bundle bundle = new Bundle();
			bundle.putString("fileName", fileName);
			bundle.putString("deviceId", deviceId);
			intent.putExtras(bundle);
			startActivity(intent);
			finish();
			return true;
		}	
	}
	
	class TapDetectorCancel extends GestureDetector.SimpleOnGestureListener {
		@Override
		public void onLongPress(MotionEvent e) {
			System.out.println("************* onLongPress *************");
		}
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			System.out.println("************* onDoubleTap *************");
			timer.cancel();
			active_ = false;	
			File tmp = new File(MainActivity.AppDir+"files/"+fileName);
			/* ファイル(ディレクトリ)が存在するかどうか判定します。 */
			if (tmp.exists()) {
				Log.d("[helloworld]", "Sensing Activity: the file exists:"+fileName);
				System.out.println("file delete results: "+tmp.delete());
			} else {
				Log.d("[helloworld]", "Sensing Activity: no file exist:"+fileName);
			}		
			
			Intent intent = new Intent(SensingActivity.this,MainActivity.class);
			startActivity(intent);
			finish();	
			
			return true;			
		}	
	}
	
}