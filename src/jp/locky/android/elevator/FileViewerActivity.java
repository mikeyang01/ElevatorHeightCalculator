package jp.locky.android.elevator;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import jp.locky.android.elevator.calculate.FileViewerCalculatorActivity;
import jp.locky.android.elevator.net.http.HttpElevatorPost;
import jp.locky.android.elevator.uti.FileCompress;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class FileViewerActivity extends Activity {
	String item;
	File[] filelist;
	String deviceId;
	String fileNameTemp;
	String csvFileFullName;
	// onCreateメソッド(画面初期表示イベントハンドラ)
	@Override
	public void onCreate(Bundle savedInstanceState) {		
		// スーパークラスのonCreateメソッド呼び出し
		super.onCreate(savedInstanceState);
		// レイアウト設定ファイルの指定
		setContentView(R.layout.filelist);
		Log.d("[hello world]", "after set content view");
		
		Bundle bundle = getIntent().getExtras();
		deviceId = bundle.getString("uuid");
		System.out.println("uuid:"+deviceId);		
		createFileList();
		
		Log.d("[hello world]", "after set text");
	}
	//file listを作って、layout に追加する
	public void createFileList(){
		System.out.println("create file list");
		File dir = new File(MainActivity.AppDir+"files/unupload");
		filelist = dir.listFiles();
		//System.out.println("file list size:"+filelist.length+"file list print"+filelist);
		LinearLayout elevatorListLayout = (LinearLayout) findViewById(R.id.filelistlayout_filelist);
		elevatorListLayout.removeAllViews();
		if ((filelist == null)||(filelist.length ==0)) {
			TextView tv_nodata = new TextView(FileViewerActivity.this);
			tv_nodata.setText(getString(R.string.fileViwer_noUnuploadedFile));//"アップロードしてないデータがありません");
			elevatorListLayout.addView(tv_nodata);
		} 
		else {
			//here we create 2 list, one to store listview,one to store data name;
			final ArrayList<String> fileNameList = new ArrayList<String>();
			final ArrayList<String> fileViewList = new ArrayList<String>();
			
			for (File file : filelist) {				
				//1.find no .postdata file			
				//2.add read .postdata
				//3.add .uploaded 
				String[] fileNameDiffer = file.getName().split("\\.");
				System.out.println("fileNameDiffer.length:"+fileNameDiffer.length+" file name:"+file.getName());
				System.out.println("file length:"+file.length());
				if(file.length()==0){
					//elete this empty file
					System.out.println("delete empty file success?:"+file.delete());
				}else{
					if (fileNameDiffer.length == 4) {// 2012....-121357.csv.postdata.uploaded
						// read file
						System.out.println("before read post data file:"+ file.getName());
						String[] postArray = readPostDataFile(MainActivity.AppDir+ "files/unupload/" + file.getName());
						// System.out.println("post data length:"+postArray.length);
						System.out.println("post array 9:"+postArray[9]);
						if ((!postArray[9].equals(""))&&(!postArray[9].equals(" "))) {//building
							
							String fileViewTmp = postArray[9]+" "+(int)Double.parseDouble(postArray[5])+"ｍ";
							fileViewList.add(fileViewTmp);
							fileNameList.add(file.getName());
						} else {
							String[] filenameshorter = file.getName().split("_");
							fileViewList.add(setFileNameView(filenameshorter[0]));//" "+(int)Double.parseDouble(postArray[5])+"ｍ");
							fileNameList.add(file.getName());
						}
					} else if (fileNameDiffer.length == 3) {// 2012....-121357.csv.postdata
						// read file
						String[] postArray = readPostDataFile(MainActivity.AppDir+ "files/unupload/" + file.getName());
						System.out.println("create file list, post data length:"+ postArray.length);
						if ((!postArray[9].equals(""))&&(!postArray[9].equals(" "))) {
							fileViewList.add(postArray[9] + " "+getString(R.string.fileViwer_unsend));//未送信");
							fileNameList.add(file.getName());
						} else {
							String[] filenameshorter = file.getName().split("_");
							fileViewList.add(filenameshorter[0] + " "+getString(R.string.fileViwer_unsend));//未送信");
							fileNameList.add(file.getName());
						}
					} else if (fileNameDiffer.length == 2) { // 2012....-121357.csv
						// csv file
					} else {
						System.out.println("file viewer error:"+ fileNameDiffer.length);
					}
				}
			}
			//in order to put into adapter
			String[] fileViewArray = new String[fileViewList.size()];
			for(int i =0; i<fileViewList.size();i++){
				fileViewArray[i] = fileViewList.get(i);
			}
			System.out.println("file name list:"+fileNameList.get(0));
			/** ---sub page--- */
			// create a layout object

			//もしユーザが建物を入れてない場合、時間表示
			//アップロードしないとき（未アップロード）			
			final ListView listView = new ListView(this);
			//ListView listView = (ListView)findViewById(id)
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.list02,fileViewArray);
			listView.setAdapter(adapter);			
			/** set list view click event */
			listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {							
				public void onItemClick(AdapterView<?> parent,View view, final int position, long id) {
						//item = (String) listView.getItemAtPosition(position);
						System.out.println("position:" + position+" file name:"+fileNameList.get(position));

						/** add the path and file string to the item */
						if(fileViewList.get(position).contains(getString(R.string.fileViwer_unsend))){//"未送信")){
							alertDialogUnupload(listView, fileNameList, position);
						}else{
							alertDialogUploaded(listView, fileNameList, position);
						}						
				}					
			});
			elevatorListLayout.addView(listView);
		}
	}
	//年　月　日
	public String setFileNameView(String s){
			try{
				String language = Locale.getDefault().getLanguage();
				if (language.equals("ja")) {
					s = s.substring(0,4)+"年"+s.substring(4,6)+"月"+s.substring(6,8)+"日"+s.substring(9,11)+"時"+s.substring(11,13)+"分"+s.substring(15);
				}
				return s;
			}catch(Exception e){
				return s;
			}
	}
	
	// クリックリスナー定義
	class ButtonClickListener implements OnClickListener {
		// onClickメソッド(ボタンクリック時イベントハンドラ)
		public void onClick(View v) {
			// タグの取得
			String tag = (String) v.getTag();
			// 表示ボタンが押された場合
			if (tag.endsWith("return")) {
				// return to main menu
				finish();
			}
		}
	}

	public void sendLocalDataLoader(){
		SendLocalDataLoader sendLocalDataLoader = new SendLocalDataLoader();
		sendLocalDataLoader.execute();
	}
	private class SendLocalDataLoader extends
			AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			//get local post data
			String[] postArray = readPostDataFile(fileNameTemp);
			
			for(int i=0;i<postArray.length;i++){
				System.out.print(postArray[i]+",");
			}
			
			int length = postArray.length;	
			System.out.println("send all loader, post local data length:"+length);
			//post dataを再建する
			List<NameValuePair> postData = new ArrayList<NameValuePair>(2);
			postData.add(new BasicNameValuePair("device_id", postArray[0]));
			postData.add(new BasicNameValuePair("device_brand", postArray[1]));
			postData.add(new BasicNameValuePair("device_type", postArray[2]));
			//calculated info
			postData.add(new BasicNameValuePair("frequency", postArray[3]+""));
			postData.add(new BasicNameValuePair("gravity", postArray[4]+""));			
			postData.add(new BasicNameValuePair("distance",postArray[5]+""));
			postData.add(new BasicNameValuePair("top_speed", postArray[6]+""));
			//building and location info
			postData.add(new BasicNameValuePair("latitude", postArray[7]+""));
			postData.add(new BasicNameValuePair("longitude", postArray[8]+""));
			postData.add(new BasicNameValuePair("building_name", postArray[9]));
			postData.add(new BasicNameValuePair("elevator_name", postArray[10]));
			postData.add(new BasicNameValuePair("segment_number", postArray[11]+""));
			postData.add(new BasicNameValuePair("segments", postArray[12]));//startfloor-stopfloor-endfloor			
			postData.add(new BasicNameValuePair("file_name", postArray[13]));
			//Additional info
			postData.add(new BasicNameValuePair("feeling", postArray[14]));
			postData.add(new BasicNameValuePair("shake", postArray[15]));
			postData.add(new BasicNameValuePair("capacity", postArray[16]));
			postData.add(new BasicNameValuePair("manufacturer", postArray[17]));
			postData.add(new BasicNameValuePair("memo", postArray[18]));
			//read file's postData
			//ObjectFileConverter objectFileConverter = new ObjectFileConverter();
			//postData = objectFileConverter.readObjectFromFile(fileNameTemp+".postdata");
			csvFileFullName = "/data/data/jp.locky.android.elevator/files/unupload/"+postArray[13];
			System.out.println("post data:"+postData);
			
			//zip the file
			FileCompress fileCompress = new FileCompress();
			fileCompress.zip(csvFileFullName, csvFileFullName+".zip");
			
			HttpElevatorPost hmpr = new HttpElevatorPost(MainActivity.serverIP+"main_submit2", postData, "csvFile",csvFileFullName+".zip");//main submit 2!!
			System.out.println("server address: "+MainActivity.serverIP);
			String replyMessage = hmpr.send()+"";//for this time, the reply message is the total distance
			Log.d("elevatorLog", "upload over");
			System.out.println("reply message:"+replyMessage+",file name:"+fileNameTemp);
			return replyMessage;
		}

		@Override
		protected void onPostExecute(String replyMessage) {
			// remove elevator list
			//no matter what returns, first delete the zip file 
			//delete zip file 
			File tmpZip = new File(csvFileFullName+".zip");
			if (tmpZip.exists()) {
				System.out.println("file delete results: "+tmpZip.delete());
			} else {
				Log.d("elevatorLog", "after send no file exist");
			}
			
			if(replyMessage.equals("null")){
				//toast please try again
				Toast.makeText(FileViewerActivity.this, getString(R.string.fileViwer_serverFail),Toast.LENGTH_SHORT).show();
			}
			else{
				//toast upload success, delete file maybe reload the layout
				Toast.makeText(FileViewerActivity.this, getString(R.string.fileViwer_serverSuccess),Toast.LENGTH_SHORT).show();
				//delete .csv and .csv.postdata
				
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
								
				File tmpFile = new File(fileNameTemp);
				/* ファイル(ディレクトリ)が存在するかどうか判定します。 */
				System.out.println("need to rename file:"+tmpFile+"\n To file:"+fileNameTemp);
				if (tmpFile.exists()) {
				//	CopyFile copyFile = new CopyFile();
				//	copyFile.copyFile(srcFilePath, dstFilePath)
					System.out.println("rename is success or not: "+tmpFile.renameTo(new File(fileNameTemp+".uploaded")));
					//System.out.println("file delete results: "+tmp.delete());
					//System.out.println("file delete results: "+tmpPostData.delete());									
				} else {
					Log.d("elevatorLog", "after send no file exist");
				}
				createFileList();//reload list view !HttpPostは時間かかるため、createFileListをここに置く
			}
		}	
	}
	
	public String[] readPostDataFile(String filePath){
		System.out.println("read post data file:"+filePath);
		String[] postArray = null;
		String[] standardArray = new String[19];
		for(int i=0;i<19;i++){//Nullを無くなるために
			standardArray[i]="";
		}
		
		File read = new File(filePath);
		System.out.println("file exist in readPostData File? " + read.exists());
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(read));			
			String tmp = br.readLine();
			System.out.println("in read Post Data File:"+tmp);
			postArray = tmp.split(",");
			System.out.println("in read Post Data File:postdata length:"+postArray.length);
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		 catch (IOException e) {
			e.printStackTrace();
		}
		
		if(postArray.length==14){//古いバージョン 1.2
			for(int i=0;i<14;i++){
				standardArray[i] = postArray[i]; 
			}
			return standardArray;
		}
		else if(postArray.length==19){//新しいバージョン1.3
			return postArray;
		}
		else{
			return standardArray;
		}
	}
	

	public void renameFileString(String from,String to) throws IOException{
		File fromFile = new File(from);
		File toFile = new File(to);
		fromFile.renameTo(toFile);
	}
	
	public void renameFile(File from,File to) throws IOException{
		from.renameTo(to);
	}

	public void alertDialogUnupload(ListView listView,final ArrayList<String> fileNameList,final int position){
		final String[] fileTmpName = fileNameList.get(position).split("\\.");
		System.out.println("file name list get position:"+fileTmpName[0]+".csv");
		/** generate a dialog alert */					
		AlertDialog.Builder dlg;		
		dlg = new AlertDialog.Builder(FileViewerActivity.this);
		dlg.setTitle(getString(R.string.fileViwer_select)).setPositiveButton(getString(R.string.fileViwer_upload),new DialogInterface.OnClickListener() {							
			public void onClick(DialogInterface dialog,int whichButton) {				
				/* ここにYESの処理 */						
				// send to upload 											
				fileNameTemp =MainActivity.AppDir+"files/unupload/"+fileNameList.get(position);							
				sendLocalDataLoader();																		
			}	
		})							
		.setNegativeButton(getString(R.string.fileViwer_delete),new DialogInterface.OnClickListener() {										
			public void onClick(DialogInterface dialog,int whichButton) {											
				/* ここにNOの処理 */											
				File fileTmpPostData = new File(MainActivity.AppDir+"files/unupload/"+fileNameList.get(position));
				File fileTmpCsv = new File(MainActivity.AppDir+"files/unupload/"+fileTmpName[0]+".csv");
				fileTmpPostData.delete();
				fileTmpCsv.delete();
				System.out.println("exist?"	+ fileTmpPostData.exists());											
				// adapter.remove(item);											
				createFileList();//reload list view										
			}									
		})							
		.setNeutralButton(getString(R.string.fileViwer_recog),new DialogInterface.OnClickListener() {										
			public void onClick(DialogInterface dialog,int whichButton) {	
				Intent intent = new Intent(FileViewerActivity.this,FileViewerCalculatorActivity.class);
				Bundle bundle = new Bundle();
				
				bundle.putString("fileName", fileTmpName[0]+".csv");
				bundle.putString("deviceId", deviceId);
				intent.putExtras(bundle);
				startActivity(intent);
			}									
		}).show();
	}
	
	public void alertDialogUploaded(ListView listView,final ArrayList<String> fileNameList,final int position){
		final String[] fileTmpName = fileNameList.get(position).split("\\.");
		System.out.println("file name list get position:"+fileTmpName[0]+".csv");
		/** generate a dialog alert */					
		AlertDialog.Builder dlg;		
		dlg = new AlertDialog.Builder(FileViewerActivity.this);
		dlg.setTitle(getString(R.string.fileViwer_select)).setPositiveButton(getString(R.string.fileViwer_recog),new DialogInterface.OnClickListener() {							
			public void onClick(DialogInterface dialog,int whichButton) {				
				Intent intent = new Intent(FileViewerActivity.this,FileViewerCalculatorActivity.class);
				Bundle bundle = new Bundle();
				String[] fileTmpName = fileNameList.get(position).split("\\.");
				System.out.println("file name list get position:"+fileTmpName[0]+"."+fileTmpName[1]);
				
				bundle.putString("fileName", fileTmpName[0]+".csv");
				bundle.putString("deviceId", deviceId);
				intent.putExtras(bundle);
				startActivity(intent);																
			}	
		})							
		.setNegativeButton(getString(R.string.fileViwer_delete),new DialogInterface.OnClickListener() {										
			public void onClick(DialogInterface dialog,int whichButton) {											
				/* ここにNOの処理 */											
				File fileTmpPostData = new File(MainActivity.AppDir+"files/unupload/"+fileNameList.get(position));
				File fileTmpCsv = new File(MainActivity.AppDir+"files/unupload/"+fileTmpName[0]+".csv");
				fileTmpPostData.delete();
				fileTmpCsv.delete();
				System.out.println("exist?"	+ fileTmpPostData.exists());										
				// adapter.remove(item);											
				createFileList();//reload list view										
			}									
		}).show();
	}
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(FileViewerActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
			}
		return false;		
	}
	
}