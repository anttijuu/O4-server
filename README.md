# O3-chat-server

A Java chat server implemented for Programming 4 course. The course teachers client side GUI programming.

For this, the `chatserver.properties` contains settings one can use to modify the behaviour of the server.

The server uses a self signed certificate.

## Building

Build the server (in the root source directory):

```console
mvn package
```

## Running 

First copy `chatserver-template.properties` to `chatserver.properties` and then edit the settings to reflect the features active in the server.

Then run the server:

```console
java -jar target/ChatServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar chatserver.properties s3rver-secr3t-d0no7-xp0s3
```

Where the first parameter is the configuration file you wish to use to run the server, and the second parameter is the password to the self-signed certificate server is using for HTTPS.

You can stop the server in a controlled way from the console with `/quit` command.

