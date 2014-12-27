package com.cric.own;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.net.ssl.HttpsURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.RecognizerIntent;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class Main extends Activity {
	
	private static final int TEMPERATURE = 0 ;
	private static final int CO2 = 1 ;
	private static final int LIGHT = 2 ;
	private static final int HUMIDITY = 3 ;
	
	private WifiManager wifiManager;
	
	private EditText textBox;
	private String ACCESS_TOKEN ;
	private String EMAIL ;
	private String PASSWORD;
	private String DEVICE;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		EMAIL = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("username", null);
		PASSWORD = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("password", null);
		ACCESS_TOKEN = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("access_token", null);
		DEVICE = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("device", null);
		
		if(EMAIL == null || PASSWORD == null || ACCESS_TOKEN == null || DEVICE == null){
			Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
			startActivity(intent);
		}
			
		
		wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		ImageView banner = (ImageView)findViewById(R.id.banner);
		
		ObjectAnimator animator_banner_text = ObjectAnimator.ofFloat(banner, View.TRANSLATION_Y,(-1)*getWindowManager().getDefaultDisplay().getHeight(),0);
		animator_banner_text.setRepeatCount(0);
		animator_banner_text.setDuration(1000);
		animator_banner_text.setRepeatMode(ValueAnimator.INFINITE);
		animator_banner_text.start();
		
		
		textBox = (EditText)findViewById(R.id.text_box);
		textBox.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if(haveNetworkConnection()){
				if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER){
					hideSoftKeyboard();
					Integer operation = getFeature(textBox.getText().toString());
					if(operation == TEMPERATURE){
						new SparkCoreConnection().execute("text","temperature");
						Toast.makeText(getApplicationContext(), "Retrieving temperature", Toast.LENGTH_LONG).show();
					}
					if(operation == LIGHT){
						new SparkCoreConnection().execute("text","light");
						try {
							String light_level = new SparkGetAsync().execute("light").get();
							Toast.makeText(getApplicationContext(), "Light level = "+ light_level, Toast.LENGTH_LONG).show();
							
							if(light_level.equals("0") || light_level.equals("1") || Integer.parseInt(light_level) > 4000){
								light_level = new SparkGetAsync().execute("light").get();
							}
							Intent lightIntent = new Intent(getApplicationContext(),ResultActivity.class);
							lightIntent.putExtra("type", "light");
							lightIntent.putExtra("value", Integer.parseInt(light_level));
							startActivity(lightIntent);
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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
		
		ObjectAnimator animator_text_box = ObjectAnimator.ofFloat(textBox,View.ALPHA,0.10f,1,1);
		animator_text_box.setRepeatCount(0);
		animator_text_box.setDuration(3000);
		animator_text_box.setRepeatMode(ValueAnimator.INFINITE);
		animator_text_box.start();
		
		final Button speak = (Button)findViewById(R.id.speak);
		speak.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent speechIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
				speechIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, "en-US");
				
				startActivityForResult(speechIntent, 200);
				
			}
		});
		speak.setAlpha(0f);
		ObjectAnimator animator_speak_button = ObjectAnimator.ofFloat(speak, View.TRANSLATION_X, (-1)*getWindowManager().getDefaultDisplay().getWidth(),0);
		animator_speak_button.setRepeatCount(0);
		animator_speak_button.setDuration(1000);
		animator_speak_button.setRepeatMode(ValueAnimator.INFINITE);
		animator_speak_button.setStartDelay(500);
		animator_speak_button.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				speak.setAlpha(1f);
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		animator_speak_button.start();
		
		final Button WiFi = (Button)findViewById(com.cric.own.R.id.wifi);
		WiFi.setAlpha(0f);
		ObjectAnimator animator_wifi_button = ObjectAnimator.ofFloat(WiFi, View.TRANSLATION_X, getWindowManager().getDefaultDisplay().getWidth(),0);
		animator_wifi_button.setRepeatCount(0);
		animator_wifi_button.setDuration(1000);
		animator_wifi_button.setRepeatMode(ValueAnimator.INFINITE);
		animator_wifi_button.setStartDelay(750);
		animator_wifi_button.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				WiFi.setAlpha(1f);
				
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		animator_wifi_button.start();
		
		WiFi.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				createWifiAccessPoint(); //start the tethering process
				final AlertDialog.Builder builder = new AlertDialog.Builder(Main.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				AlertDialog.Builder info = new AlertDialog.Builder(Main.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				info.setTitle("Nu te asteptai la asta");
				TextView informatii = new TextView(getApplicationContext());
				informatii.setText("Pentru a muta Spark Core-ul in alta parte trebuie sa te asiguri ca ai Wi Fi acolo unde vrei sa il muti" +
						".Daca nu ai Wi Fi atunci apasa butonul de mai" +
						" jos si adauga credentialele noului punct de acces de internet");
				informatii.setGravity(Gravity.CENTER);
				informatii.setTextSize(20);
				info.setView(informatii);
				
				info.setPositiveButton("Asa o sa fac", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						builder.create().show();
					}
				});
				info.setNegativeButton("Nu vreau !!", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						if(!wifiManager.isWifiEnabled()){
							wifiManager.setWifiEnabled(true);
							//close the tethering
						}
						
					}
				});
				
				info.create().show();
				
				final EditText SSID = new EditText(getApplicationContext());
				SSID.setHint("SSID");
				
				final EditText Password = new EditText(getApplicationContext());
				Password.setHint("Parola");
				Password.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
				
				
				builder.setTitle("Introduceti credentiale");
				builder.setCancelable(true);
				builder.setPositiveButton("Gata", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Toast.makeText(getApplicationContext(), "Cerere trimisa", Toast.LENGTH_LONG).show(); 
						new SparkCoreConnection().execute("credentials",SSID.getText().toString()+" "+Password.getText().toString());
						
						if(!wifiManager.isWifiEnabled()){
							wifiManager.setWifiEnabled(true);
							//close the tethering
						}
					}
				});
				builder.setNegativeButton("Renunta", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						if(!wifiManager.isWifiEnabled()){
							wifiManager.setWifiEnabled(true);
							//close the tethering
						}
						
					}
				});
				LinearLayout WiFiLayout = createLinearLayout(SSID,Password);						
				builder.setView(WiFiLayout);
