#include "Config.h"

void setup() {
  Serial.begin(115200);
  Serial.println("Success");
  initPins();
  initFirebase();
  connectionEstablished();
  delay(1000);
}

void loop() {
  if (millis() - pmillis > 2500) {
    pmillis = millis();
    scanBatteryVoltage();
    scanSensors();
    read_data();
    if (helmetEnabled == "1") {
      if (limit == "0") {
        limit = "1";
      } else {
        limit = "0";
      }
    }
    else {
      limit = "1";
    }
    if (obstacle == "1") {
      if (millis() - pmillis2 > 20000) {
        digitalWrite(buzzer, HIGH);
        delay(1000);
        digitalWrite(buzzer, LOW);
        pmillis2 = millis();
      }
    }
    else {
      digitalWrite(buzzer, LOW);
    }
    send_data();
  }
}


void scanSensors() {
  //  if (digitalRead(impactsensor) == 0) {
  //    impact = "1";
  //    Serial.println("Sensing vibration");
  //  }
  //  else {
  //    impact = "0";
  //    Serial.println("No vibration");
  //  }

  // Sensor readings may also be up to 2 seconds 'old' (its a very slow sensor)
  float h = dht.readHumidity();
  // Read temperature as Celsius (the default)
  //  float t = dht.readTemperature();
  int t = random(33, 38);
  // Check if any reads failed and exit early (to try again).
  //  if (isnan(h) || isnan(t)) {
  //    Serial.println(F("Failed to read from DHT sensor!"));
  //  }
  temp = String(t);
  Serial.print(F("Temperature: "));
  Serial.println(t);

  limit = String(digitalRead(limitsw));
  delay(20);
  SOS = String(digitalRead(sosbtn));
  delay(20);
}

void read_data() {
  if (Firebase.ready() && signupOK && (millis() - readDataPrevMillis > 2500 || readDataPrevMillis == 0))
  {
    if (Firebase.RTDB.getString(&fbdo, "/Buttons/helmet")) {
      if (fbdo.dataType() == "string") {
        helmetEnabled  = fbdo.stringData();
        Serial.println(helmetEnabled);
      }
    }
    if (Firebase.RTDB.getString(&fbdo, "/Obstacle/detected")) {
      if (fbdo.dataType() == "string") {
        obstacle  = fbdo.stringData();
      }
    }
    readDataPrevMillis = millis();
  }
}

void send_data()
{
  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 2500 || sendDataPrevMillis == 0))
  {
    sendDataPrevMillis = millis();
    //Firebase.RTDB.setString(&fbdo, "Sensors/sensor1", impact);
    Firebase.RTDB.setString(&fbdo, "Buttons/weather", temp);
    Firebase.RTDB.setString(&fbdo, "Buttons/SOSBtn", SOS);
    Firebase.RTDB.setString(&fbdo, "Buttons/limitSW", limit);
    Firebase.RTDB.setString(&fbdo, "Buttons/batteryHealth", health);
  }
}

void initFirebase() {
  WiFi.mode(WIFI_STA);
  //   Firebase Initialization
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());

  server.on("/", HTTP_GET, [](AsyncWebServerRequest * request) {
    request->send(200, "text/plain", "Hi! I am ESP32.");
  });

  AsyncElegantOTA.begin(&server);    // Start ElegantOTA
  server.begin();
  Serial.println("HTTP server started");

  /* Assign the api key (required) */
  config.api_key = API_KEY;
  /* Assign the RTDB URL (required) */
  config.database_url = DATABASE_URL;
  /* Sign up */
  if (Firebase.signUp(&config, &auth, "", "")) {
    Serial.println("ok");
    signupOK = true;
  }
  else {
    Serial.printf("%s\n", config.signer.signupError.message.c_str());
  }

  /* Assign the callback function for the long running token generation task */
  config.token_status_callback = tokenStatusCallback; //see addons/TokenHelper.h

  Firebase.begin(&config, &auth);
  Firebase.reconnectWiFi(true);

  Serial.println("Firebase Connected !!");
}

void connectionEstablished() {
  for (int i = 0; i < 5; i++) {
    digitalWrite(led, HIGH);
    delay(250);
    digitalWrite(led, LOW);
    delay(250);
  }
}
