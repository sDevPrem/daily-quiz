# Daily Quiz

A Quiz App made using firebase. It is based on Unidirectional Data Flow (UDF) Architecture and
Single-Activity Architecture.

## Built With

[**Kotlin:**](https://kotlinlang.org/) As the programming language.

[**Firebase:**](https://firebase.google.com/) As the backend for fetching quizes.

[**Navigation
Component:**](https://developer.android.com/guide/navigation/navigation-getting-started) For
navigating between destinations (fragments)

[**ViewModel:**](https://developer.android.com/guide/navigation/navigation-getting-started) For
cashing data and persists it through configuration changes.

[**Hilt:**](https://developer.android.com/training/dependency-injection/hilt-android) For dependency
injection.

[**Flow:**](https://developer.android.com/kotlin/flow) For flow of data from data layer to UI layer.

## Architecture

Follows Unidirectional Data Flow ([UDF](https://developer.android.com/topic/architecture)) fpr the
flow of data and [Single Activity Architecture](https://youtu.be/2k8x8V77CrU) for UI.

![android architecture](https://developer.android.com/static/topic/libraries/architecture/images/mad-arch-overview.png)

## Installation

Simply clone this repository and open it in android studio.

### Firebase Integration

See [Add Firebase to your Android project](https://firebase.google.com/docs/android/setup) to setup
connect the project with your firebase. Alternatively delete `app/google-services.json` and use
Android integrated Firebase Assistant for the setup
through `Tools/Firebase/<Any_Feature>/Connect your app to Firebase` and connect it with your
existing firebase project or create a new one. To understand the structure of firestore, see the
model classes in `data/model` package.
