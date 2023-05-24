# O4-server

This is a simple Java chat server implemented for Programming 4 course. The course teachers client side GUI programming. Students can use this server to develop their own GUI clients. A [sample console chat client](https://github.com/anttijuu/O4-client) can be used as a reference in GUI client development.

Underlying protocol the server and clients use is TCP. 

> Note that TLS is not used, so messages are passed over the network unencrypted.

## Messages

The messages between the client and server are passed as JSON. Each message has a type element (integer), specifiyng the meaning and structure of the message.

Message types are the following:

| ID   | Message Type         |
|------|----------------------|
| -1   | Error message        |
|  0   | Status message       |
|  1   | Chat message         |
|  2   | Join channel message |
|  3   | Change topic message |
|  4   | List channels message|

Java Message classes in `oy.tol.chat` package implement these message types. These classes can be used both in server side and in client side.

Converting Message objects to JSON text is implemented in `Message.toJSON()` methods. Parsing message objects from JSONObjects is done in `MessageFactory.fromJSON(JSONObject)`.

Message contents in JSON are specified below. 

> Note that the chat protocol requires that each individual message is encoded/decoded using UTF-8 and is terminated with newline `\n` character. The provided `Message` classes and the client side `ChatTCPClient` class as well as the server side `ChatServerSession` classes fulfill these requirements.

### Error message

Error messages are sent by the server only.

If the value of `clientshutdown` is different from zero (0), the error *requires* the clients to shut down the connection with the server. This implies the server is either shutting down (initiated by the admin) or has had a critical error and cannot continue. 

```JSON
{
	"type": -1,
	"error" : "Some error message here",
	"clientshutdown": 0
}
```

### Status message

Status messages are sent by the server only. They indicate some event on the server side clients or users may be interested in.

```JSON
{
	"type": 0,
	"status" : "Some status message here"
}
```

### Chat message

A client sends chat messages to the server, and server relays chat messages to other clients in the same *channel*.

A message sent by a client is *not* sent back to that same client by the server (echoed).

Below you can see a simple chat message, from user "telemakos". 

The `sent` element is a UTC timestamp in unix time. Clients must convert the local time to UTC before sending the `ChatMessage` to the server. Also, clients must convert the UTC time to client's local time before showing the time of the incoming messages to the user.

The `id` element must be a valid UUID.

```JSON
{
	"type" : 1,
	"id" : "d4986bec-8026-462a-9a7a-f04eebcf7612",
	"message" : "mutta lopettakaa te hurjistelunne ja vierailkaa",
	"user" : "telemakos", 
	"sent":16782697319
}
```
A chat message can also be a **reply to** another chat message. In this case, the JSON contains an *optional* element `inReplyTo`: 

```JSON
{
	"type" : 1,
	"id" : "d4986bec-8026-462a-9a7a-f04eebcf7612",
	"inReplyTo": "d4986bec-8026-462b-8a7a-f04dfbcf1115",
	"message" : "mutta lopettakaa te hurjistelunne ja vierailkaa",
	"user" : "telemakos", 
	"sent":16782697319
}
```
The UUID in the `inReplyTo` field *must* be a valid chat message sent *before* the reply message in the same channel.

A *private message* has an optional field `directMessageTo`:

```JSON
{
	"type" : 1,
	"id" : "d4986bec-8026-462a-9a7a-f04eebcf7612",
	"message" : "mutta lopettakaa te hurjistelunne ja vierailkaa",
	"user" : "telemakos", 
	"directMessageTo" : "athene", 
	"sent":16782697319
}
```
Here Telemakos is sending a private message to Athene. 

Private message is relayed only to the chat user connected to the server using the provided nick / user name. It is *not* relayed to other participants in any channel. 

> Note that the server cannot relay private messages to a user unless the user has sent at least one message using the nick in the current session with server. Note also that the server does not implement any means to make sure user nicks are unique. Private message is then relayed to the first nick found. If no user with that nick is found, the message is lost.

### Join channel message

User can move between channels in the server using join channel messages. If a channel with the specified name does not exist, server creates that channel.

When the last user leaves the channel, it is closed. The channel "main" is an exception. It is always created when the server starts, and is never closed. Clients connecting to the server are automatically joining the main channel.

```JSON
{
	"type": 2,
	"channel" : "ohjelmointi-4",
}
```

Users should keep the channel names short since displaying long channel names is inconvenient. Currently the server does not limit channel nor nick (user) name lengths.

### Change topic message

Change topic message changes the topic of the channel. Clients send this message, and server relays the topic to other clients on this channel.

```JSON
{
	"type": 3,
	"topic" : "ohjelmointi 4 -kurssiin liittyvää keskustelua"
}
```
The topic message is also send to client when it joins a channel.

### List channels message

Clients can send a list channels message to the server. Server then replies with the same type of a message listing the currently open channels on the server. 

The message sent by the client:

```JSON
{
	"type": 4
}
```
As you can see, the message sent by the client does not include any other data than the message type.

When the server receives this query, it will create a response with the same message type but this time, containing the list of channels as a JSON string array:

```JSON
{
	"type" : 4,
	"channels" : [
		"odysseu (0)",
		"main (1)"
	]
}
```
The numbers with each channel indicate the number of users currently on that channel.

## Bot channel

Server may also host a *bot channel*. Bot channel loads messages from a text file. A channel gets it's name from the file name (minus extension). 

If a user joins the channel, the bot channel starts to send chat messages from the text file to that channel, from the loaded text file. When the last user leaves the bot channel, no messages are sent, until a user again joins the channel.

Bot channels are never closed, similarily to "main" channel. Thus, when users list available channels on the server, the bot channel (if configured) is also listed as an available channel.

To set up a bot channel, specify the file name (possibly with full path) in the `chatserver.properties` settings file:

```
botchannel=odysseu
```
The file `odysseu.txt` (note the compulsory file extension that is *not* mentioned in the config file!) contains an example file. Some rules for the file structure:

* The bot channel name comes from the file name, so this file would create a channel named `odysseu`.
* Lines beginning with `#` are comment lines and are not handled.
* Lines beginning with `$` change the topic of the channel.
* Empty lines are skipped.
* Other lines are assumed to be chat message lines, where first `:` character found splits the line in two: text *before* `:` is the *nick* of the sender and text after `:` is the chat message to send to the client sessions in the channel.

If this setting does not exist or the file name is empty or invalid, no bot channel is created.

Currently server supports only one bot channel at a time.

## Building

The following are needed to build and run the server:

* JDK 18 or later.
* Maven for building and packaging the executable.
* JSON library (see details from pom.xml, Maven will download and install this when building).
* An IDE if you wish to view and/or edit the code (e.g. Visual Studio Code with Java extensions, or Eclipse).

Build the server (in the root directory of the project, where the `pom.xml` is located):

```console
mvn package
```

## Running 

First review the settings in `chatserver.properties` if necessary. Settings file has the following items:

```
port=10000
botchannel=odysseu
```

* `port` is the TCP port number the server is listening to for client connections. Make sure the port is not used by other apps nor services in your system.
* `botchannel` indicates the bot channel name as well as the file name containing the messages server sends to the bot channel if any users are on that channel.

Then run (assuming the server has been built) the server:

```console
java -jar target/ChatServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar chatserver.properties
```

Where the first parameter is the configuration file you wish to use to run the server.

Your operating system may ask for your permit so that the server can accept incoming connections. Obviously you should allow this.

The `main` method launching the server can be found in class `ChatSocketServer`.

> Be aware of the security implications on open Wi-Fi connections. Close the server when you no longer test your client. Note also that your local network may have configurations or firewalls preventing traffic on certain ports or protocols. In this case the server and client cannot connect. If having issues like this, check out your router settings.

You can stop the server in a controlled way from the console with `/quit` command. An error message with shutdown required flag is sent to all connected clients before the server shuts down.

You can use the command `/status` on the server console to print out the current channels and sessions on the server.

## More information

* (c) Antti Juustila 2020-2023, All rights reserved.
* MIT License (see LICENSE)
* INTERACT Research Unit, University of Oulu, Finland
