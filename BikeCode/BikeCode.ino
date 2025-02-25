#include "Config.h"
int pointsCount = 0;

void setup() {
  Serial.begin(115200);
  Serial.println("OK");
  SerialGPS.begin(9600, SERIAL_8N1, 16, 17);
  //  Serial2.begin(9600);

  initPins();
  initFirebase();
  connectionEstablished();
  delay(1000);

  read_data();
  total = TotalDist.toDouble();
  totalOil = KmDrivenOil.toDouble();
  locationStart = false;
  pointsCount = 0;
}

uint32_t pmillis = 0;
unsigned long previousMillis = millis();
bool enabled = false, lcdswap = false;

void loop() {
  while (SerialGPS.available() > 0) {
    if (gps.encode(SerialGPS.read())) {
      gps.f_get_position(&flat, &flon, &age);
      digitalWrite(2, HIGH);
      Speed = String(gps.f_speed_kmph());
    }
  }

  read_data();

  // Bike On/Off Feature
  lcd.clear();
  check_bikestatus();
  checkObstacle();
  bikeonoff();
  midnightReset();
  

  if (bikestatus == "OFF") {
    lcd.setCursor(0, 1);
    lcd.print(lcddisplay);
  } else if (bikestatus == "ON") {
    int spd = Speed.toInt();
    if (spd > 10) {
      if (lcdswap) {
        lcd.setCursor(0, 1);
        lcd.print("person");
        delay(250);
      } else if (lcdswap == false) {
        lcd.setCursor(0, 1);
        lcd.print("car");
        delay(250);
      }
    }
  }

  //Get Distance
  getDistance();

  //  Serial.print(flat);
  //  Serial.print("\t");
  //  Serial.println(flon);
  send_data();

  // if WiFi is down, try reconnecting
  if ((WiFi.status() != WL_CONNECTED) && (millis() - previousMillis >= 10000)) {
    Serial.println("Reconnecting to WiFi...");
    WiFi.disconnect();
    WiFi.reconnect();
    Serial.println("Reconnected");
    previousMillis = millis();
  }
}


// **************************************************************************************************************
void getDistance() {
  if (bikestatus == "ON" && locationStart == false) {
    locationStart = true;
    start_lat = flat;
    start_lon = flon;
    if (addcode == 2) {
      addcode = 3;
    } else {
      addcode = 1;
    }
  } else if (bikestatus == "OFF" && locationStart) {
    locationStart = false;
    double dist = calculateDistance(start_lat, start_lon, flat, flon);

    pointsCount++;
    if (pointsCount == 1) {
      PointA_Dist = String(dist);
      addcode = 2;
    } else if (pointsCount == 2) {
      PointB_Dist = String(dist);
      addcode = 4;
    }
    total += dist;
    totalOil += dist;
    daily += dist;

    TotalDist = String(total);
    DailyDist = String(daily);
    KmDrivenOil = String(totalOil);
    Serial.println("Addcode : " + String(addcode));
  }
}

void checkObstacle() {
  if (bikestatus == "ON") {
    double distance = read_ultra(trig_main, echo_main);
    lcd.setCursor(0, 0);
    lcd.print(distance);
    Serial.print("Distance : ");
    Serial.println(distance);
    if (distance <= 200.0 && distance > 0.0) {
      obstacle = "1";
    } else {
      obstacle = "0";
    }
    sensorsUpdate();
  }
}

void midnightReset() {
  if (millis() - pmillis >= 20000) {
    char output_hours[3];
    strftime(output_hours, 3, "%H", &timeinfo);
    //    Serial.println("Hour : " + String(output_hours));
    if (String(output_hours) == "0" && rst == false) {
      pointsCount = 0;
      DailyDist = "0";
      rst = true;
      addcode = 0;
    } else if (String(output_hours) == "1" && rst) {
      rst = false;
    }
  }
}

void bikeonoff() {
  if (Bikeoffbtn == "0" && helmetLimit == "1") {
    BikeON();
  } else {
    BikeOFF();
  }
}

void check_bikestatus() {
  float VIN = scanbikeSW();
  if (VIN > 10.0) {
    bikestatus = "ON";
  } else {
    bikestatus = "OFF";
  }
}

void send_data() {
  if (Firebase.ready() && signupOK && (millis() - sendDataPrevMillis > 3500)) {
    digitalWrite(2, LOW);
    Firebase.RTDB.setString(&fbdo, "Buttons/bikeSW", bikestatus);
    Firebase.RTDB.setFloat(&fbdo, "Location/Lattitude", flat);
    Firebase.RTDB.setFloat(&fbdo, "Location/Longitude", flon);
    Firebase.RTDB.setString(&fbdo, "Users/RiderSpeed", Speed);
    Firebase.RTDB.setString(&fbdo, "KmDriven/Total", TotalDist);
    Firebase.RTDB.setString(&fbdo, "KmDriven/kmA", PointA_Dist);
    Firebase.RTDB.setString(&fbdo, "KmDriven/kmB", PointB_Dist);
    Firebase.RTDB.setString(&fbdo, "KmDriven/dailyKM", DailyDist);
    Firebase.RTDB.setString(&fbdo, "OilChange/KmDrivenOil", KmDrivenOil);
    Firebase.RTDB.setString(&fbdo, "Obstacle/detected", obstacle);
    Firebase.RTDB.setString(&fbdo, "Sensors/sensor1", sensor1);
    Firebase.RTDB.setString(&fbdo, "Sensors/sensor2", sensor2);
    if (addcode == 1) {
      Firebase.RTDB.setString(&fbdo, "KmDriven/add1", address);
    } else if (addcode == 2) {
      Firebase.RTDB.setString(&fbdo, "KmDriven/add2", address);
    } else if (addcode == 3) {
      Firebase.RTDB.setString(&fbdo, "KmDriven/add3", address);
    } else if (addcode == 4) {
      Firebase.RTDB.setString(&fbdo, "KmDriven/add4", address);
      addcode = 5;
    }
    sendDataPrevMillis = millis();
  }
}

void read_data() {
  if (Firebase.ready() && signupOK && (millis() - readDataPrevMillis > 2000)) {
    Serial.println("Read data !");
    if (Firebase.RTDB.getString(&fbdo, "/Buttons/BikeOffBtn")) {
      if (fbdo.dataType() == "string") {
        Bikeoffbtn = fbdo.stringData();
      }
    }
    if (Firebase.RTDB.getString(&fbdo, "/OilChange/KmDrivenOil")) {
      if (fbdo.dataType() == "string") {
        KmDrivenOil = fbdo.stringData();
      }
    }
    if (Firebase.RTDB.getString(&fbdo, "/Location/address")) {
      if (fbdo.dataType() == "string") {
        address = fbdo.stringData();
      }
    }
    if (Firebase.RTDB.getString(&fbdo, "/Buttons/limitSW")) {
      if (fbdo.dataType() == "string") {
        helmetLimit = fbdo.stringData();
      }
    }
    if (Firebase.RTDB.getString(&fbdo, "/KmDriven/Total")) {
      if (fbdo.dataType() == "string") {
        TotalDist = fbdo.stringData();
      }
    }
    if (Firebase.RTDB.getString(&fbdo, "/LCD/detected")) {
      if (fbdo.dataType() == "string") {
        lcddisplay = fbdo.stringData();
      }
    }
    readDataPrevMillis = millis();
  }
}
