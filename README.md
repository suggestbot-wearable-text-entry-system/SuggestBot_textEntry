# SuggestBot Wearable Text Entry System

This is the main application for smart glasses.

Specifically, it had been coded for Moverio BT300 device, but it can be run with any other devices powered by Andorid OS.

However, if you use any other device, please re-set a keyboard layout dimension to consider a dimension of display on your device.

We also recommanded to use this program with SDK ver. of higher than 19, and its target SDK version is 26.


This program also used OTG to connect a edge-touch sensor for a smartwatch. 

To do this, we used 3rd Party program for USB-to-Serial (com.hoho.android:usb-serial-for-android:0.2.0-SNAPSHOT)


If you do not want or do not have edge-touch sensor with USB OTG connected, please be modify the code related to USB-to-Serial.

-------

This program work with TouchSender (https://github.com/suggestbot-wearable-text-entry-system/TouchSender). 

TouchSender is a program for a smartwatch, which is used to input device for a smart glssses.

You can also check the implementation details on my publication, Typing on a Smartwatch for Smart Glasses (ISS17) doi.org/10.1145/3132272.3134136 
