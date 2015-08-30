package jp.locky.android.elevator.net.http;

import java.util.ArrayList;

import jp.locky.android.elevator.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.view.WindowManager.LayoutParams;
import jp.locky.android.elevator.uti.NumberPickerDialog;
import jp.locky.android.elevator.uti.NumberPickerDialog.OnMyNumberSetListener;

public class AdditionalInfoShortActivity extends Activity{
	
	private String feeling = "";
	private String shake="";
	private String capacity ="";
	private String manufacturer = "";
	private String memo = " ";
	
	String[] manufacturerArray;
	String[] capacityArray;
		
	EditText memo_ed;
	TextView textViewCapacity;
	Button buttonCapacity;
	Button chooseManuBtn;
	String selectedCapacity="0";
	String selectedManufact="";
	
	RadioGroup raGroup1;
	RadioGroup raGroup2;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.additional_info_short);
	
		Bundle bundle = getIntent().getExtras();
		feeling = bundle.getString("feeling");		
		shake = bundle.getString("shake");
		capacity = bundle.getString("capacity");		
		manufacturer = bundle.getString("manufacturer");
		memo = bundle.getString("memo");		
		System.out.println("bundle get dditional:"+feeling+shake+capacity+manufacturer+memo);
		
		manufacturerArray =new String[]{
				getString(R.string.additional_mitubishi),
				getString(R.string.additional_toshiba),
				getString(R.string.additional_hitachi),
				getString(R.string.additional_otis),
				getString(R.string.additional_Schindler),
				getString(R.string.additional_nobrand),
				getString(R.string.additional_otherBrand)};
		
		capacityArray = new String[]{"450kg","600kg","750kg","900kg","1000kg","1500kg","1600kg",getString(R.string.select_none),};
			
		
		Button returnButton = (Button) findViewById(R.id.additional_ReturnUploadButton);
		returnButton.setOnClickListener(new returnButtonClickListener());	
		 		   
		//通过findViewById获得RadioGroup对象        
		raGroup1=(RadioGroup)findViewById(R.id.radioGroup_acc);
		//添加事件监听器  
        raGroup1.setOnCheckedChangeListener(new radioFeelingOnCheckedChangeListener());  
        setInitialRadioBtnAcc();//初期化         
        
		raGroup2=(RadioGroup)findViewById(R.id.radioGroup_shake);       
        raGroup2.setOnCheckedChangeListener(new radioShakeOnCheckedChangeListener());  
		setInitialRadioBtnShake();
        
       //createManufacturerList();
        memo_ed = (EditText)findViewById(R.id.additional_memo_ed);
        if(!memo.equals("")){
    	   memo_ed.setText(memo);
        }
        textViewCapacity = (TextView) findViewById(R.id.additional_capacity_tv);
		buttonCapacity = (Button) findViewById(R.id.additional_getCapacity);
		buttonCapacity.setOnClickListener(new buttonCapacityOnclickListener());
		if(!capacity.equals("")){//再変更
			System.out.println("!capacity.equals");
			buttonCapacity.setText(capacity);
		}		
		chooseManuBtn = (Button) findViewById(R.id.addtional_btn_chooseManu);
		chooseManuBtn.setOnClickListener(new chooseManuBtnListener());
		if(!manufacturer.equals("")){
			chooseManuBtn.setText(manufacturer);
		}
	}
	
	private void setInitialRadioBtnAcc(){
		//radiogroup 初期値
		if(feeling.equals("")){
			raGroup1.clearCheck();
		}else{
			System.out.println("feeling button");
			RadioButton radioAcc0 = (RadioButton) findViewById(R.id.radio_acc0);
			RadioButton radioAcc1 = (RadioButton) findViewById(R.id.radio_acc1);
			RadioButton radioAcc2 = (RadioButton) findViewById(R.id.radio_acc2);
			
			ArrayList<RadioButton> radioBtnAccList = new ArrayList<RadioButton>();
			radioBtnAccList.add(radioAcc0);
			radioBtnAccList.add(radioAcc1);
			radioBtnAccList.add(radioAcc2);
			
			ArrayList<String> radioList = new ArrayList<String>();
			radioList.add("strong");
			radioList.add("little");
			radioList.add("nothing");
			for(int i=0;i<radioList.size();i++){
				//System.out.println("feeling:"+feeling);
				//System.out.println("radioListAcc"+radioList.get(i));
				
				if(feeling.equals(radioList.get(i))){
					System.out.println("feel button check");
					radioBtnAccList.get(i).setChecked(true);
					}
			}
		}
	}
	
	private void setInitialRadioBtnShake(){
		//radiogroup 初期値
		if(shake.equals("")){
			raGroup2.clearCheck();
		}else{
			System.out.println("shake button");
			
			RadioButton radioShake0 = (RadioButton) findViewById(R.id.radio_shake0);
			RadioButton radioShake1 = (RadioButton) findViewById(R.id.radio_shake1);
			RadioButton radioShake2 = (RadioButton) findViewById(R.id.radio_shake2);
			
			ArrayList<RadioButton> radioBtnList = new ArrayList<RadioButton>();
			radioBtnList.add(radioShake0);
			radioBtnList.add(radioShake1);
			radioBtnList.add(radioShake2);
			
			ArrayList<String> radioList = new ArrayList<String>();
			radioList.add("strong");
			radioList.add("little");
			radioList.add("nothing");
			for(int i=0;i<radioList.size();i++){
				if(shake.equals(radioList.get(i))){
					System.out.println("shake button check");
					radioBtnList.get(i).setChecked(true);
					}
			}
		}
	}
	private class buttonCapacityOnclickListener implements OnClickListener{
		@Override
		public void onClick(View view) {
			//showNumberPicker(capacity, 0);
			createCapacityAlertD();			
		}
	}
	private void createCapacityAlertD(){
		new AlertDialog.Builder(this)
		.setItems(capacityArray, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				System.out.println("selected:"+which);
				buttonCapacity.setText(capacityArray[which]);
				if(capacityArray[which].equals(getString(R.string.select_none))){
					//create a new dialog input
					showNumberPicker(selectedCapacity, 0);
				}else{
					selectedCapacity = capacityArray[which];
					capacity =selectedCapacity;
					System.out.println("selectedCapacity"+selectedCapacity);
				}
			}
		})
		.show();
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
			buttonCapacity.setText(number+"kg");
			selectedCapacity = selectedCapacity+"kg";
			System.out.println("selectedCapacity:"+selectedCapacity);
		}
	};
	
	private void selectOtherBrand(){
		final EditText edtInput = new EditText(this);
        new AlertDialog.Builder(this)
        .setTitle(getString(R.string.additional_insertManufacturer))
        .setView(edtInput)
        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		/* OKボタンをクリックした時の処理 */
        		selectedManufact = edtInput.getText().toString();
        		
        		System.out.println("selectedManufact:"+selectedManufact);
        		manufacturer = selectedManufact;
        		chooseManuBtn.setText(manufacturer);
        	}
        })
        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
        	public void onClick(DialogInterface dialog, int whichButton) {
        		/* Cancel ボタンをクリックした時の処理 */
        	}
        })
        .show();
	}
	private class chooseManuBtnListener implements OnClickListener{
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			createManufactAlertD();
		}	
	} 
	private void createManufactAlertD(){
		new AlertDialog.Builder(this)
		//.setTitle(getString(R.string.additional_chooseManufacturer))
		.setItems(manufacturerArray, new DialogInterface.OnClickListener(){
			public void onClick(DialogInterface dialog, int which) {
				System.out.println("selected:"+which);
				if(manufacturerArray[which].equals(getString(R.string.additional_otherBrand))){
					//alertdialog
					System.out.println("begin other brand");
					selectOtherBrand();
				}else{
					selectedManufact = manufacturerArray[which];
					manufacturer = selectedManufact;
					System.out.println("choose mamu:");
					chooseManuBtn.setText(manufacturer);					
				}
			}
		})
		.show();
	}
	
	private class radioFeelingOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{
        @Override 
        public void onCheckedChanged(RadioGroup group, int checkedId) {  
            // TODO Auto-generated method stub  
            if(checkedId==R.id.radio_acc0){                    
            	feeling = "strong";
                System.out.println("selected 1");
            }  
            else if(checkedId==R.id.radio_acc1){  
                feeling = "little";  
                System.out.println("selected 2");
            }  
            else if(checkedId ==R.id.radio_acc2){              	
            	feeling = "nothing";  
                System.out.println("selected 3");
            } else {
            	feeling =" ";
            } 
        }  		
	}
	private class radioShakeOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener{
        @Override 
        public void onCheckedChanged(RadioGroup group, int checkedId) {  
            // TODO Auto-generated method stub  
            if(checkedId==R.id.radio_shake0){                                  
            	shake = "strong";
                System.out.println("selected 4");
            }  
            else if(checkedId == R.id.radio_shake1){  
            	shake = "little";  
                System.out.println("selected 5");
            }  
            else if(checkedId == R.id.radio_shake2){  
            	shake = "nothing";  
                System.out.println("selected 6");
            }else{
            	shake = " "; 
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
		System.out.println("capacity go back:"+capacity);
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
