<h1 align="center"><img src="https://user-images.githubusercontent.com/68066041/158012063-d7ba8fe3-8df6-48b8-abd6-01f25109f873.png"></h1>

<p align="center"><b>MyCelsius</b> is a temperature submission and tracking automation application for organisations. It seeks to allow for a quicker and more simplified way for staff members or visitors to submit their temperature for their organisation without sacrificing the lack of verification from the submission and ease of use during the COVID-19 pandemic. </p>

<p align="center"><b>MyCelsius</b> is a JavaFX application integrated with Firebase using Firebase Admin SDK that I developed as a school project. An Internet connection is required for the application to work. </p>

---

### Goals of MyCelsius
The project's goals included two major aspects:
 - JavaFX user application where users manage and view temperature submission records
 - Telegram Bot that facilitate the temperature recording process with the recording and verification of the submission records

This repository combines both the Telegram Bot and the user application. Therefore, running the program will start both the JavaFX user application and the Telegram Bot server. The project integrates with Firebase using the Firebase Admin SDK. The merging of the functionalities is mainly to achieve a minimum viable product (MVP) without complicating the software stack, but mainly to satisfy the project submission requirements.

---

### Overview of Features

![MyCelsiusUI](https://github.com/Kzeezee/MyCelsius/assets/68066041/1777946b-7cc9-4441-8b24-dc80c401aa65)

The user application supports:
 - Organisation creation and management
 - Organisation members creation and management
 - Temperature submission recording for verified organisation members and guests
 - Temperature submission history
 - Temperature submission details

The Telegram Bot supports:
 - Temperature submission for an organisation for both organisation members and guests
 - Verification for organisation members using the user's Telegram user ID
 - Validation for the temperature submission process

---

### Additional Requirements
* Active internet connection
* Firebase project with Firestore
* Telegram bot
* .env file with *"TELEGRAM_BOT_TOKEN"* and *"SERVICE_ACCOUNT_JSON_PATH"* filled with your Telegram bot's token and your Firebase project's service account json respectively
