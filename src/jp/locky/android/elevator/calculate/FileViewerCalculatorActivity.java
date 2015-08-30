package jp.locky.android.elevator.calculate;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import jp.locky.android.elevator.MainActivity;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import jp.locky.android.elevator.R;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class FileViewerCalculatorActivity extends Activity {
	private double gravity = 0;
	private int segmentsNumber = 0;// how many elevator segments
	private String fileName;
	private double[] distanceArray;
	private double[] velocityArray;
	private String deviceId;
	private int count2 = 0;
	private int fileLength;
	private double currentDistance = 0;//the total distance of all sets of current space recognition
	private double maximumVelocity =0;
	private double samplingFrequency=0;
	private int elevatorMay = 0;
	private ArrayList<Double> velocityList = new ArrayList<Double>();
	
	static final int SCOUNT=5; // グラフ表示の間引きの割合

	//recognition strategy1
	//static private int mpFilterWindowSize = 100;//この値が大きい場合、エレベータ認識できない
	//static private int gravityWindowSize = 120;//この値が小さい場合、エレベータ区間を重力として認識
	//static int varianceSize =20;
	//static double varTheshold=1.0E-4;
	
	//recognition strategy2
	static private int mpFilterWindowSize = 100;//この値が大きい場合、エレベータ認識できない
	static private int gravityWindowSize = 100;//この値が小さい場合、エレベータ区間を重力として認識
	static int varianceSize =5;
	static double varTheshold=2.0E-4;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);// スーパークラスのonCreateメソッド呼び出し
		Log.v("[HelloWorld]", "after oncreate");// レイアウト設定ファイルの指定
		setContentView(R.layout.fileviewercalculateresults);
		Bundle bundle = getIntent().getExtras();
		
		fileName = bundle.getString("fileName");//ここを直すことで、読み込みファイルを変換
		//fileName = "acctmp.csv";//test model
		deviceId = bundle.getString("deviceId");
		
		//エレベータの計算を始まるAsynTask
		calculateEntrance();
		double startTime=0.0f;
		
		//view the elevator data
		ArrayList<GraphViewData> gvd_list = new ArrayList<GraphViewData>();		
		try {
			File read = new File(MainActivity.AppDir+"files/"+fileName);//read csvdata
			System.out.println("--graph view file exist? :"+read.exists());
			if(read.exists()){
				BufferedReader br = new BufferedReader(new FileReader(read));
				String eachline;
				int counter=0;
				double nsum=0.0f;
				while ((eachline = br.readLine()) != null) {
					String[] d = eachline.split(",");
					double x,y,z;
					if(startTime == 0.0f){
						startTime = Double.parseDouble(d[0]);
					}
//					t = Double.parseDouble(d[0]);
					x = Double.parseDouble(d[1]);      
					y = Double.parseDouble(d[2]);      
					z = Double.parseDouble(d[3]);
				    float norm = (float) Math.sqrt((double) (x*x + y*y + z*z));
				    nsum+=norm;
				    counter ++;
				    if(counter >=SCOUNT){
				    	nsum /= SCOUNT;
				    	counter = 0;				    	
						GraphViewData gvd = new GraphViewData(((int) ((Double.parseDouble(d[0])-startTime)*10))/10.0,(int)(nsum*100)/100.0 );
						gvd_list.add(gvd);
						nsum=0;
				    }
				}
				br.close();
			}else{// no file.
				
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		GraphViewData[] gvd_array = new GraphViewData[gvd_list.size()];
		for (int i = 0; i < gvd_list.size(); i++) {
			gvd_array[i] = gvd_list.get(i);
		}
		GraphViewSeries exampleSeries1 = new GraphViewSeries(gvd_array);
		// context,heading
		LineGraphView graphView1 = new LineGraphView(this, "");
		
		graphView1.addSeries(exampleSeries1); // data
		
		LinearLayout layout1 = (LinearLayout) findViewById(R.id.linearlayout_graph);
		layout1.addView(graphView1);				
		
		Button backToFileListBtn = (Button) findViewById(R.id.BackToFileViewButton);
		backToFileListBtn.setOnClickListener(new backToFileListBtnClickListener());
	}
	//ユーザの体験を上がるために、計算をAsynTaskで行う
	public void calculateEntrance(){
		CalcuateLoader calculateLoader = new CalcuateLoader();
		calculateLoader.execute();
	}
	private class CalcuateLoader extends AsyncTask<String, Integer, String>{
				
		LinearLayout calcResultLayout = (LinearLayout)findViewById(R.id.calculate_calculateResultsLayout);
		TextView tv_waiting = new TextView(FileViewerCalculatorActivity.this);
		TextView tv_calcResults = new TextView(FileViewerCalculatorActivity.this);	
		@Override
		protected void onPreExecute() {
			// Initialize progress and image
			tv_waiting.setText(getString(R.string.calculated_recoging));
			tv_waiting.setTextSize(20);
			calcResultLayout.addView(tv_waiting);
		}
		@Override
		protected String doInBackground(String... arg0) {
			// TODO Auto-generated method stub
			try {
				File testFile = new File(fileName);
				System.out.println("--before elevator_main starts csv file exist?"+testFile.exists());
				//process data
				getFileLength(fileName);
				elevator_main(fileName);
			} catch (Exception e) {
				System.out.println(e);
				System.out.println("do in background file read error in elevator_main");
			}								
			return null;
		}
		@Override
		protected void onPostExecute(String replyMessage) {
			calcResultLayout.removeAllViews();
			if (segmentsNumber > 0) {
				distanceArray[0]=convertToTwoDecimal(distanceArray[0]);
				velocityArray[0]=convertToTwoDecimal(velocityArray[0]);							
				tv_calcResults.setText(getString(R.string.calcualted_EleSeg)+segmentsNumber+getString(R.string.calculated_times)+
										  ((double)((int)(currentDistance*100))/100)+getString(R.string.calculated_speed)+
										  ((double)((int)(maximumVelocity*100))/100)+getString(R.string.calculated_unit));
				System.out.println("segments number:"+segmentsNumber);
				//System.out.println("velocity array:"+velocityArray[0]);
				Log.d("elevatorLog", distanceArray[0] + "");
				tv_calcResults.setTextSize(26);
				calcResultLayout.addView(tv_calcResults);
			}
			else{//elevator recognition failed		
				if(elevatorMay>0){
					segmentsNumber = elevatorMay;
					tv_calcResults.setText(getString(R.string.calcualted_EleSeg)+elevatorMay+getString(R.string.calculated_times)+
							  ((double)((int)(currentDistance*100))/100)+getString(R.string.calculated_speed)+
							  ((double)((int)(maximumVelocity*100))/100)+getString(R.string.calculated_unit));
					tv_calcResults.setTextSize(26);
					calcResultLayout.addView(tv_calcResults);
					/*tv_calcResults.setText("エレベータ乗る回数:"+elevatorMay+"\n"+
							"移動距離を推定できませんでした。\n"+
							  "データをアップデートしてください。");
								tv_calcResults.setTextSize(18);
								calcResultLayout.addView(tv_calcResults);
								*/
				}else{
				tv_calcResults.setText(getString(R.string.calculated_unrecog));
				tv_calcResults.setTextSize(18);
				calcResultLayout.addView(tv_calcResults);
				}				
			}
		}
	}	

	class backToMainClickListener implements OnClickListener {
		public void onClick(View arg0) {
			Intent intent = new Intent(FileViewerCalculatorActivity.this,MainActivity.class);
			startActivity(intent);
			finish();
		}
	}
	public void getFileLength(String filePath) throws IOException{
		int countLength =0;
		System.out.println("--elevator main starts--");
		FileInputStream stream1 = openFileInput(filePath);
		BufferedReader bufferedreader_data1 = new BufferedReader(new InputStreamReader(stream1));

		while ((bufferedreader_data1.readLine()) != null) {
			countLength++;
		}
		fileLength = countLength;// file length starts from 1!!
	}
	
	public double[] elevator_main(String filePath) throws Exception {
		FileInputStream stream2 = openFileInput(filePath);
		BufferedReader bufferedreader_data2 = new BufferedReader(new InputStreamReader(stream2));

		double time1_double = 0;
		double acc1_x_double = 0;
		double acc1_y_double = 0;
		double acc1_z_double = 0;
		String line_data;

		double[] acc1NormDouble = new double[fileLength];

		double[] timeArray = new double[fileLength];
		while ((line_data = bufferedreader_data2.readLine()) != null) {
			String[] line_array_data1 = line_data.split(",");
			String time1 = line_array_data1[0];
			String acc1_x = line_array_data1[1];
			String acc1_y = line_array_data1[2];
			String acc1_z = line_array_data1[3];

			time1_double = Double.parseDouble(time1);
			acc1_x_double = Double.parseDouble(acc1_x);
			acc1_y_double = Double.parseDouble(acc1_y);
			acc1_z_double = Double.parseDouble(acc1_z);
			acc1NormDouble[count2] = Math.sqrt(acc1_x_double * acc1_x_double + acc1_y_double * acc1_y_double + acc1_z_double * acc1_z_double);
			timeArray[count2] = time1_double;
			count2++;
		}
		//first, we have to check the sampling rate, if the sampling rate is too low, we need to update to 100hz
		samplingFrequency = 100*1/(timeArray[200]-timeArray[100]);
		System.out.println("sampling frequency:"+samplingFrequency);
		if(samplingFrequency>95){
			//the sampling frequency is good, continue to calculate
		}
		else{
			Interpolation interpolation = new Interpolation();
			interpolation.linearInterpolation(acc1NormDouble, timeArray);
			int iLength = interpolation.idataList.size();
			//update array
			acc1NormDouble = new double[iLength];
			timeArray = new double[iLength];
			for(int i=0;i<iLength;i++){
				acc1NormDouble[i] = interpolation.idataList.get(i);
				timeArray[i] = interpolation.iTimeList.get(i);
			}
			fileLength = iLength;
		}
		
		/**next version is going to think about the different gravity calculate
		 * the distance using the data before the elevator and after the
		 * elevator ,about 2~3 seconds is still,
		 */
		/** get the accurate gravity */
		// MinusGravity mg = new MinusGravity();
		// double accurateGravity = mg.getGravity(acc1NormDouble,60,fileLength);//data,gravityWindowSize!!!
		/** complex version */
		System.out.println("--elevator main 1--before start minus gravity");
		MinusGravityVar mgv = new MinusGravityVar();
		double accurateGravity = mgv.gravityMain(acc1NormDouble, gravityWindowSize,fileLength);// here set the window size !!!!
		gravity = accurateGravity;
		double[] arrayNoGravity = new double[fileLength];// Gravity
		System.out.println("--elevator main--2");
		int i;
		for (i = 0; i < fileLength; i++) {
			// arrayNoGravity[i] = (acc1NormDouble[i]-0.994566304114127)*9.80665;
			arrayNoGravity[i] = acc1NormDouble[i] * 9.80665 - 9.80665* accurateGravity;
		}
		//if is not 0, beforeWindowData can not be initialized
		arrayNoGravity[0] = 0;

		arrayNoGravity = dealWithLastPartNoise(arrayNoGravity, 100);// !!!!!

		arrayNoGravity = minusAndPlusFilter(arrayNoGravity, mpFilterWindowSize);// !!!!!
		//output arrayNoGravity 
		
		storeArrayNoGravity(arrayNoGravity);
		
		spaceRecognition(arrayNoGravity, timeArray);
		System.out.println("current distance:"+currentDistance);
		count2 = 0;
		fileLength = 0;		
		return arrayNoGravity;		
	}

	/**
	 * minus and plus filter "before window begin" is needed
	 * */
	public double[] minusAndPlusFilter(double[] arrayNoGravity, int windowSize) {
		System.out.println("--minusAndPlusFilter starts--");
		int i;
		int j = 1;
		int k;
		/** train filter head||trail, the train is running against the head */
		double beforeWindowData = 0;
		double[] dataInWindow = new double[windowSize];
		// Initialization, because the memory is not cleaned, easy to view the
		// debug
		for (i = 0; i < windowSize; i++) {
			dataInWindow[i] = 1;
		}

		for (i = 1; i < fileLength - windowSize; i++) {// read whole csv
			beforeWindowData = arrayNoGravity[i - 1];
			// read whole window,number of j is the same as i
			for (j = i; j < i + windowSize; j++) {
				int plusData = 0;
				int minusData = 0;
				int zeroData = 0;
				for (k = 0; k < windowSize; k++) {
					dataInWindow[k] = arrayNoGravity[i + k];
					if (dataInWindow[k] > 0) {
						plusData++;
					} else if (dataInWindow[k] < 0) {
						minusData++;
					} else if (dataInWindow[k] == 0) {
						zeroData++;
					} else {
						// System.out.println("error data was found"+arrayNoGravity[i]);
					}
				}

				if (plusData == 0 && minusData == 0 && zeroData == 0) {
					// System.out.println("error: no data");
				}

				else if (plusData > 0 && minusData > 0 && zeroData == 0) {
					if (beforeWindowData * arrayNoGravity[i] <= 0) {
						arrayNoGravity[i] = 0;
					}
				} else if (plusData == 0 && minusData > 0 && zeroData > 0) {
					if (beforeWindowData * arrayNoGravity[i] <= 0) {
						arrayNoGravity[i] = 0;
					}
				} else if (plusData > 0 && minusData == 0 && zeroData > 0) {
					if (beforeWindowData * arrayNoGravity[i] <= 0) {
						arrayNoGravity[i] = 0;
					}
				} else if (plusData > 0 && minusData > 0 && zeroData > 0) {
					if (beforeWindowData * arrayNoGravity[i] <= 0) {
						arrayNoGravity[i] = 0;
					}
				} else if (plusData == 0 && minusData == 0 && zeroData == 0) {
					if (beforeWindowData * arrayNoGravity[i] <= 0) {
						arrayNoGravity[i] = 0;
					}
				}
			}
		}
		return arrayNoGravity;
	}

	/** Cancel the Noise in last window */
	public double[] dealWithLastPartNoise(double arrayNoGravity[], int trainSize) {
		System.out.println("--dealWithLastPartNoise starts--");
		for (int j = fileLength - trainSize; j < fileLength; j++) {
			arrayNoGravity[j] = 0;
		}
		return arrayNoGravity;
	}

	/** count by 1 set ,train algorithm [0][1] * */
	public void spaceRecognition(double arrayNoGravity[], double timeArray[]) {		
		/**
		 * one set of elevator data compose of
		 * "hillHead","hillTail","valleyHead","valleyTail" if there are 4 sets
		 * of elevator,there will be hillHead's size will be 4!
		 * 
		 * "hillHead" 1 2 3 4 "hillTail" 1 2 3 4 "valleyHead" 1 2 3 4
		 * "valleyTail" 1 2 3 4
		 * */
		System.out.println("--spaceRecognition starts--");
		String storeResults = null;
		List<Integer> hillHead = new ArrayList<Integer>();// store the index
		List<Integer> hillTail = new ArrayList<Integer>();
		List<Integer> valleyHead = new ArrayList<Integer>();
		List<Integer> valleyTail = new ArrayList<Integer>();

		for (int i = 0; i < fileLength - 1; i++) {
			if (arrayNoGravity[i] == 0 && arrayNoGravity[i + 1] > 0) {
				hillHead.add(i + 1);
			} else if (arrayNoGravity[i] > 0 && arrayNoGravity[i + 1] == 0) {
				hillTail.add(i);
			} else if (arrayNoGravity[i] == 0 && arrayNoGravity[i + 1] < 0) {
				valleyHead.add(i + 1);
			} else if (arrayNoGravity[i] < 0 && arrayNoGravity[i + 1] == 0) {
				valleyTail.add(i);
			} else if (arrayNoGravity[i] > 0 && arrayNoGravity[i + 1] < 0) {
				hillTail.add(i);
				valleyHead.add(i + 1);
			} else if (arrayNoGravity[i] < 0 && arrayNoGravity[i + 1] > 0) {
				valleyTail.add(i);
				hillHead.add(i + 1);
			}
		}
		// verify the elevator's numbers//山と谷のセットが同じ
		if ((hillHead.size() == hillTail.size())&& (valleyHead.size() == valleyTail.size())&& (hillHead.size() == valleyHead.size()) &&hillHead.size()!=0){
			storeResults = "numberOfSets:OK" + "," + valleyHead.size()+ ",times";
			segmentsNumber = valleyHead.size();
			Log.d("elevatorLog", "segment number: " + segmentsNumber);
			distanceArray = new double[segmentsNumber];
			velocityArray = new double[segmentsNumber];
			// calculate each set of distance
			for (int k = 0; k < segmentsNumber; k++) {
				if (hillHead.get(k) < valleyHead.get(k)) {
					distanceArray[k] = distanceCalculator(arrayNoGravity,timeArray, hillHead.get(k), valleyTail.get(k));
					velocityArray[k] = velocityCalculator(arrayNoGravity, timeArray, hillHead.get(k), hillTail.get(k));
					// storeResults = storeResults + "," + (k + 1)+
					// ",UpDistance:," + aSetOfDistance;
				} else if (hillHead.get(k) > valleyHead.get(k)) {
					distanceArray[k] = distanceCalculator(arrayNoGravity,timeArray, valleyHead.get(k), hillTail.get(k));
					velocityArray[k] = velocityCalculator(arrayNoGravity, timeArray, hillHead.get(k), hillTail.get(k));
					// storeResults = storeResults + "," + (k + 1) +
					// ",DownDistance:," + aSetOfDistance;
				}
			}
			//各segmentsの距離の統計を出す
			for(int i=0;i<segmentsNumber;i++){
				currentDistance = currentDistance + distanceArray[i];
			}
			currentDistance = (double)Math.round(currentDistance*1000)/1000;//3ケタ保存
			//最大距離を出す
			for(int i=0;i<segmentsNumber;i++){
				if(velocityArray[i]>maximumVelocity){
					maximumVelocity = velocityArray[i];
				}
			}
			maximumVelocity = (double)Math.round(maximumVelocity*1000)/1000;//3ケタ保存
			System.out.println("maximum veocity:"+maximumVelocity);
		}
		//間違った時の出力
		else if(hillHead.size() == 0 && valleyHead.size() == 0) {
				storeResults = storeResults + "No elevator";
			} 
		else{			
			//calculate all velocities
			ArrayList<Double> velocityHillList = new ArrayList<Double>();
			ArrayList<Double> velocityValleyList = new ArrayList<Double>();
			velocityArray = new double[segmentsNumber];
			
			for(int i=0;i<hillHead.size();i++){
				double tmpVelocity = velocityCalculator(arrayNoGravity, timeArray, hillHead.get(i), hillTail.get(i));
				velocityHillList.add(tmpVelocity);
			}		
			for(int i=0;i<valleyHead.size();i++){
				double tmpVelocity = velocityCalculator(arrayNoGravity, timeArray, valleyHead.get(i), valleyTail.get(i));
				velocityValleyList.add(tmpVelocity);
			}
			
			//find which one is smaller
			if(hillHead.size()>valleyHead.size()){
				//valley is fewer
				
				elevatorMay =valleyHead.size();
				System.out.println("elevator may:"+elevatorMay);
				int rememberTheHill =0;

				for(int i = 0;i<valleyHead.size();i++){				
					for(int j = rememberTheHill;j<hillHead.size();j++){//edited@2012/01/03
						System.out.println(" valley is fewer i:"+i+",j:"+j);
						double tmpPercent = Math.abs((velocityHillList.get(j)+velocityValleyList.get(i)))/(-velocityValleyList.get(i));
						if(tmpPercent<0.2){//threshold
							//the hill is found;
							
							//calculate the distance
							if(valleyHead.get(i)<hillHead.get(j)){
								double aSetOfDistance = distanceCalculator(arrayNoGravity, timeArray, valleyHead.get(i), hillTail.get(j));
								currentDistance = currentDistance + aSetOfDistance;
								double aSetOfVelocity = velocityCalculator(arrayNoGravity, timeArray, valleyHead.get(i), hillTail.get(j));
								velocityList.add(aSetOfVelocity);
							}else if(valleyHead.get(i)>hillHead.get(j)){
								double aSetOfDistance = distanceCalculator(arrayNoGravity, timeArray, hillHead.get(j), valleyTail.get(i));
								currentDistance = currentDistance + aSetOfDistance;
								double aSetOfVelocity = velocityCalculator(arrayNoGravity, timeArray, hillHead.get(j), valleyTail.get(i));
								velocityList.add(aSetOfVelocity);
							}
							rememberTheHill = j+1;
							break;
						}else{
							//the valley and hill is different, ignore the hill
						}
					}
				}
			}else if(valleyHead.size()>hillHead.size()){
				//hill is fewer
				System.out.println("head size:"+valleyHead.size()+" "+hillHead.size());
				elevatorMay =hillHead.size();
				int rememberTheValley =0;
				for(int i = 0;i<hillHead.size();i++){	//edited@2012/01/03			
					for(int j = rememberTheValley;j<valleyHead.size();j++){
						System.out.println(" hill is fewer i:"+i+",j:"+j);
						
						double tmpPercent = Math.abs((velocityValleyList.get(j)+velocityHillList.get(i)))/(velocityHillList.get(i));
						System.out.println("tmp per:"+tmpPercent);
						if(tmpPercent<0.2){//threshold
							//calculate the distance				
							System.out.println("found:"+"i:"+i+",j:"+j);
							if(valleyHead.get(j)<hillHead.get(i)){
								double aSetOfDistance = distanceCalculator(arrayNoGravity, timeArray, valleyHead.get(j), hillTail.get(i));
								currentDistance = currentDistance + aSetOfDistance;
								double aSetOfVelocity = velocityCalculator(arrayNoGravity, timeArray, valleyHead.get(j), hillTail.get(i));
								velocityList.add(aSetOfVelocity);
							}else if(valleyHead.get(j)>hillHead.get(i)){
								double aSetOfDistance = distanceCalculator(arrayNoGravity, timeArray, hillHead.get(i), valleyTail.get(j));
								currentDistance = currentDistance + aSetOfDistance;
								double aSetOfVelocity = velocityCalculator(arrayNoGravity, timeArray, hillHead.get(i), valleyTail.get(j));
								velocityList.add(aSetOfVelocity);
							}	
							rememberTheValley = j+1;
							break;
						}else{
							//the valley and hill is different, ignore the valley
						}
					}
				}
			}
			currentDistance = (double)Math.round(currentDistance*100)/100;//２ケタ保存
			System.out.println("all distances:"+currentDistance);
			for(int i= 0;i<velocityList.size();i++){		
				if(velocityList.get(i)>maximumVelocity){
					maximumVelocity = velocityList.get(i);
				}				
			}
			maximumVelocity = (double)Math.round(maximumVelocity*100)/100;//２ケタ保存
		}
	}

	public double velocityCalculator(double arrayNoGravity[],double timeArray[],int startIndex,int endIndex){
		System.out.println("--velocityCalculator starts--");
		int length = endIndex - startIndex;
		double[] deltaTime = new double[length];
		double[] instantVelocity = new double[length];
		double[] totalVelocity = new double[length];

		// first line data
		deltaTime[0] = timeArray[0 + startIndex]- timeArray[0 - 1 + startIndex];// delta time is OK
		//instant velocity is OK
		instantVelocity[0] = deltaTime[0] * arrayNoGravity[0 + startIndex];
		totalVelocity[0] = instantVelocity[0];// seems OK
		// second line and soon on
		for (int i = 1; i < length; i++) {
			deltaTime[i] = timeArray[i + startIndex] - timeArray[i - 1 + startIndex];// delta time is OK
			//instant velocity is OK
			instantVelocity[i] = deltaTime[i] * arrayNoGravity[i + startIndex];						
			totalVelocity[i] = instantVelocity[i] + totalVelocity[i - 1];//seems OK
		}						
		return totalVelocity[length-1];
	}
	/** calculate the distance, for example, a hill or a valley or a set of hill and valley**/
	public double distanceCalculator(double arrayNoGravity[],double timeArray[], int startIndex, int endIndex) {
		
		System.out.println("--distanceCalculator starts--");
		/** calculate the hill or valley distance */
		int length = endIndex - startIndex;

		double[] deltaTime = new double[length];
		double[] instantVelocity = new double[length];
		double[] totalVelocity = new double[length];
		double[] distancePerMilisecond = new double[length];
		double[] totalDistance = new double[length];

		// first line
		deltaTime[0] = timeArray[0 + startIndex]- timeArray[0 - 1 + startIndex];// delta time is OK
		//instant velocity is OK
		instantVelocity[0] = deltaTime[0] * arrayNoGravity[0 + startIndex];
		totalVelocity[0] = instantVelocity[0];// seems OK
		distancePerMilisecond[0] = deltaTime[0] * totalVelocity[0];//
		totalDistance[0] = distancePerMilisecond[0];
		// second line and soon on
		for (int i = 1; i < length; i++) {
			deltaTime[i] = timeArray[i + startIndex] - timeArray[i - 1 + startIndex];// delta time is OK
			//instant velocity is OK
			instantVelocity[i] = deltaTime[i] * arrayNoGravity[i + startIndex];			
			
			totalVelocity[i] = instantVelocity[i] + totalVelocity[i - 1];//seems OK
			distancePerMilisecond[i] = deltaTime[i] * totalVelocity[i];//
			totalDistance[i] = totalDistance[i - 1] + distancePerMilisecond[i];
		}
		return totalDistance[length - 1];
	}

	public double convertToTwoDecimal(double value) {
		long l1 = Math.round(value * 100); // 四舍五入
		double ret = l1 / 100.0; // 注意：使用 100.0 而不是 100
		return ret;
	}
	//デバッグためのメソッド
	public void storeArrayNoGravity(double arrayNoGravity[]) {
		System.out.println("start store array no gravity");
		String noGravityFile = ("arrayNoGravity.csv");
		try {
			FileOutputStream stream = openFileOutput(noGravityFile, 0);
			BufferedWriter out = new BufferedWriter(new OutputStreamWriter(
					stream));
			for (int i = 0; i < arrayNoGravity.length; i++) {
				out.write(arrayNoGravity[i] + "");
				out.newLine();
			}
			out.close();
		} catch (Exception e) {
			System.out.println(e);
		}
	}
	
	private class backToFileListBtnClickListener implements OnClickListener{

		@Override
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			finish();
		}
		
	}
}
