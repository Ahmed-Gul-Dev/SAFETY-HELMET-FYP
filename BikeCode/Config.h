#include <Wire.h>
#include <LiquidCrystal_I2C.h>
#include <WiFi.h>
#include <Firebase_ESP_Client.h>
// #include <AsyncTCP.h>
// #include <ESPAsyncWebServer.h>
// #include <AsyncElegantOTA.h>
#include <TinyGPS.h>
#include "time.h"

struct tm timeinfo;

const char* ntpServer = "pool.ntp.org";
const long  gmtOffset_sec = 0;
const int   daylightOffset_sec = 18000;

HardwareSerial SerialGPS(1);
TinyGPS  gps;

//Provide the token generation process info.
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions
#include "addons/RTDBHelper.h"

// Insert your network credentials
#define WIFI_SSID "ECC"
#define WIFI_PASSWORD "12345678"

// AsyncWebServer server(80);

// Insert Firebase project API Key
#define API_KEY "AIzaSyDV7efOZkYOo_wMw8Zo3MlzHQlzzP0NSA4"

// Insert RTDB URLefine the RTDB URL */
#define DATABASE_URL "https://safetyhelmet-6bdea-default-rtdb.firebaseio.com/"

//Define Firebase Data object
FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

uint32_t sendDataPrevMillis = 0;
uint32_t readDataPrevMillis = 0;
bool signupOK = false;
volatile bool dataChanged = false;

LiquidCrystal_I2C lcd(0x27, 16, 2);

//Connections
const int trig_main = 18;
const int echo_main = 19;

const int r1 = 23;
const int r2 = 25;
const int r3 = 26;
const int r4 = 27;
const int voltagepin = 34;

const double EARTH_RADIUS_KM = 6371.0; // Earth's radius in kilometers

//Oil Change
String KmDrivenOil = "0";
double totalOil = 0.0;

//Location
String Speed = "0", address = " ";
float latt = 0.0, longg = 0.0;
float start_lat, start_lon, flat = 0.0, flon = 0.0;
unsigned long age;
int addcode = 0;
bool locationStart = false;


// Buttons
String Bikeoffbtn = "0", lcddisplay = " ", helmetLimit = " ";
String bikestatus = "0"; // monitor bike SW

// Obstacle Detection
String obstacle = "0";

//Sensors
String sensor1 = "0", sensor2 = "0";

//Km Driven
bool rst = false;
String TotalDist = "0";
String DailyDist = "0";
String PointA_Dist = "0";
String PointB_Dist = "0";
double total = 0.0, daily = 0.0;

// Floats for resistor values in divider (in ohms)
float R1 = 22000.0;
float R2 = 2200.0;
float ref_voltage = 3.3;
// Integer for ADC value
int adc_value = 0;

// Function to convert degrees to radians
double toRadians(double degrees) {
  return degrees * PI / 180.0;
}

// Function to calculate the distance using the Haversine formula
double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
  double dlat = toRadians(lat2 - lat1);
  double dlon = toRadians(lon2 - lon1);

  double a = sin(dlat / 2) * sin(dlat / 2) +
             cos(toRadians(lat1)) * cos(toRadians(lat2)) *
             sin(dlon / 2) * sin(dlon / 2);

  double c = 2 * atan2(sqrt(a), sqrt(1 - a));
  double distance = EARTH_RADIUS_KM * c;

  return distance;
}


void initPins() {
  pinMode(voltagepin, INPUT);
  pinMode(echo_main, INPUT);
  pinMode(trig_main, OUTPUT);

  pinMode(2, OUTPUT);
  digitalWrite(2, LOW);

  pinMode(r1, OUTPUT);
  digitalWrite(r1, HIGH);
  pinMode(r2, OUTPUT);
  digitalWrite(r2, HIGH);
  pinMode(r3, OUTPUT);
  digitalWrite(r3, HIGH);
  pinMode(r4, OUTPUT);
  digitalWrite(r4, HIGH);

  // Print a message to the LCD.
  lcd.init();
  lcd.backlight();
  lcd.clear();
  delay(500);
}

float scanbikeSW() {
  float Vin = 0.0, Vout = 0.0;
  // Read the Analog Input
  adc_value = analogRead(voltagepin);
  Vout  = (adc_value * ref_voltage) / 4095.0;

  // Calculate voltage at divider input
  Vin = ((Vout * (R1 + R2) / R2)) + 1.5;
  return Vin;
}

void BikeON() {
  digitalWrite(r1, HIGH);
  digitalWrite(r2, HIGH);
  digitalWrite(r3, HIGH);
  digitalWrite(r4, HIGH);
}

void BikeOFF() {
  digitalWrite(r1, LOW);
  digitalWrite(r2, LOW);
  digitalWrite(r3, LOW);
  digitalWrite(r4, LOW);
}

void initFirebase() {
  // WiFi.mode(WIFI_STA);
  // Firebase Initialization
  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());

  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print(WiFi.localIP());
  Serial.println("Wifi Connected !!");

  Serial.println();
  delay(1000);

  // Init and get the time
  configTime(gmtOffset_sec, daylightOffset_sec, ntpServer);
  if (!getLocalTime(&timeinfo)) {
    Serial.println("Failed to obtain time");
    return;
  }

  // server.on("/", HTTP_GET, [](AsyncWebServerRequest * request) {
  //   request->send(200, "text/plain", "Hi! I am ESP32.");
  // });

  // AsyncElegantOTA.begin(&server);    // Start ElegantOTA
  // server.begin();
  // Serial.println("HTTP server started");

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
  lcd.clear();
  lcd.setCursor(0, 0);
  lcd.print("Internet");
  lcd.setCursor(0, 1);
  lcd.print("Connected");
}

void connectionEstablished() {
  for (int i = 0; i < 5; i++) {
    digitalWrite(2, HIGH);
    delay(250);
    digitalWrite(2, LOW);
    delay(250);
  }
}

double read_ultra(const int trigPin , const int echoPin) {
  digitalWrite(trigPin, LOW);
  delayMicroseconds(2);
  digitalWrite(trigPin, HIGH);
  delayMicroseconds(20);
  digitalWrite(trigPin, LOW);

  // Reads the echoPin, returns the sound wave travel time in microseconds
  long duration = pulseIn(echoPin, HIGH);
  double distance = (duration * 0.034) / 2;
  return distance;
}

void sensorsUpdate() {
  if (sensor1 == "0") {
    sensor1 = "1";
  } else {
    sensor1 = "0";
  }
  if (sensor2 == "1") {
    sensor2 = "0";
  } else {
    sensor2 = "1";
  }
}
