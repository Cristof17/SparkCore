package com.cric.own;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ResultActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.result_activity);
		overridePendingTransition(R.anim.slide_in, R.anim.slide_out);
		
		Intent intent = getIntent();
		TextView resultTextView = (TextView)findViewById(R.id.result_text);
		RelativeLayout layout = (RelativeLayout)findViewById(R.id.result_layout);
		Toast.makeText(getApplicationContext(), "Press anywhere to go back ", Toast.LENGTH_LONG).show();
		
		layout.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent backIntent = new Intent(getApplicationContext(),Main.class);
				backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				backIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(backIntent);
			}
		});
		
		String type = intent.getExtras().getString("type");
		int value = intent.getExtras().getInt("value");
		AlertDialog.Builder builder = new AlertDialog.Builder(ResultActivity.this);
		
		int percent = (int)(100-((float)value/4100)*100);
		
//		TextView newText = new TextView(getApplicationContext());
//		newText.setText("Percent = "+(100-((float)value/4100)*100));
//		
//		builder.setView(newText);
//		builder.setPositiveButton("Got it", null);
//		builder.create().show();
		switch (type) {
		case "light":
			if(value < 2150)
				resultTextView.setText("Camera este foarte luminata\n" + percent +"%");
			else if(2150 <= value && value <= 3000)
				resultTextView.setText("Camera este luminata normal\n" + percent +"%");
			else if (value > 3000)
				resultTextView.setText("Camera este intunecata\n" + percent +"%");
			break;

		default:
			break;
		}
		
		
		
	}
	
	

	@Override
	protected void onPause() {
		overridePendingTransition(R.anim.child_slide_in, R.anim.child_slide_out);
		super.onPause();
	}



	@Override
	protected void onDestroy() {
		overridePendingTransition(R.anim.child_slide_in, R.anim.child_slide_out);
		super.onDestroy();
	}
	
	

	
	
}
