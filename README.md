BII4
====

BII Project 4

2 parts
  + Android: BluetoothSpectralAnalyser
      Process audio input - do FFT and send the spectral analysis to the Desktop server via Bluetooth
      The data will be serialized into a byte array
      On Android device there is also a simple visualisation for debug purposes
      Binary (.apk) can be found under https://github.com/mirobyrtus/BII4/tree/master/BluetoothSpectrumAnalyser/app/build/outputs/apk    

  + Desktop (Java): RemoteBluetoothServer
      Retrieves the data, deserializes them
      Visualize the serialized data

!!! IMPORTANT for RemoteBluetoothServer: configure eclipse to pass the -d32 JVM argument, does not run in the 64 bit mode !!!

Used libraries: 
For FastFourierTransform : https://github.com/sommukhopadhyay/FFTBasedSpectrumAnalyzer
For Bluetooth : https://github.com/luugiathuy/Remote-Bluetooth-Android
Bluecove 2.1.0.: http://bluecove.org/
