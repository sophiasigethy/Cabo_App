This is Group3´s project for the practical Master Course "Mobile und verteilte Systeme" at LMU Munich in Winter term 20/21.  
The project was to create a Multiplayer Online Card game for Android OS called Cabo.  
This repository contains 2 projects, which are neede for the game.  

- **CaboServer** is a Java-Project containing a SpringServer and everything else needed to deploy a Server through which the Clients can communicate. 

- **CaboClient** contains the Android App.  


If you want to develop for the Client, make sure, that you have added your SHA1 fingerprint to our Google Cloud API-Key.   
Otherwise you will not be able to connect to the Firebase Realtime Database, which might cause bad behavior.