package jp.locky.android.elevator.net.http;

import jp.locky.android.elevator.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.view.WindowManager.LayoutParams;
import jp.locky.android.elevator.uti.NumberPickerDialog;
import jp.locky.android.elevator.uti.NumberPickerDialog.OnMyNumberSetListener;

public class AdditionalInfoActivity extends Activity{
	
	private String feeling = "";
	private String shake="";
	private String capacity ="";
	private String manufacturer = "";
	private String memo = "";
	
	String[] manufacturerList =null;
	EditText memo_ed;
	TextView textViewCapacity;
	Button buttonCapacity;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.additionalinfo);
		
		manufacturerList =new String[]{
				getString(R.string.additional_mitubishi),
				getString(R.string.additional_toshiba),
				getString(R.string.additional_hitachi),
				getString(R.string.additional_otis),
				getString(R.string.additional_Schindler),
				getString(R.string.additional_nobrand)};
			
		
		Button returnButton = (Button) findViewById(R.id.additional_ReturnUploadButton);
		returnButton.setOnClickListener(new returnButtonClickListener());	
		 		   
		//通过findViewById获得RadioGroup对象        
		RadioGroup raGroup1=(RadioGroup)findViewById(R.id.radioGroup1);
		raGroup1.clearCheck();
        //添加事件监听器  
        raGroup1.setOnCheckedChangeListener(new radioFeelingOnCheckedChangeListener());  
		         
		//通过findViewById获得RadioGroup对象        
		RadioGroup raGroup2=(RadioGroup)findViewById(R.id.radioGroup2);       
        //添加事件监听器
		raGroup2.clearCheck();
        raGroup2.setOnCheckedChangeListener(new radioShakeOnCheckedChangeListener());  
		 
        createManufacturerList();
        memo_ed = (EditText)findViewById(R.id.additional_memo_ed);
       
        textViewCapacity = (TextView) findViewById(R.id.additional_capacity_tv);
		buttonCapacity = (Button) findViewById(R.id.additional_getCapacity);
		buttonCapacity.setOnClickListener(new buttonCapacityOnclickListener());
        
	}
	
	private class buttonCapacityOnclickListener implements OnClickListener{
		@Override
		public void onClick(View view) {
			showNumberPicker(capacity, 0);
		}
	}	
	private void showNumberPicker(String number, int mode) {
		String nowNumber = (String) textViewCapacity.getText();
		if (!number.equals("")) {
			nowNumber = number;
		}
		new NumberPickerDialog(this, listener, nowNumber, mode).show();
	}
	private OnMyNumberSetListener listener = new OnMyNumberSetListener() {
		@Override
		public void onNumberSet(String number, int mode) {
			buttonCapacity.setText(number+"　人");
		}
	};
	
	private void createManufacturerList(){
		LinearLayout layoutMain = (LinearLayout) findViewById(R.id.additional_manufacturer_ll);
		
		final LinearLayout llVertical = new LinearLayout(AdditionalInfoActivity.this);
		llVertical.setOrientation(LinearLayout.VERTICAL);
		ListView listView = new ListView(AdditionalInfoActivity.this);
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(AdditionalInfoActivity.this,R.layout.list02, manufacturerList);
		listView.setAdapter(adapter);				
		
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View view,int position, long id) {
				ListView listView = (ListView) parent;
				// クリックされたアイテムを取得します
				String item = (String) listView.getItemAtPosition(position);
				manufacturer = item;
				Toast.makeText(AdditionalInfoActivity.this, item+getString(R.string.common_selected), Toast.LENGTH_SHORT).show();
				//選択した後、建築リストをクリアする 
				llVertical.removeAllViews();
					
				//show building in the layout
					
				TextView textView = new TextView(AdditionalInfoActivity.this);					
				textView.setText(manufacturer);					
				llVertical.addView(textView);		
			}
		});
		llVertical.removeAllViews();		
		llVertical.addView(listView);	
		setListViewHeightBasedOnChildren(listView);

		layoutMain.addView(llVertical); // Show the page first	 
	} 
	
	private class radioFeelingOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{
        @Override 
        public void onCheckedChanged(RadioGroup group, int checkedId) {  
            // TODO Auto-generated method stub  
            if(checkedId==R.id.radio0){                    
                feeling = "nothing";  
                System.out.println("selected 1");
            }  
            else if(checkedId==R.id.radio1){  
                feeling = "little";  
                System.out.println("selected 2");
            }  
            else{  
            	feeling = "strong";
                System.out.println("selected 3");
            }  
        }  		
	}
	private class radioShakeOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{
        @Override 
        public void onCheckedChanged(RadioGroup group, int checkedId) {  
            // TODO Auto-generated method stub  
            if(checkedId==R.id.radio2_0){                    
                shake = "nothing";  
                System.out.println("selected 4");
            }  
            else if(checkedId==R.id.radio2_1){  
            	shake = "little";  
                System.out.println("selected 5");
            }  
            else{  
            	shake = "strong";
                System.out.println("selected 6");
            }  
        }  		
	}
	private class returnButtonClickListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			GoBackToUploadActivity();
		}		
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			//same as return button
			GoBackToUploadActivity();
		}
		return false;
	}
	
	public void GoBackToUploadActivity(){
		
		memo = memo_ed.getText().toString();
		memo=memo.replaceAll(","," ");//splitを使うとき、","がある場合は削除
		capacity = textViewCapacity.getText().toString();
		replyAdditionalInfo();
	}
	public void replyAdditionalInfo() {
		Intent i = new Intent();
		Bundle b = new Bundle();
		b.putString("feeling", feeling);
		b.putString("shake", shake);
		b.putString("capacity", capacity);
		b.putString("manufacturer", manufacturer);
		b.putString("memo", memo);

		i.putExtras(b);
		this.setResult(RESULT_OK, i);
		this.finish();
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
