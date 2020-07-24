# Hi-Music-Android-App
Hi Music is an android application that enables the user to listen local music files available on the device.
Using the Android Service class this music player is able to play media even when the application is in background.
Implements the interface of MusicController(provided by android) and binds the UI thread with the Service class so that Android Services are able to interact with the hosting thread.
By implementing the BroadcastReceivers, the Hi Music application is able to the control the music Play from the lockscreen as well as from the the notification pannel. 
