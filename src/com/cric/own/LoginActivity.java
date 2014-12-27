package com.cric.own;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		if(prefs.getString("email", null) == null){
			
		}else{
			if(prefs.getString("email", null) != null && prefs.getString("password", null) != null ){
				Intent  mainIntent = new Intent(getApplicationContext(),Main.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(mainIntent);
			}
		}
		
	final EditText username = (EditText)findViewById(R.id.user_edit_text);
	final EditText password = (EditText)findViewById(R.id.password_edit_text);
	
	
		
	Button login = (Button)findViewById(R.id.login);
	login.setOnClickListener(new View.OnClickListener() {
		
		@Override
		public void onClick(View v) {
			if(username.getText().toString() == "" || password.getText().toString() == ""){
				Toast.makeText(getApplicationContext(), "Introduceti toate credentialele", Toast.LENGTH_LONG).show();
			}else{
				final AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_DARK);
				final EditText access_token = new EditText(getApplicationContext());
				builder.setView(access_token);
				builder.setTitle("Introduceti access_token-ul");
				builder.setPositiveButton("Gata", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface arg0, int arg1) {
						prefs.edit().putString("email", username.getText().toString()).commit();
						prefs.edit().putString("password", password.getText().toString()).commit();
						prefs.edit().putString("access_token", access_token.getText().toString()).commit();
						
					}
				});
				builder.setNegativeButton("Renunt", new OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						
					}
				});
				builder.create().show();
				Intent  mainIntent = new Intent(getApplicationContext(),Main.class);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(mainIntent);
			}
		}
	});
	}
	
}
