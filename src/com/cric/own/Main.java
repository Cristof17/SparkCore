package com.cric.own;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Main extends Activity {
	
	private static final int TEMPERATURE = 0 ;
	private static final int CO2 = 1 ;
	private static final int LIGHT = 2 ;
	private static final int HUMIDITY = 3 ;
	
	private EditText textBox;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		textBox = (EditText)findViewById(R.id.text_box);
		textBox.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
					Integer operation = getFeature(textBox.getText().toString());
					if(operation == TEMPERATURE)
						Toast.makeText(getApplicationContext(), "Retrieving temperature", Toast.LENGTH_LONG).show();
					if(operation == LIGHT)
						Toast.makeText(getApplicationContext(), "Retrieving light level", Toast.LENGTH_LONG).show();
					if(operation == HUMIDITY)
						Toast.makeText(getApplicationContext(), "Retrieving humidity level", Toast.LENGTH_LONG).show();
					if(operation == CO2)
						Toast.makeText(getApplicationContext(), "Retrieving CO2 level", Toast.LENGTH_LONG).show();
					new SparkCoreConnection().execute(textBox.getText().toString());
					Toast.makeText(getApplicationContext(), "Sending text to SparkCore", Toast.LENGTH_LONG).show();
					return true;
				}
				return false;
			}
		});
		
		Button speak = (Button)findViewById(R.id.speak);
		speak.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
				
				startActivityForResult(speechIntent, 200);
				
			}
		});
		
	}
	
	
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if(resultCode == Activity.RESULT_OK){
			
			if(requestCode == 200 ){
				ArrayList text = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
				if(text.get(0) == null)
					Toast.makeText(getApplicationContext(), "No word recognized", Toast.LENGTH_LONG).show();
				else{
					
					String speechLine = text.get(0).toString();
					textBox.setText(speechLine);
					
					Integer operation = getFeature(speechLine);
					if(operation == TEMPERATURE)
						Toast.makeText(getApplicationContext(), "Retrieving temperature", Toast.LENGTH_LONG).show();
					if(operation == LIGHT)
						Toast.makeText(getApplicationContext(), "Retrieving light level", Toast.LENGTH_LONG).show();
					if(operation == HUMIDITY)
						Toast.makeText(getApplicationContext(), "Retrieving humidity level", Toast.LENGTH_LONG).show();
					if(operation == CO2)
						Toast.makeText(getApplicationContext(), "Retrieving CO2 level", Toast.LENGTH_LONG).show();
					
					new SparkCoreConnection().execute(textBox.getText().toString());
					Toast.makeText(getApplicationContext(), "Sending text to SparkCore", Toast.LENGTH_LONG).show();
				}
			}
		}
	}




	public int getFeature(String text){
		text = text.toLowerCase();
		
		if(text.contains("temperatura") || text.contains("caldura") || text.contains("cald") || text.contains("hot")
		  || text.contains("frig") || text.contains("calduros") || text.contains("frigut") || text.contains("caldut")){
			return TEMPERATURE;
		}
		
		if(text.contains("CO2") || text.contains("bioxid") || text.contains("carbon") || text.contains("dioxid")){
			return CO2;
		}

		if(text.contains("lumina") || text.contains("intuneric") || text.contains("lumin") || text.contains("negru")
						 || text.contains("umbra") || text.contains("luminos") || text.contains("luminoasta") || text.contains("intunecat")){
			return LIGHT;
		}
		
		if(text.contains("umiditate") || text.contains("umed") || text.contains("apa") || text.contains("aer")
				 || text.contains("densitate")){
			return HUMIDITY;
		}
		return Integer.MIN_VALUE;
	}
	
	private class SparkCoreConnection extends AsyncTask<String, Boolean, Boolean>{

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				URL url = new URL("https://api.spark.io/v1/devices/" + "53ff72066667574846452367" + "/"+"text/");
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				String urlParameters = "access_token=" + "16410d29d21c6bb44c3110228b72f6972ebe3514" + "&args=" + ""+(params[0]);
				con.setDoOutput(true);
				
				DataOutputStream out = new DataOutputStream(con.getOutputStream());
				out.writeBytes(urlParameters);
				out.flush();
				out.close();
				
				StringBuilder response = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line ;
				
				while((line = reader.readLine()) !=null){
					response.append(line);
				}
				
				Log.d("Own", "Response is "+response) ;
				reader.close();
					
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return null;
		}
	}
}


