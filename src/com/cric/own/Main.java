package com.cric.own;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
				if(haveNetworkConnection()){
				if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
					Integer operation = getFeature(textBox.getText().toString());
					if(operation == TEMPERATURE){
						new SparkCoreConnection().execute("text","temperature");
						Toast.makeText(getApplicationContext(), "Retrieving temperature", Toast.LENGTH_LONG).show();
					}
					if(operation == LIGHT){
						new SparkCoreConnection().execute("text","light");
						try {
							String light_level = new SparkCoreConnection().execute("getLight","none").get();
							Toast.makeText(getApplicationContext(), "Light level = "+ light_level, Toast.LENGTH_LONG).show();
							
							if(light_level.equals("0") || light_level.equals("1")){
								light_level = new SparkCoreConnection().execute("getLight","none").get();
							}
							Intent lightIntent = new Intent(getApplicationContext(),ResultActivity.class);
							lightIntent.putExtra("type", "light");
							lightIntent.putExtra("value", Integer.parseInt(light_level));
							startActivity(lightIntent);
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Toast.makeText(getApplicationContext(), "Retrieving light level", Toast.LENGTH_LONG).show();
					}
					if(operation == HUMIDITY){
						new SparkCoreConnection().execute("text","humidity");
						Toast.makeText(getApplicationContext(), "Retrieving humidity level", Toast.LENGTH_LONG).show();
					}
					if(operation == CO2){
						new SparkCoreConnection().execute("text","co2");
						Toast.makeText(getApplicationContext(), "Retrieving CO2 level", Toast.LENGTH_LONG).show();
					}
					return true;
				}
				}//endof haveNetworkConnection();
				else
					Toast.makeText(getApplicationContext(), "No internet connection ", Toast.LENGTH_SHORT).show();
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
					
					if(haveNetworkConnection()){
						
					if(operation == TEMPERATURE){
						new SparkCoreConnection().execute("text","temperature");
						Toast.makeText(getApplicationContext(), "Retrieving temperature", Toast.LENGTH_LONG).show();
					}
					if(operation == LIGHT){
						new SparkCoreConnection().execute("text","light");
						try {
							String light_level = new SparkCoreConnection().execute("getLight","none").get();
							Toast.makeText(getApplicationContext(), "Light level = "+ light_level, Toast.LENGTH_LONG).show();
							if(light_level.equals("0") || light_level.equals("1")){
								light_level = new SparkCoreConnection().execute("getLight","none").get();
							}
							
							Intent lightIntent = new Intent(getApplicationContext(),ResultActivity.class);
							lightIntent.putExtra("type", "light");
							lightIntent.putExtra("value", Integer.parseInt(light_level));
							startActivity(lightIntent);
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						Toast.makeText(getApplicationContext(), "Retrieving light level", Toast.LENGTH_LONG).show();
					}
					if(operation == HUMIDITY){
						new SparkCoreConnection().execute("text","humidity");
						Toast.makeText(getApplicationContext(), "Retrieving humidity level", Toast.LENGTH_LONG).show();
					}
					if(operation == CO2){
						new SparkCoreConnection().execute("text","co2");
						Toast.makeText(getApplicationContext(), "Retrieving CO2 level", Toast.LENGTH_LONG).show();
					}
					}else
						Toast.makeText(getApplicationContext(), "No internet connection", Toast.LENGTH_SHORT).show();//endof haveNetworkConnection
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
		
		if(text.contains("co2") || text.contains("bioxid") || text.contains("carbon") || text.contains("dioxid")){
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
	
	
	private boolean haveNetworkConnection() {
	    boolean haveConnectedWifi = false;
	    boolean haveConnectedMobile = false;

	    ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
	    NetworkInfo[] netInfo = cm.getAllNetworkInfo();
	    for (NetworkInfo ni : netInfo) {
	        if (ni.getTypeName().equalsIgnoreCase("WIFI"))
	            if (ni.isConnected())
	                haveConnectedWifi = true;
	        if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
	            if (ni.isConnected())
	                haveConnectedMobile = true;
	    }
	    return haveConnectedWifi || haveConnectedMobile;
	}
	
	
	private class SparkCoreConnection extends AsyncTask<String, Boolean, String>{

		@Override
		
		protected String doInBackground(String... params) {
			String value = null;
			try {
				URL url = new URL("https://api.spark.io/v1/devices/" + "53ff72066667574846452367" + "/"+params[0]+"/");
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				String urlParameters = "access_token=" + "708724b49e2162c67b0b4347cc209ee5058abdaa" + "&args=" + ""+(params[1]);
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
				
				JSONObject json_root = new JSONObject(response.toString());
				value = json_root.getString("return_value");
				
				reader.close();
					
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			return value;
		}
	}
	
	
	private class SparkGetAsync extends AsyncTask<String, Void, String>{

		@Override
		protected String doInBackground(String... params) {
			
			try {
			HttpClient client = new DefaultHttpClient();
			HttpGet get = new HttpGet("https://api.spark.io/v1/devices/" + "53ff72066667574846452367" + "/"+params[0]);
			get.setHeader("authorization","708724b49e2162c67b0b4347cc209ee5058abdaa");
			HttpResponse response ;
			
			response = client.execute(get);
			String rasp;
			
				rasp = EntityUtils.toString(response.getEntity());
			
			Log.d("TAG", "Variable is "+rasp);
			} catch (ParseException | IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
			return null;
		}
		
	}
}


