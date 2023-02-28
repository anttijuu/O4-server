# O3-chat-server

A Java chat server implemented for Programming 4 course. The course teachers client side GUI programming.

For this, the `chatserver.properties` contains settings one can use to modify the behaviour of the server.

The server uses a self signed certificate.

## Messages

Message types:

| ID   | Message Type |
|------|--------------|
| 1    | Register user|
| 2    | Login user   |
| 3    | Chat message |
| 4    | Message array|
| 5    | Join channel |
| 98   | Status msg   |
| 99   | Error msg    |

Register message:
```JSON
{
	"type": 1,
	"userName" : "username here",
	"password" : "password-here",
	"email" : "email@here.fi"
}
```
Login message:
```JSON
{
	"type": 2,
	"userName" : "username here",
	"password" : "password-here"
}
```
Chat message:
```JSON
{
	"type": 3,
	"userName" : "username here",
	"message" : "some message here",
	"timestamp" : 234141352
}
```
Chat message array:
```JSON
[
	"type": 4,
	{
		"userName" : "username here",
		"message" : "some message here",
		"timestamp" : 234141352
	},
	{
		"userName" : "username here",
		"message" : "some message here",
		"timestamp" : 234141352
	}
]
```

Join channel message:
```JSON
{
	"type": 5,
	"channel" : "channel",
}
```


Status message:
```JSON
{
	"type": 98,
	"message" : "some status message here"
}
```
Error message:
```JSON
{
	"type": 99,
	"message" : "some error message here"
}
```



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

