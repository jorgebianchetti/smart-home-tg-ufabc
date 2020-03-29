# Smart Home Undergraduate Degree Final Project

## What is this??
This project was made for the subject "Undergraduate Degree Project" of the course Instrumentation, Automation and Robotics Engineering Bachelor Degree at Federal University of ABC. It consists of a home automation system having a central hub, which communicates with some peripherals (for now, a smart power plug and/or an LPG sensor) and stores their data into a Firebase Real Time Database. These data can also be accessed by a smartphone thus controlling the peripherals.

## Is there any innovation in this project??
Yeah! The main idea was to use Bluetooth 5 for the communication between the hub and the peripherals, because this technology is not yet as widespread as its previous versions are. This new version features long range and high throughput modes, perfect for IoT applications.

## How did you do it?
The project was divided into a few parts:

### Hardware
For the peripherals, some standalone boards were created for simplify the use of the Texas Instruments' CC2640R2F Bluetooth microcontroller. General purpose PCBs were also used to connect the remaining electronics components together.

The hub was made of a NodeMCU, for the internet and Wi-Fi stuff, and a CC2640R2F LaunchKit, for the communication with the peripherals over Bluetooth 5.

### Firmware
The [firmware](Bluetooth-5/Firmware/Peripheral) for the peripherals' microcontroller was developed using the Texas Instruments' Code Composer Studio software and the "project_zero" source code included in the Texas Instruments' Simplelink SDK as reference.

The [firmware](Bluetooth-5/Firmware/Hub) for the CC2640R2F LaunchKit used in the hub was also developed using the Code Composer Studio, with the "project_central" source code as reference.

The [firmware](Firebase/Arduino/Firebase-Source) for the NodeMCU used in the hub was developed using the Arduino IDE and the [FirebaseArduino](https://github.com/FirebaseExtended/firebase-arduino) and [WiFiManager](https://github.com/tzapu/WiFiManager) libraries.

### Android App Software
The [Android app](Android-App/Android-Studio/TGIARAPP/) was developed using the Android Studio software, in Java language.

Both NodeMCU and Android app communicate with the Firebase Server.

I hope you all enjoy it! :smiley:
