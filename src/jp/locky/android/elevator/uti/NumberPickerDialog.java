package jp.locky.android.elevator.uti;

import jp.locky.android.elevator.R;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NumberPickerDialog extends Dialog implements OnClickListener {
	Button btn_ok;
	Button btn_cancel;
	TextView txt_input;
	TextView txt_rangeMin;
	TextView txt_rangeMax;
	Button btn_1;
	Button btn_2;
	Button btn_3;
	Button btn_4;
	Button btn_5;
	Button btn_6;
	Button btn_7;
	Button btn_8;
	Button btn_9;
	Button btn_0;
	Button btn_clear;
	Button btn_dot;
	Context context;
	String initNumber;
	int mode;

	public interface OnMyNumberSetListener {
		void onNumberSet(String number, int mode);
	}

	private OnMyNumberSetListener mListener;

	public NumberPickerDialog(Context context, OnMyNumberSetListener listener,
			String number, int mode) {
		super(context);
		this.context = context;
		this.mListener = listener;
		this.initNumber = number;
		this.mode = mode;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.number_picker_layout);
		setTitle("Number Picker");

		txt_input = (TextView) findViewById(R.id.txt_inputNumber);

		txt_input.setText(String.valueOf(initNumber));

		btn_1 = (Button) findViewById(R.id.btn_1);
		btn_2 = (Button) findViewById(R.id.btn_2);
		btn_3 = (Button) findViewById(R.id.btn_3);
		btn_4 = (Button) findViewById(R.id.btn_4);
		btn_5 = (Button) findViewById(R.id.btn_5);
		btn_6 = (Button) findViewById(R.id.btn_6);
		btn_7 = (Button) findViewById(R.id.btn_7);
		btn_8 = (Button) findViewById(R.id.btn_8);
		btn_9 = (Button) findViewById(R.id.btn_9);
		btn_0 = (Button) findViewById(R.id.btn_0);
		btn_clear = (Button) findViewById(R.id.btn_clear);
		btn_dot = (Button) findViewById(R.id.btn_dot);
		btn_ok = (Button) findViewById(R.id.ok);
		btn_cancel = (Button) findViewById(R.id.cancel);
		btn_1.setOnClickListener(this);
		btn_2.setOnClickListener(this);
		btn_3.setOnClickListener(this);
		btn_4.setOnClickListener(this);
		btn_5.setOnClickListener(this);
		btn_6.setOnClickListener(this);
		btn_7.setOnClickListener(this);
		btn_8.setOnClickListener(this);
		btn_9.setOnClickListener(this);
		btn_0.setOnClickListener(this);
		btn_clear.setOnClickListener(this);
		btn_dot.setOnClickListener(this);
		btn_ok.setOnClickListener(this);
		btn_cancel.setOnClickListener(this);
		setCancelable(false);
	}

	private void setText(String num) {
		String nowNumber = txt_input.getText().toString();
		String newNumber = "";

		if (nowNumber.length() >= 8) {
			return;
		}

		int dotSite = nowNumber.indexOf(".");
		if (dotSite > 0 && dotSite + 2 < nowNumber.length()) {
			return;
		}

		if (!num.equals(".")) {
			
			if (nowNumber.equals("") || nowNumber.equals("0")) {
				newNumber = String.valueOf(num);
			} else {
				newNumber = nowNumber.concat(String.valueOf(num));
			}
		} else {
			if (nowNumber.equals("") || nowNumber.contains(".")) {
				return;
			} else {
				newNumber = nowNumber.concat(".");
			}
		}
		txt_input.setText(newNumber);
	}

	private void deleteText() {
		txt_input.setText("");
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ok:
			String number = txt_input.getText().toString();
			if (number.endsWith(".")) {
				number = number.substring(0, number.length() - 1);
			}
			mListener.onNumberSet(number, mode);
			dismiss();
			break;
		case R.id.cancel:
			dismiss();
			break;
		case R.id.btn_0:
			setText("0");
			break;
		case R.id.btn_1:
			setText("1");
			break;
		case R.id.btn_2:
			setText("2");
			break;
		case R.id.btn_3:
			setText("3");
			break;
		case R.id.btn_4:
			setText("4");
			break;
		case R.id.btn_5:
			setText("5");
			break;
		case R.id.btn_6:
			setText("6");
			break;
		case R.id.btn_7:
			setText("7");
			break;
		case R.id.btn_8:
			setText("8");
			break;
		case R.id.btn_9:
			setText("9");
			break;
		case R.id.btn_dot:
			setText(".");
			break;
		case R.id.btn_clear:
			deleteText();
			break;
		}
	}
}
