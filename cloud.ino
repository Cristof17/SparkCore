bool on ;
int light =0;
String SSID ;
String password;

void setup(){
	Spark.function("text",text);
	Spark.variable("light",&light,INT);
	Serial.begin(9600);
	pinMode(D7,OUTPUT);
	Spark.function("credentials",setCredentials);
	
}

void loop(){
	if(on){
		digitalWrite(D7,LOW);
	}else{
		digitalWrite(D7,HIGH);
	}

	light = analogRead(A2);
	Serial.print("Value from sensor ");
	Serial.println(light);
	delay(1000);
}

int text(String args){
	if(on)
		on = false;
	else
		on = true ;
	return 0 ;
}

int setCredentials(String args){
	Serial.println(args);
	char buff[args.length()];
	args.toCharArray(buff ,args.length());
	int i ;
	Serial.print("args length ");
	Serial.println(args.length());
	for(i = 1 ; i < args.length(); i++){
		if(buff[i] == ' ' || buff[i] == '\0'){
			break;
		}
		else{
			SSID.concat(buff[i]);		
		}
	}

	for(++i; i < args.length(); i++){
		if(buff[i] == ' ' || buff[i] == '\0')
			break;
		else{
			password.concat(buff[i]);		
		}
	}

	WiFi.setCredentials(SSID.c_str(), password.c_str());

	Serial.print("SSID =");
	Serial.println(SSID);

	Serial.print("Password =");
	Serial.println(password);
	return 0 ; 
}

