BII4
====

BII Project 4

!!! IMPORTANT for RemoteBluetoothServer: configure eclipse to pass the -d32 JVM argument, does not run in the 64 bit mode !!!

RemoteBluetooth (Android) ALSO does the Spectral Analysis! 
Then it can connnect remotely via Bluetooth to RemoteBluetoothServer running on your PC. 
The data will be serialized into a byte[] and sent to the RemoteBluetoothServer. 

TODO: 
Create Visualisation to the data. The byte[] array filled with the data will be outputed into Console (for now) in the RemoteBluetoothServer program. 

Used Code: 
SpectralAnalyzer: https://github.com/sommukhopadhyay/FFTBasedSpectrumAnalyzer - Check how the data is used here, that will help u
Bluetooth: https://github.com/luugiathuy/Remote-Bluetooth-Android (IMPORTANT to run: configure eclipse to pass the -d32 JVM argument) 
