package jp.locky.android.elevator.net.http;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import jp.locky.android.elevator.MainActivity;
import jp.locky.android.elevator.R;
import jp.locky.android.elevator.uti.CopyFile;
import jp.locky.android.elevator.uti.FileCompress;
import jp.locky.android.elevator.uti.NumberPickerDialog;
import jp.locky.android.elevator.uti.SaveToFile;
import jp.locky.android.elevator.uti.NumberPickerDialog.OnMyNumberSetListener;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


/**google play service import*/
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class UploadHttpActivity extends Activity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener {
	public String TAG ="uploadHttpActivity";
	
	DigitsKeyListener numericOnlyListener = new DigitsKeyListener(false, true);
	private TextView CoordinateTV;
	private TextView distanceTextView;
	private String buildingName_ ="";
	private String elevatorName_ ="";

	private String deviceId = null;
	private String deviceName = null;
	private String deviceBrand = null;
	
	private double samplingfrequency = 0;
	private double currentDistance = 0;
	private double maxVelocity = 0;
	private int segmentsNumber =0;
	private String segments = ""; //2-5-7 means starts from 2, stop at 5, end with 7  
    private double latitude = 0; 
    private double longitude = 0;
    private double longitudeMax = 0;
    private double longitudeMin =0;
    private double latitudeMax = 0;
    private double latitudeMin = 0;
    private String elevatorList ="";
    //additional message
	private String feeling = "";
    private String shake ="";
    private String capacity ="";
	private String manufacturer = "";
	private String memo = "";    
    
    EditText editTextStart;
    ArrayList<EditText> editTextTempList = new ArrayList<EditText>();
    EditText editTextStop;    
    
	private double accuracy =0;
	
	private double gravity =  0;
	private String fileName = null;
	private String PopUpBuildingText;
	private String PopUpElevatorText;
	Button uploadButton;
	Button storeLocalButton;
	
	//number picker
	int[] floorNumberArray;
	TextView[] floorNumber_tv;
	int floorCount;
	Button[] btnArray;//start middle end all buttons
	Button buildingListBtn;
	private String buildingListString = "";
	private String elevatorListString = "";
	//private String[] floorListArray;
	private TextView elevatorName_tv;
	
/**Google location API*/
	private GoogleApiClient mGoogleApiClient;   
	private LocationRequest mLocationRequest;
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		Log.d("elevatorLog", "upload on create");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.upload2);
		
