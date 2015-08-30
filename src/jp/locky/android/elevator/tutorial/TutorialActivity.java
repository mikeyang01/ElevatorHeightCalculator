package jp.locky.android.elevator.tutorial;

import java.util.Locale;

import jp.locky.android.elevator.MainActivity;
import jp.locky.android.elevator.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ViewFlipper;

public class TutorialActivity extends Activity implements OnGestureListener {
	/** Called when the activity is first created. */
	private ViewFlipper viewFlipper;
	private GestureDetector gestureDetector = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		String language = Locale.getDefault().getLanguage();
		System.out.println("language:" + language);
		if (language.equals("ja")) {
			setContentView(R.layout.tutorial_flipper_jp);
			viewFlipper = (ViewFlipper) findViewById(R.id.flipper_jp);
			System.out.println("view ja");
		}else if(language.equals("zh")){
			setContentView(R.layout.tutorial_flipper_zh);
			viewFlipper = (ViewFlipper) findViewById(R.id.flipper_zh);
			System.out.println("wiev zh");
		}else {
			setContentView(R.layout.tutorial_flipper_en);
			viewFlipper = (ViewFlipper) findViewById(R.id.flipper_en);
			System.out.println("view en");
		}
		
		gestureDetector = new GestureDetector(this);

		Button buttonIntro = (Button) findViewById(R.id.TutorialToMainButton);
		buttonIntro.setOnClickListener(new ToMainButtonClickListener());

	}

	class ToMainButtonClickListener implements OnClickListener {
		public void onClick(View v) {
			// TODO Auto-generated method stub
			Intent intent = new Intent(TutorialActivity.this, MainActivity.class);
			startActivity(intent);
			finish();
		}
	}

	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// 对手指滑动的距离进行了计算，如果滑动距离大于120像素，就做切换动作，否则不做任何切换动作。
		// 从左向右滑动
		if (arg0.getX() - arg1.getX() > 80) {
			// 添加动画
			this.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_left_in));
			this.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_left_out));
			//System.out.println("display tutorial page number:"+this.viewFlipper.getDisplayedChild());
			if(this.viewFlipper.getDisplayedChild()!=3){
				this.viewFlipper.showNext();
			}
			return true;
		}// 从右向左滑动
		else if (arg0.getX() - arg1.getX() < -80) {
			this.viewFlipper.setInAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_right_in));
			this.viewFlipper.setOutAnimation(AnimationUtils.loadAnimation(this,
					R.anim.push_right_out));
			
			if(this.viewFlipper.getDisplayedChild()!=0){
				this.viewFlipper.showPrevious();
			}						
			return true;
		}
		return true;
	}

	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
	}

	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
	}

	public boolean onSingleTapUp(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return this.gestureDetector.onTouchEvent(event);
	}
}