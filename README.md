# Temerarious Thirteens - Project 2 for CSE-4100

## How to start

Stub

```sh
sudo bash deploy.sh
```

Login credentials for the application:
* User: ?
* Password: ?

## Extras
Besides the Android application fully working according to the project description, we have implemented two of the proposed challenges:
* Facebook authentication.
* Context-awareness, in case the user loses the internet connection after login.

## In this repository you will find

### Documentation
In this folder you can find the file _cluster_commands.txt_ with *docker*, *gcloud* and *kubernetes* commands, and the file _remote_ocr_processing.txt_ with detailed information on how the http requests work in this application.

### Server


### TOCR-mobileUI
This is where the Android application source code is. Under _app/src/main/java/com/temerarious/mccocr13/temerariousocr/_ you can find the classes. They are separated in four groups: *activities*, with the actual activities of the application; *fragments*, basically with the fragment to handle the Facebook authentication; *helpers*, with several classes and methods to support the activities; and *tasks*, with the local and async tasks to run the OCR and communicate with the server.
