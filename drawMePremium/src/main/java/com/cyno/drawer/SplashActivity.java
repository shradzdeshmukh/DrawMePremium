package com.cyno.drawer;

import com.cyno.drawme.R;
import com.cyno.drawer.ui.MainActivity;

import android.support.v7.app.ActionBarActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;

public class SplashActivity extends ActionBarActivity  {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		super.onCreate(savedInstanceState);
		getSupportActionBar().hide();
		setContentView(R.layout.activity_splash);
		
		final View rootView = findViewById(R.id.root);
		View textView = findViewById(R.id.splashscreen_text);
		
		Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
		final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.fade_out);
		
		textView.startAnimation(fadeIn);
		
		fadeIn.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				rootView.startAnimation(fadeOut);
			}
		});
		fadeOut.setAnimationListener(new AnimationListener() {
			
			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animation animation) {
				startActivity(new Intent(SplashActivity.this , MainActivity.class));
				finish();				
			}
		});
		
//		new Handler().postDelayed(new Runnable() {
//			
//			@Override
//			public void run() {
//				startActivity(new Intent(SplashActivity.this , MainActivity.class));
//				finish();
//			}
//		}, 5000);
	}


}
