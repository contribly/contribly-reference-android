#Contribly reference Android client

A basic Android application which demonstrates basic read/write usages of the Contribly API in a mobile setting.

Provides a list of open assignments, views moderated contributions and allows new contributions to be posted.

Acts as an Android share target for images.

## Building

The project is layed out in Android Studio format. It should import correctly into a copy of Android Studio which has Android SDK 23 or higher installed.


###Generate the client using Swagger codegen

This application uses a Java client library auto generated from the Contribly API's Swagger definition.

To generate a local copy of this client follow this steps:

    git clone https://github.com/swagger-api/swagger-codegen.git
    mvn clean install
    java -jar modules/swagger-codegen-cli/target/swagger-codegen-cli.jar generate -i https://api.contribly.com/1/swagger.json -l java --group-id com.contribly.client --api-package com.contribly.client.api --invoker-package com.contribly.client --model-package com.contribly.client.model --library=okhttp-gson -DserializableModel=true -o /tmp/contribly

Build the client and publish to your local Maven repo

    cd /tmp/contribly
    mvn clean package


###Build the apk

    ./gradlew assemble