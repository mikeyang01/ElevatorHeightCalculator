package jp.locky.android.elevator;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import jp.locky.android.elevator.tutorial.TutorialActivity;
import jp.locky.android.elevator.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.View.OnFocusChangeListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class Settings extends Activity {
	private EditText usernameET;
	private String userName;
	String uuid;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);
		
		Bundle bundle = getIntent().getExtras();
		uuid = bundle.getString("uuid");

		Button setUserNameButton = (Button) findViewById(R.id.setUserNameButton_settings);
		setUserNameButton.setOnClickListener(new SetUserNameButtonClickListener());
		
		Button buttonTutorial = (Button) findViewById(R.id.tutorialButton_settings);
		buttonTutorial.setOnClickListener(new Tutorial_ClickListener());
		
		Button buttonMail = (Button) findViewById(R.id.settings_mailButton);
		buttonMail.setOnClickListener(new Mail_ClickListener());
	}
	
	class Tutorial_ClickListener implements OnClickListener {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(Settings.this,TutorialActivity.class);
			startActivity(intent);
		}
	}

	class Mail_ClickListener implements OnClickListener {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			// ACTION_SENDTO filters for email apps (discard bluetooth and others)
			String uriText ="mailto:elevator.locky@uclab.jp";

			Uri uri = Uri.parse(uriText);
			Intent sendIntent = new Intent(Intent.ACTION_SENDTO);
			sendIntent.setData(uri);
			sendIntent.putExtra("body", "uuid:"+uuid);

			startActivity(Intent.createChooser(sendIntent, "Send email")); 
		}
	}
	
	class SetUserNameButtonClickListener implements OnClickListener {
		public void onClick(View arg0) {
			// TODO Auto-generated method stub
			showDialog();
		}
	}

	public void onClickFinish(View arg0){
		Intent intent = new Intent(Settings.this, MainActivity.class);
		startActivity(intent);
	}
	
	private void showDialog() {
		usernameET = new EditText(this);
		final AlertDialog alertDialog = new AlertDialog.Builder(this)
				.setTitle("ニックネームを入れてください").setView(usernameET)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {

					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						// Toast.makeText(PopUpActivity.this, "Yes Clicked!",
						// Toast.LENGTH_LONG).show();
						userName = usernameET.getText().toString();
						Log.d("[helloworld]", userName);
						TextView userNameTV = (TextView)findViewById(R.id.username_settings);
						userNameTV.setText(userName);
						storeUserName();
					}
				}).create();
		usernameET.setOnFocusChangeListener(new OnFocusChangeListener() {
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
	
	/**store the user name*/
	private void storeUserName(){
		File storeUserName = new File(MainActivity.AppDir+"files/username.txt");
		if (storeUserName.exists()) {
			//update user name 上書き
			try {				
				FileWriter filewriter = new FileWriter(MainActivity.AppDir+"files/username.txt");
				 filewriter.write(userName);
				 filewriter.close();
			} catch (Exception e) {
			}
		} else {
			// ファイルにデータ保存
			try {				
				String fileName = "username.txt";
				FileOutputStream stream = openFileOutput(fileName, MODE_APPEND);
				BufferedWriter out = new BufferedWriter(new OutputStreamWriter(stream));
				out.write(userName + "");
				out.close();
			} catch (Exception e) {
			}
		}
	}
	
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			Intent intent = new Intent(Settings.this, MainActivity.class);
			startActivity(intent);
			finish();
			}
		return false;		
	}
}