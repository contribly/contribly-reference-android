# Contribly reference Android app

A basic Android application which demonstrates read/write usages of the Contribly API in a native app setting.

Shows a list of open assignments allowing users to browser moderated contributions.
New contributions can be posted to open assignments.

Acts as a share target for images.

Contribly's preferred approach to native app development is that SDKs should be generated from the Swagger API definition rather than been hand crafted for each platform.
This app is used internally to validate that this approach works in practise.


## Building

This project is an Android Studio gradle build.
It should import correctly into a copy of Android Studio which has Android SDK 28 or higher installed.


### Generate the client using Swagger codegen

This application uses a Java client library auto generated from the Contribly API's Swagger definition.

To generate a local copy of this client follow this steps:

```
git clone https://github.com/swagger-api/swagger-codegen.git
mvn clean install
java -jar modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate -i https://api.contribly.com/1/swagger.json -l java --group-id com.contribly.client --api-package com.contribly.client.api --invoker-package com.contribly.client --model-package com.contribly.client.model --library=okhttp-gson -DserializableModel=true -o /tmp/contribly
```

Build the client and publish to your local Maven repo


```
cd /tmp/contribly
mvn clean package
```

### Build the apk

```
./gradlew assemble
```

### Credentials

The default API credentials shown in the preferences.xml file are for demonstration proposes only and may change without warning.
You should be replace them with your own client credentials.