/**Google location API*/
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();     
        
		final int servieCheck = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (servieCheck != ConnectionResult.SUCCESS) {
			Toast.makeText(this, "Google Play service is not available (status=" + servieCheck + ")", Toast.LENGTH_LONG).show();
		}
		elevatorName_tv = (TextView)findViewById(R.id.elevatorName_tv_upload);
		
		CoordinateTV = (TextView) findViewById(R.id.coordinateTV_upload);
		CoordinateTV.setText(getString(R.string.gettinglocation_upload));

		Bundle bundle = getIntent().getExtras();
		gravity = bundle.getDouble("gravity");		
		deviceId = bundle.getString("deviceId");
		currentDistance = bundle.getDouble("currentDistance");
		System.out.println("currentDistance:"+currentDistance);
		fileName = bundle.getString("fileName");	
		segmentsNumber = bundle.getInt("segmentsNumber");
		samplingfrequency = bundle.getDouble("samplingFrequency");
		maxVelocity = bundle.getDouble("maximumVelocity"); 
		distanceTextView = (TextView) findViewById(R.id.distanceTextView);
		distanceTextView.setText(currentDistance+getString(R.string.meters_upload));
		//fakeNumberを作らないと、エレベータ認識できないときのフロア数を入力できない
		int fakeSegNumber =0;
		if(segmentsNumber==0){
			fakeSegNumber=1;
		}else{
			fakeSegNumber=segmentsNumber;
		}
		
		//for number use
		System.out.println("segments number:"+segmentsNumber);
		floorNumberArray = new int[fakeSegNumber+1];//if 2 segments,input 3 floors		
		floorCount = fakeSegNumber+1;
		btnArray = new Button[floorCount];
		
		/** ---elevator floor info, sub page--- */
		// 縦方向
		LinearLayout llVertical = new LinearLayout(this);
		llVertical.setOrientation(LinearLayout.VERTICAL);
		// 横方向
		LinearLayout subHorizontalStart = new LinearLayout(this);
		subHorizontalStart.setGravity(Gravity.LEFT);		
		//開始フロアの生成
		TextView tvStart = new TextView(this);
		tvStart.setGravity(Gravity.CENTER);
		tvStart.setText(getString(R.string.startFloor_upload));//"開始フロア: ");
		btnArray[0] = new Button(this);
		btnArray[0].setText(getString(R.string.inputFloorNumber_upload));//"フロアの入力");		
		btnNumOnclickListener btnListener = new btnNumOnclickListener();
		btnListener.set(0);
		btnArray[0].setOnClickListener(btnListener);
		
		subHorizontalStart.addView(tvStart);
		subHorizontalStart.addView(btnArray[0]);
		
		llVertical.addView(subHorizontalStart);
		// tv.setWidth(200);
		if (fakeSegNumber > 1) {		//tmp means middle
			for (int i = 1; i < fakeSegNumber; i++) {
				LinearLayout subHorizontalTemp = new LinearLayout(this);
				TextView tvTemp = new TextView(this);
				tvTemp.setText(R.string.middleFloor_upload);//"途中で止まったフロア： ");				
				btnNumOnclickListener btnTmpListener = new btnNumOnclickListener();				
				btnTmpListener.set(i);
				btnArray[i] = new Button(this);
				btnArray[i].setOnClickListener(btnTmpListener);		
				btnArray[i].setText(getString(R.string.inputFloorNumber_upload));//"フロアの入力");				
				subHorizontalTemp.addView(tvTemp);
				subHorizontalTemp.addView(btnArray[i]);
				llVertical.addView(subHorizontalTemp);
			}
		}
		//到着フロアの生成
		LinearLayout subHorizontalStop = new LinearLayout(this);
		TextView tvStop = new TextView(this);
		tvStop.setText(R.string.endFloor_upload);//"到着フロア: ");		
		
		//tvStop.setHeight(50);
		tvStop.setGravity(Gravity.CENTER);
		tvStop.setText(getString(R.string.endFloor_upload));//"開始フロア: ");
		btnArray[fakeSegNumber] = new Button(this);
		btnArray[fakeSegNumber].setText(getString(R.string.inputFloorNumber_upload));//"フロアの入力");
				
		btnNumOnclickListener btnEndListener = new btnNumOnclickListener();
		btnEndListener.set(fakeSegNumber);//last elevator's No is segment number
		btnArray[fakeSegNumber].setOnClickListener(btnEndListener);
		
		subHorizontalStop.addView(tvStop);				
		subHorizontalStop.addView(btnArray[fakeSegNumber]);

		llVertical.addView(subHorizontalStop);
		
		// setContentView(llVertical);
		/** Add first page */
		LinearLayout layoutMain = (LinearLayout) findViewById(R.id.elevatorSpacelayout_upload);
		layoutMain.removeAllViews();
		layoutMain.addView(llVertical); // Show the page first

		getDeviceName();
		getDeviceBrand();

		Button addtionalButton = (Button) findViewById(R.id.upload_addtionalInfobutton);
		addtionalButton.setOnClickListener(new addtionalButtonClickListener());
		
		uploadButton = (Button) findViewById(R.id.upload_Button);
		uploadButton.setOnClickListener(new UploadButtonClickListener());

		storeLocalButton = (Button) findViewById(R.id.upload_storeLocal_Button);
		storeLocalButton.setOnClickListener(new StoreLocalButtonClickListener());
				
		buildingListBtn = (Button) findViewById(R.id.upload_buildingListBtn);
		buildingListBtn.setOnClickListener(new buildingListBtnCListener());
		buildingListBtn.setText(getString(R.string.collectBuilding_Btn_upload));
	}
		
    @Override
    protected void onStart() {
        super.onStart();
        // Connect the client.
        mGoogleApiClient.connect();
    }
    @Override
    protected void onStop() {
        // Disconnecting the client invalidates it.
        mGoogleApiClient.disconnect();
        super.onStop();
    }
	@Override
	protected void onPause() {
		super.onPause();
		mGoogleApiClient.disconnect();
	}
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection has been suspend");
    }
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "GoogleApiClient connection has failed");
    }
	@Override
	public void onConnected(Bundle arg0) {
		// check Google Play service APK is available and up to date. see http://developer.android.com/google/play-services/setup.html
		final int servieCheck = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (servieCheck != ConnectionResult.SUCCESS) {
			latitude = 0;
			longitude = 0;
			accuracy = 0;
			CoordinateTV = (TextView) findViewById(R.id.coordinateTV_upload);			
			CoordinateTV.setText(R.string.failed_gettinglocation_upload);
		}
		else{			
/**Google Location API*/		       			
	        mLocationRequest = LocationRequest.create();
	        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
	        mLocationRequest.setInterval(1000); // Update location every second
	        LocationServices.FusedLocationApi.requestLocationUpdates(
	                mGoogleApiClient, mLocationRequest, this);
			if(mLocationRequest==null){
				latitude = 0;
				longitude = 0;
				accuracy = 0;
				CoordinateTV = (TextView) findViewById(R.id.coordinateTV_upload);			
				CoordinateTV.setText(R.string.failed_gettinglocation_upload);
			}
		}
	}
	@Override
	public void onLocationChanged(Location location) {
		Log.d("XXX", "location=" + location.toString());
		Geocoder geocoder = new Geocoder(this);
		try {
			latitude = location.getLatitude();
			longitude = location.getLongitude();
			accuracy = location.getAccuracy();
			accuracy = (double)(Math.round(accuracy * 10)) / 10;
		
			List<Address> result = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
			CoordinateTV = (TextView) findViewById(R.id.coordinateTV_upload);			
			String locationText = "";
			if(result.size() == 0){
				System.out.println("null found!!!!!!!!!!!!!!!!!!!!!!!!!");
				CoordinateTV.setText(R.string.failed_gettinglocation_upload);
			}
			else {
				if (result.get(0).getCountryName() == null) {
					CoordinateTV.setText(R.string.failed_gettinglocation_upload);
				} else if (result.get(0).getAdminArea() == null) {
					locationText = result.get(0).getCountryName();
				} else if (result.get(0).getLocality() == null) {
					locationText = result.get(0).getCountryName()+ result.get(0).getAdminArea();
				} else {
					locationText = result.get(0).getCountryName()+ result.get(0).getAdminArea()+ result.get(0).getLocality();
				}
			}
			CoordinateTV.setText(locationText);
			System.out.println("latitude:"+latitude+" "+longitude+"accuracy:"+accuracy);			
		} catch (IOException e) {
			e.printStackTrace();	
		}	
        mGoogleApiClient.disconnect();
		getCoordinate();		
	}
	private class buildingListBtnCListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			createBuildingListAlertD();
		}		
	}
	//piker class
	private class btnNumOnclickListener implements OnClickListener{
		int num;
		void set(int tmp){
			num=tmp;
		}
		@Override
		public void onClick(View view) {
			showNumberPicker(floorNumberArray[num]+"", 0,num);
			System.out.println("num2:"+num);
		}
	}
	private void showNumberPicker(String number, int mode,int num) {
		String nowNumber = "0";//(String) floorNumber_tv[num].getText();
		if (!number.equals("")) {
			nowNumber = number;
		}
		onMyNumberSetListener listener = new onMyNumberSetListener();
		listener.set(num);
		System.out.println("num1:"+num);
		new NumberPickerDialog(this, listener, nowNumber, mode).show();
	}
	
	private class onMyNumberSetListener implements OnMyNumberSetListener{
		int num;
		void set(int tmp){
			num=tmp;
		}		
		@Override
		public void onNumberSet(String number, int mode) {// number is user pressed number
			//textViewCapacity.setText(number);			//num i segment number
			System.out.println("number:"+number);
			System.out.println("num:"+num);
			System.out.println("length:"+floorNumberArray.length);
			floorNumberArray[num] = Integer.parseInt(number);
			btnArray[num].setText(number);
		}
	}
	
	
	private class addtionalButtonClickListener implements OnClickListener{
		@Override
		public void onClick(View arg0) {
			Intent i = new Intent(UploadHttpActivity.this,AdditionalInfoShortActivity.class);	
			
			Bundle bundle = new Bundle();

			bundle.putString("feeling", feeling);
			bundle.putString("shake", shake);
			bundle.putString("capacity", capacity);
			bundle.putString("manufacturer", manufacturer);
			bundle.putString("memo", memo);
			i.putExtras(bundle);			
    		System.out.println("bundle send:"+feeling+shake+capacity+manufacturer+memo);
	    	startActivityForResult(i,10);
		}		
	} 
    protected void onActivityResult(int requestCode, int resultCode,Intent data){
    	switch (resultCode){		
    	case RESULT_OK:   				
    		Bundle b = data.getExtras(); 			
    		feeling = b.getString("feeling");
    		shake = b.getString("shake");
    		capacity =b.getString("capacity");
    		manufacturer = b.getString("manufacturer");
    		memo = b.getString("memo");
    		System.out.println("bundle get:"+feeling+shake+capacity+manufacturer+memo);
    	}
    }
	//エレベータを選択してください
	//example: 1)B1 2)C2 3)input elevator 
	//2 Possibilities for inputting the elevator:
	//(1)no building and no elevator: input building and then input elevator
	//(2)building exists but no this elevator: select building and then update elevator list  	

	public void getBuildingList() {
		GetBuildingListLoader loader =new GetBuildingListLoader();
		loader.execute();
	}
	
	private class GetBuildingListLoader extends AsyncTask<String, Integer, String> {
						
		@Override
		protected void onPreExecute() {
		}	
		@Override
		protected String doInBackground(String... params) {
			List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
			postData.add(new BasicNameValuePair("latitudeMax", latitudeMax+""));
			postData.add(new BasicNameValuePair("latitudeMin", latitudeMin+""));
			postData.add(new BasicNameValuePair("longitudeMax", longitudeMax+""));
			postData.add(new BasicNameValuePair("longitudeMin", longitudeMin+""));
			
			HttpMultiPost hmp = new HttpMultiPost(MainActivity.serverIP+"get_buildings2", postData);
			System.out.println("server ip:"+MainActivity.serverIP+"get_buildings2");
			
			String replyMessage = hmp.send();
			if(replyMessage ==null){
				replyMessage ="server error";
				System.out.println("server error, null message found");
			}
			System.out.println("reply message from get_buildings:"+replyMessage+",space problem found");
			System.out.println("reply message:"+replyMessage);
			return replyMessage;
		}
		
		@Override
		protected void onPostExecute(String replyMessage) {
			buildingListString = replyMessage;
						
			String buildingListTmp = replyMessage;
			System.out.println("建物リスト"+buildingListTmp);
			 //process response message	
			 final String[] buildingList = buildingListTmp.split(",");	
			 final int buildingNumber = buildingList.length;
			 buildingList[buildingNumber-1] = getString(R.string.insertBuilding_upload);//"建物を入力"
			 System.out.println("buildinglist_btn_upload");
			 buildingListBtn.setText(getString(R.string.buildinglist_btn_upload));
		}
	}
	
	private void createBuildingListAlertD(){
		final String[] buildingArray;
		if(!buildingListString.equals("")){
			buildingArray = buildingListString.split(",");	
		}else{
			buildingArray = new String[1];
			buildingArray[0] ="";
		}
		
		final int buildingNumber = buildingArray.length;
		buildingArray[buildingNumber-1] = getString(R.string.insertBuilding_upload);//"建物を入力";
	 
		//create alert dialog			
		new AlertDialog.Builder(this)
			.setItems(buildingArray, new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which) {
					System.out.println("selected:"+which);
					if(buildingArray[which].equals(getString(R.string.insertBuilding_upload))){
						//alertdialog
						System.out.println("begin other building");
						//insert other building
						Dialog_UserInputBuidling();						
					}else{					
						buildingName_ = buildingArray[which];
						buildingListBtn.setText(buildingName_);//ボタンの文字変換
						getElevators();
					}
				}
			}).show();	
	}
	
	public void getElevators(){
		//post the building name to the server, server replies a elevator list
		GetElevatorListLoader loader = new GetElevatorListLoader();
		loader.execute();			
	}
	
	private class GetElevatorListLoader extends AsyncTask<String, Integer, String> {
		
		//LinearLayout layoutText = (LinearLayout) findViewById(R.id.elevatorlistlayout_upload);					
		TextView tvElevatorInput = new TextView(UploadHttpActivity.this);		
		@Override
		protected String doInBackground(String... params) {
			List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
			postData.add(new BasicNameValuePair("buildingName", buildingName_));
			HttpMultiPost hmp = new HttpMultiPost(MainActivity.serverIP+"get_elevatorList", postData);
			String replyMessage = hmp.send();//when we get the message from the server, there is a SPACE after the data.--f0_0--			
			System.out.println("get elevator list reply message:"+replyMessage+"checkspace");
			return replyMessage;
		}
		@Override
		protected void onPostExecute(String replyMessage) {
			elevatorListString = replyMessage; 					
			selectElevatorAlertD();
		}
	
	}
	
	private void selectElevatorAlertD(){
		final String[] elevatorListArray = elevatorListString.split(",");
		elevatorListString=elevatorListString.replaceAll("\n|\r","");//splitを使うとき、","の後に内容がない場合はarrayに入らない
		System.out.println("get elevator list reply message rewrited:"+elevatorListString+"checkspace");
		//elevatorList = replyMessage;
		int elevatorAmount = elevatorListArray.length;
		// if no elevator list of this building, show "elevator input"
		elevatorListArray[elevatorAmount - 1] = getString(R.string.insertElevator_upload);
		
		new AlertDialog.Builder(this)
		.setItems(elevatorListArray, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				System.out.println("selected:"+which);
				if(elevatorListArray[which].equals(getString(R.string.insertElevator_upload))){
					//alertdialog
					System.out.println("begin other elevator");
					//insert other elevator
					DialogUserInputElevator_addOtherElevator();					
				}else{
					//selectedManufact = manufacturerArray[which];
					elevatorName_ = elevatorListArray[which];		
					elevatorName_tv.setText(elevatorName_);
				}
			}
		}).show();	
	}
	
	class StoreLocalButtonClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			//getEditText();
			getFloorList();
			storeLocalButton.setEnabled(false);
			//サーバーにつながってもデータをローカルに保存するように、.uploadedを追加
			Toast.makeText(UploadHttpActivity.this, getString(R.string.storeLocalToast_upload),Toast.LENGTH_SHORT).show();
			
			//まず、ファイルをunuploadフォルダーにコピーする
			storeLocalPostDataUnupload();

			Intent intent = new Intent(UploadHttpActivity.this,MainActivity.class);
			startActivity(intent);
			finish();
		}		
	}
	
	
	class UploadButtonClickListener implements OnClickListener {
		public void onClick(View v) {
			//getEditText();
			getFloorList();
			uploadButton.setEnabled(false);
			SendAllLoader loader = new SendAllLoader();
			loader.execute();
		}
	}
	
	//this one have process bar
	private class SendAllLoader extends AsyncTask<String, Integer, String> {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
		
	    private ProgressDialog dialog;
	    //LinearLayout loadinglayout = (LinearLayout)findViewById(R.id.loadinglayout_upload);
	    TextView loading_tv = new TextView(UploadHttpActivity.this); 
	    @Override
	    protected void onPreExecute() {
	    	loading_tv.setTextSize(20);
	    	loading_tv.setText(getString(R.string.loading_tv_upload));
	    	//loadinglayout.addView(loading_tv);
	    }
		
		@Override
		protected String doInBackground(String... params) {			
			System.out.println("upload info: "+deviceId+","+deviceBrand+","+deviceName+"\n"+
											   samplingfrequency+","+gravity+","+currentDistance+","+maxVelocity+"\n"+
											   latitude+","+longitude+","+buildingName_+"\n"+
											   elevatorName_+","+segmentsNumber+","+segments+","+fileName+"\n"+
											   feeling+","+shake+","+capacity+","+manufacturer+","+memo);
			//device info
			postData.add(new BasicNameValuePair("device_id", deviceId));
			postData.add(new BasicNameValuePair("device_brand", deviceBrand));
			postData.add(new BasicNameValuePair("device_type", deviceName));
			//calculated info
			postData.add(new BasicNameValuePair("frequency", samplingfrequency+""));
			postData.add(new BasicNameValuePair("gravity", gravity+""));			
			postData.add(new BasicNameValuePair("distance",currentDistance+""));
			postData.add(new BasicNameValuePair("top_speed", maxVelocity+""));
			//building and location info
			postData.add(new BasicNameValuePair("latitude", latitude+""));
			postData.add(new BasicNameValuePair("longitude", longitude+""));
			postData.add(new BasicNameValuePair("building_name", buildingName_));
			postData.add(new BasicNameValuePair("elevator_name", elevatorName_));
			postData.add(new BasicNameValuePair("segment_number", segmentsNumber+""));
			postData.add(new BasicNameValuePair("segments", segments));//startfloor-stopfloor-endfloor			
			postData.add(new BasicNameValuePair("file_name", fileName));
			//Additional info
			postData.add(new BasicNameValuePair("feeling", feeling));
			postData.add(new BasicNameValuePair("shake", shake));
			postData.add(new BasicNameValuePair("capacity", capacity));
			postData.add(new BasicNameValuePair("manufacturer", manufacturer));
			postData.add(new BasicNameValuePair("memo", memo));
			System.out.println("file name: "+fileName);
			StringBuilder builder = new StringBuilder();
			builder.append(MainActivity.AppDir+"files/").append(fileName);
			File fileTmp = new File(builder.toString());
			if (fileTmp.exists()) {
				Log.d("elevatorLog", "upload file exist");				
			} else {
				Log.d("elevatorLog", "no file exist "+fileName);
			}
			StringBuilder fileString = new StringBuilder();
			fileString.append(MainActivity.AppDir+"files/");
			fileString.append(fileName);
			
			//zip the file
			FileCompress fileCompress = new FileCompress();
			fileCompress.zip(fileString+"", fileString+".zip");
			
			// File fileUp = new File(fileString.toString());
			// fileTmp.renameTo(fileUp);
			//HttpElevatorPost hmpr = new HttpElevatorPost(MainActivity.serverIP+"main_submit2", postData, "csvFile",fileString + "");
			HttpElevatorPost hmpr = new HttpElevatorPost(MainActivity.serverIP+"main_submit2", postData, "csvFile",fileString+".zip");
			System.out.println("server address: "+MainActivity.serverIP);
			String replyMessage = hmpr.send()+"";//for this time, the reply message is the total distance
			Log.d("elevatorLog", "upload over");
			//after send the data, delete the local csv file.		
			return replyMessage;
		}

		@Override
		protected void onPostExecute(String replyMessage) {			
	        try {
	            dialog.dismiss();
	        } catch(Exception e) {
	        }
	        
			System.out.println("reply message:"+replyMessage);
			if (replyMessage.equals("null")) {//今回はequalsが正しく動作した。
				Toast.makeText(UploadHttpActivity.this, getString(R.string.ToastServerFailStoreLocal_upload),Toast.LENGTH_SHORT).show();
				//まず、ファイルをunuploadフォルダーにコピーする
				storeLocalPostDataUnupload();
				
			} else {
				Toast.makeText(UploadHttpActivity.this, getString(R.string.ToastServerSuccess_upload),Toast.LENGTH_SHORT).show();				
				// store the replyMessage in the totalDistance.txt
				// if reply message is null, we will not change the file
				if (replyMessage != null) {
					//サーバーにつながってもデータをローカルに保存するように、.uploadedを追加!!!	
					storeLocalPostDataUploaded();
					
					
					//distance.txtファイルを更新
					try {
						String fileName = "totaldistance.txt";
						FileOutputStream stream = openFileOutput(fileName, 0);
						BufferedWriter out = new BufferedWriter(
								new OutputStreamWriter(stream));
						out.write(replyMessage);
						out.close();
						System.out.println("get distance from server :"+ replyMessage);
					} catch (Exception e) {
						System.out.println("get distance file error update\n"+ e);
					}
				}
			}
			//delete censored file
			File tmp = new File(fileName);
			if (tmp.exists()) {
				System.out.println("file delete results: "+tmp.delete());
			} else {
				Log.d("elevatorLog", "after send no file exist");
			}
			
			//delete zip file 
			File tmpZip = new File(fileName+".zip");
			if (tmpZip.exists()) {
				System.out.println("file delete results: "+tmpZip.delete());
			} else {
				Log.d("elevatorLog", "after send no file exist");
			}
			
			Intent intent = new Intent(UploadHttpActivity.this,MainActivity.class);
			startActivity(intent);
			finish();
		}
	}
		
	//store post data (not uploaded)
	public void storeLocalPostDataUnupload(){
		CopyFile copyFile = new CopyFile();				
		String tmpStoreLocal = deviceId+","+deviceBrand+","+deviceName+","+samplingfrequency+","+gravity+","+
							   currentDistance+","+maxVelocity+","+latitude+","+longitude+","+buildingName_+","+
							   elevatorName_+","+segmentsNumber+","+segments+","+fileName+","+
							   feeling+","+shake+","+capacity+","+manufacturer+","+memo+"END";//without END, the "split" will ignore some of the data
		System.out.println("post data:"+tmpStoreLocal);
		//次、postDataを保存する
		SaveToFile saveToFile = new SaveToFile();
		saveToFile.saveToFile(tmpStoreLocal+"", MainActivity.AppDir+"files/unupload/"+fileName+".postdata");//here is the difference
		try {
			copyFile.copyFile(MainActivity.AppDir+"files/"+fileName, MainActivity.AppDir+"files/unupload/"+fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("move file over:"+fileName);		
	}
	
	//store post data uploaded
	public void storeLocalPostDataUploaded(){
		CopyFile copyFile = new CopyFile();				
		String tmpStoreLocal = deviceId+","+deviceBrand+","+deviceName+","+samplingfrequency+","+gravity+","+
				   			   currentDistance+","+maxVelocity+","+latitude+","+longitude+","+buildingName_+","+
				   			   elevatorName_+","+segmentsNumber+","+segments+","+fileName+","+
				   			   feeling+","+shake+","+capacity+","+manufacturer+","+memo+"END";
		System.out.println("post data:"+tmpStoreLocal);
		//次、postDataを保存する
		SaveToFile saveToFile = new SaveToFile();
		saveToFile.saveToFile(tmpStoreLocal+"", MainActivity.AppDir+"files/unupload/"+fileName+".postdata.unploaded");//here is the difference
		try {
			copyFile.copyFile(MainActivity.AppDir+"files/"+fileName, MainActivity.AppDir+"files/unupload/"+fileName);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("move file over:"+fileName);		
	}
	//get coordinate	
	public void getCoordinate(){
		float[] longitudeResults = new float[3];
		//startLatitude, startLongitude, endLatitude, endLongitude, results
		Location.distanceBetween(latitude, longitude, latitude, longitude+0.0001, longitudeResults);
		System.out.println("location1 distance:"+longitudeResults[0]+" "+longitude+" "+(longitude+0.0001));
		float[] latitudeResults = new float[3];
		Location.distanceBetween(latitude, longitude, latitude+0.0001, longitude, latitudeResults);
		System.out.println("location2:"+latitudeResults[0]);	
		if(accuracy ==0 ){
			getBuildingList();
			
		}else {
			//多くの携帯端末の精度がよくないので、精度は100以内のとき、300に拡大し、2000を超えると、正しくない
			double tmpAccuracy = 0;
			if(accuracy<100){
				tmpAccuracy = accuracy;
			}else if(accuracy>2000){
				tmpAccuracy =2000;
			}else {
				tmpAccuracy = accuracy;
			}			
			//0.0001/x=results[0]/accuracy
			double latitudeDiffer = 0.0001*tmpAccuracy/latitudeResults[0];
			double longitudeDiffer = 0.0001*tmpAccuracy/longitudeResults[0];
			latitudeMin = latitude - latitudeDiffer;
			latitudeMax = latitude + latitudeDiffer;
			longitudeMin = longitude - longitudeDiffer;
			longitudeMax = longitude + longitudeDiffer;
		
			latitudeMin = (double)Math.round(latitudeMin*100000)/100000;//４ケタ
			latitudeMax = (double)Math.round(latitudeMax*100000)/100000;//４ケタ
			longitudeMin = (double)Math.round(longitudeMin*100000)/100000;//４ケタ
			longitudeMax = (double)Math.round(longitudeMax*100000)/100000;//４ケタ
			
			System.out.println("differ:"+latitudeDiffer+" "+longitudeDiffer+" coordinate x,y:"+latitude+" "+longitude);
			System.out.println("coordinate range:"+latitudeMax+" "+latitudeMin+" "+longitudeMax+" "+longitudeMin);
			//get building list
			getBuildingList();	
			
		}
	}
	
	private void getDeviceName() {
	//	textViewDeviceName = (TextView) findViewById(R.id.TerminalTypeTextView);
		deviceName = android.os.Build.MODEL;
	//	textViewDeviceName.setText(deviceName);
	}

	private void getDeviceBrand() {
	//	textViewBrand = (TextView) findViewById(R.id.TerminalBrandTextView);
		deviceBrand = android.os.Build.BRAND;
	//	textViewBrand.setText(deviceBrand);
	}

	private void Dialog_UserInputBuidling() {
		final EditText PopUpEditText = new EditText(this);
		final AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setTitle(getString(R.string.titleInBuilding_upload)).setView(PopUpEditText)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						System.out.println("建築を入力。。。。2");
						PopUpBuildingText = PopUpEditText.getText().toString();
						PopUpBuildingText=PopUpBuildingText.replaceAll(","," ");//splitを使うとき、","がある場合は削除
						//Toast.makeText(UploadHttpActivity.this, PopUpBuildingText+"を入力しました", Toast.LENGTH_SHORT).show();
						buildingName_ = PopUpBuildingText;
						
						buildingListBtn.setText(buildingName_);//ボタンの文字変換
												
						//post new building and coordinate to table block
						//1,collect building and location info(coordinate,accuracy)
						//2,create building name and coordinate pairs
						//3,send info	
						//caution: this time only send one coordinate					
						DialogUserInputElevator_noBuilding();//!!					
					}
				}).create();
		PopUpEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					alertDialog
							.getWindow()
							.setSoftInputMode(
									WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});
		alertDialog.show();
	}
	//エレベータを入れてください！
	//no this building or elevator information
	private void DialogUserInputElevator_noBuilding(){
		final EditText PopUpEditText = new EditText(this);
		final AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setMessage(getString(R.string.titleInElevator_upload)).setView(PopUpEditText)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {						
						PopUpElevatorText = PopUpEditText.getText().toString();
						if(PopUpElevatorText.equals("")){
							PopUpElevatorText = getString(R.string.OnlyOneEle_upload); 
						}
						Log.d(TAG,PopUpElevatorText);
						//Toast.makeText(UploadHttpActivity.this, PopUpElevatorText+"を入力しました", Toast.LENGTH_SHORT).show();						
						elevatorName_ = PopUpElevatorText;
						
						elevatorName_tv.setText(elevatorName_);
						//start to post the elevator and building info
						Log.d(TAG,"post data:"+latitude+","+longitude+","+PopUpBuildingText+","+elevatorName_);	
						//サーバーに、squareとbuilding listを更新
						
						InputNoBuildingLoader inputNoBuildingLoader = new InputNoBuildingLoader();
						inputNoBuildingLoader.execute();
									
					}
				}).create();
		PopUpEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					alertDialog
							.getWindow()
							.setSoftInputMode(
									WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});
		alertDialog.show();
	}
	private class InputNoBuildingLoader extends AsyncTask<String, Integer, String> {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
		
	    @Override
	    protected void onPreExecute() {
	    }
		
		@Override
		protected String doInBackground(String... params) {			
			postData.add(new BasicNameValuePair("latitude", latitude+""));
			postData.add(new BasicNameValuePair("longitude", longitude+""));			
			postData.add(new BasicNameValuePair("building_name", PopUpBuildingText));
			postData.add(new BasicNameValuePair("elevator_name", elevatorName_+","));//"," is required to add another
			if(elevatorName_.equals("")){
				elevatorName_ = getString(R.string.OnlyOneEle_upload);
			}
			HttpMultiPost hmp = new HttpMultiPost(MainActivity.serverIP+"insert_building2", postData);
			String replyMessage = hmp.send();
			return replyMessage;
		}
		@Override
		protected void onPostExecute(String replyMessage) {	
			if(replyMessage==null){
				//Toast.makeText(UploadHttpActivity.this, "サーバにつながりませんでした", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(UploadHttpActivity.this,"Building info is uploaded", Toast.LENGTH_SHORT).show();
			}
		}	
	}
	
	//in this building, we have elevators, but do not have this one
	private void DialogUserInputElevator_addOtherElevator(){
		final EditText PopUpEditText = new EditText(this);
		final AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setMessage(getString(R.string.titleInElevator_upload)).setView(PopUpEditText)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {						
						elevatorName_ = PopUpEditText.getText().toString();
						elevatorName_=elevatorName_.replaceAll(","," ");//splitを使うとき、","がある場合は削除
						if(elevatorName_.equals("")){
							//this maybe a error need to solve in future version.
						}
						System.out.println("dialog user input elevator, add other elevator:"+elevatorList+elevatorName_);
						//Toast.makeText(UploadHttpActivity.this, elevatorName_+"を入力しました", Toast.LENGTH_SHORT).show();						
						//elevatorName_ = PopUpElevatorText;
						
						elevatorName_tv.setText(elevatorName_);
						//building list に更新する						
						InputElevatorLoader inputElevatorLoader = new InputElevatorLoader();
						inputElevatorLoader.execute();						
						//1,search building in building table
						//2,add post new elevator
						System.out.println("before string builder:"+elevatorList);
						//StringBuilder sb = new StringBuilder();
						//sb.append(elevatorList).append(elevatorName_).append(",");												
					}
				}).create();
		PopUpEditText.setOnFocusChangeListener(new OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					alertDialog
							.getWindow()
							.setSoftInputMode(
									WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
				}
			}
		});
		alertDialog.show();
	}
	//建物情報がデータベースにあり、エレベータがない場合
	private class InputElevatorLoader extends AsyncTask<String, Integer, String> {
		List<NameValuePair> postData = new ArrayList<NameValuePair>(2);		
	    @Override
	    protected void onPreExecute() {
	    }
		@Override
		protected String doInBackground(String... params) {			
			postData.add(new BasicNameValuePair("buildingname", PopUpBuildingText));
			postData.add(new BasicNameValuePair("elevatorname", elevatorList+elevatorName_+","));
			System.out.println("check elevator list before upload:"+PopUpBuildingText+","+elevatorList+elevatorName_+",");
			HttpMultiPost hmp = new HttpMultiPost(MainActivity.serverIP+"insert_elevator2", postData);			
			String replyMessage = hmp.send();

			return replyMessage;
		}		
		@Override
		protected void onPostExecute(String replyMessage) {	
			if(replyMessage==null){
				//Toast.makeText(UploadHttpActivity.this, "サーバにつながりませんでした", Toast.LENGTH_SHORT).show();
			}
			else{
				Toast.makeText(UploadHttpActivity.this,replyMessage, Toast.LENGTH_SHORT).show();
			}
		}
	}
	//put floor number into segments 
	public void getFloorList(){
		for(int i=0;i<floorCount;i++){
			//System.out.println("i:"+i);
			if(i==(floorCount-1)){//last floor
				segments = segments+floorNumberArray[i];
			}
			else{
				segments = segments+floorNumberArray[i]+"-";
			}
		}
		System.out.println("segemtns:"+segments);
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog alertDialog;
			alertDialog  = new AlertDialog.Builder(UploadHttpActivity.this).create();
			alertDialog.setTitle(R.string.finishbutton_upload);
			alertDialog	.setButton(AlertDialog.BUTTON_POSITIVE,getString(R.string.finishbuttonyes_upload),new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,int whichButton) {
									File tmp = new File(MainActivity.AppDir+"files/"+fileName);
									/* ファイル(ディレクトリ)が存在するかどうか判定します。 */
									if (tmp.exists()) {
										Log.d("[helloworld]", "Sensing Activity: the file exists:"+fileName);
										System.out.println("file delete results: "+tmp.delete());
									} else {
										Log.d("[helloworld]", "Sensing Activity: no file exist:"+fileName);
									}			
									Intent intent = new Intent(UploadHttpActivity.this,MainActivity.class);
									startActivity(intent);
									finish();
								}
							});
			alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE,getString(R.string.finishbuttonno_upload),new DialogInterface.OnClickListener() {
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
	public void setListViewHeightBasedOnChildren(ListView listView) {
		ListAdapter listAdapter = listView.getAdapter();
		if (listAdapter == null) {
			return;
		}

		int totalHeight = 0;
		for (int i = 0; i < listAdapter.getCount(); i++) {
			View listItem = listAdapter.getView(i, null, listView);
			listItem.measure(0, 0);
			totalHeight += listItem.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight+ (listView.getDividerHeight() * (listAdapter.getCount() - 1));
		//((MarginLayoutParams) params).setMargins(10, 10, 10, 10);
		listView.setLayoutParams(params);
	}
}