//				builder.create().show();
			}
		});
		
		final Button settings = (Button)findViewById(R.id.settings);
		settings.setAlpha(0f);
		settings.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(),SettingsActivity.class);
				startActivity(intent);
			}
		});
		
		ObjectAnimator animator_settings_button = ObjectAnimator.ofFloat(settings, View.TRANSLATION_Y, getWindowManager().getDefaultDisplay().getHeight(),0);
		animator_settings_button.setRepeatCount(0);
		animator_settings_button.setDuration(1000);
		animator_settings_button.setRepeatMode(ValueAnimator.INFINITE);
		animator_settings_button.setStartDelay(1000);
		animator_settings_button.addListener(new AnimatorListener() {
			
			@Override
			public void onAnimationStart(Animator animation) {
				settings.setAlpha(1f);
			}
			
			@Override
			public void onAnimationRepeat(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationEnd(Animator animation) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onAnimationCancel(Animator animation) {
				// TODO Auto-generated method stub
				
			}
		});
		animator_settings_button.start();
	
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
							String light_level = new SparkGetAsync().execute("light").get();
							Toast.makeText(getApplicationContext(), "Light level = "+ light_level, Toast.LENGTH_LONG).show();
							if(light_level.equals("0") || light_level.equals("1") || Integer.parseInt(light_level) > 4000){
								light_level = new SparkGetAsync().execute("light").get();
							}
							Intent lightIntent = new Intent(getApplicationContext(),ResultActivity.class);
							lightIntent.putExtra("type", "light");
							lightIntent.putExtra("value", Integer.parseInt(light_level));
							startActivity(lightIntent);
						} catch (InterruptedException | ExecutionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
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

	private LinearLayout createLinearLayout(View...views ){
		LinearLayout layout = new LinearLayout(getApplicationContext());
		layout.setOrientation(LinearLayout.VERTICAL);
		for(int i = 0 ; i < views.length; i++){
			layout.addView(views[i]);
		}
		return layout;
	}
	
	private void hideSoftKeyboard(){
	    if(getCurrentFocus()!=null && getCurrentFocus() instanceof EditText){
	        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
	        imm.hideSoftInputFromWindow(textBox.getWindowToken(), 0);
	        imm.hideSoftInputFromInputMethod(textBox.getWindowToken(), 0);
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
				URL url = new URL("https://api.spark.io/v1/devices/" + DEVICE + "/"+params[0]+"/");
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				con.setRequestMethod("POST");
				String urlParameters = "access_token=" + ACCESS_TOKEN + "&args=" + " "+(params[1]+" ");
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
			String value = null;
			try {
				URL url = new URL("https://api.spark.io/v1/devices/" + DEVICE + "/"+params[0]+"?access_token=" + ACCESS_TOKEN);
				HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
				con.setRequestMethod("GET");
				
				StringBuilder response = new StringBuilder();
				BufferedReader reader = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line ;
				
				while((line = reader.readLine()) !=null){
					response.append(line);
				}
				value = response.toString();
				Log.d("Own", "Response is "+response) ;
				
				JSONObject root = new JSONObject(response.toString());
				value = root.getString("result");
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
	
	private void createWifiAccessPoint() {
		
		
	    if(wifiManager.isWifiEnabled())
	    {
	        wifiManager.setWifiEnabled(false);          
	    }       
	    Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();   
	    boolean methodFound=false;
	    for(Method method: wmMethods){
	        if(method.getName().equals("setWifiApEnabled")){
	            methodFound=true;
	            WifiConfiguration netConfig = new WifiConfiguration();
	            netConfig.SSID = "AccessPoint"; 
	            netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
	            try {
	                boolean apstatus=(Boolean) method.invoke(wifiManager, netConfig,true);          
	                for (Method isWifiApEnabledmethod: wmMethods)
	                {
	                    if(isWifiApEnabledmethod.getName().equals("isWifiApEnabled")){
	                        while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager)){
	                        };
	                        for(Method method1: wmMethods){
	                            if(method1.getName().equals("getWifiApState")){
	                                int apstate;
	                                apstate=(Integer)method1.invoke(wifiManager);
	                            }
	                        }
	                    }
	                }
	                if(apstatus)
	                {
	                    Log.d("Splash Activity", "Access Point created");   
	                }else
	                {
	                    Log.d("Splash Activity", "Access Point creation failed");   
	                }

	            } catch (IllegalArgumentException e) {
	                e.printStackTrace();
	            } catch (IllegalAccessException e) {
	                e.printStackTrace();
	            } catch (InvocationTargetException e) {
	                e.printStackTrace();
	            }
	        }      
	    }
	}
}


