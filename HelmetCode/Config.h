#include <WiFi.h>
#include <Firebase_ESP_Client.h>
#include <AsyncTCP.h>
#include <ESPAsyncWebServer.h>
#include <AsyncElegantOTA.h>
#include "DHT.h"

//Provide the token generation process info
#include "addons/TokenHelper.h"
//Provide the RTDB payload printing info and other helper functions.
#include "addons/RTDBHelper.h"

// Insert your network credentials
#define WIFI_SSID "ECC"
#define WIFI_PASSWORD "12345678"

AsyncWebServer server(80);

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

const int led = 2;
const int impactsensor = 32;
const int voltagepin = 39; //VN
const int sosbtn = 34;
const int limitsw = 35;
const int buzzer = 25;
#define DHTPIN 26     // Digital pin connected to the DHT sensor
#define DHTTYPE DHT11   // DHT 11
DHT dht(DHTPIN, DHTTYPE);

uint32_t pmillis = 0,pmillis2 = 0;

// Floats for ADC voltage & Input voltage
float Vout = 0.0;
float Vin = 0.0;
// Floats for resistor values in divider (in ohms)
float R1 = 22000.0;
float R2 = 2200.0;
float ref_voltage = 3.3;
// Integer for ADC value
int adc_value = 0;

// Cloud Parameters
String impact = " ", temp = " ", SOS = " ", limit = " ", health = " ",helmetEnabled=" ",obstacle;

void initPins() {
  pinMode(led, OUTPUT);
  digitalWrite(led, LOW);

  pinMode(impactsensor, INPUT); //Set pin as input
  pinMode(buzzer, OUTPUT);
  digitalWrite(buzzer, HIGH);
  delay(500);
  digitalWrite(buzzer, LOW);
  dht.begin();

  pinMode(impactsensor, INPUT);
  pinMode(voltagepin, INPUT);
  pinMode(sosbtn, INPUT);
  pinMode(limitsw, INPUT);
}

void scanBatteryVoltage() {
  // Read the Analog Input
  adc_value = analogRead(voltagepin);
  Vout  = (adc_value * ref_voltage) / 4095.0;

  // Calculate voltage at divider input
  Vin = ((Vout * (R1 + R2) / R2)) + 1.3;
  Vin = (Vin / 12.4) * 100;
  Serial.print("Voltage : ");
  Serial.println(Vin);
  health = String(Vin);
}
