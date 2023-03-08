# O4-server

This is a simple Java chat server implemented for Programming 4 course. The course teachers client side GUI programming. Students can use this server to develop their own GUI clients.

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

Java Message classes in `oy.tol.chat` package implement these message types. 

Converting Message objects to JSON text is implemented in `Message.toJSON()` methods. Parsing message objects from JSONObjects is done in `MessageFactory.fromJSON(JSONObject)`.

Message contents in JSON are specified below.

### Error message

Error messages are sent by the server only.

If the value of `clientshutdown` is different from zero (0), the error requires the clients to shut down. This implies the server is either shutting down or has had a critical error and cannot continue. 

```JSON
{
	"type": -1,
	"error" : "some error message here",
	"clientshutdown": 0
}
```

### Status message

Status messages are sent by the server only. They indicate some event on the server side clients or users may be interested in.

```JSON
{
	"type": 0,
	"status" : "some status message here"
}
```

### Chat message

A client sends chat messages to the server, and server relays chat messages to other clients in the same *channel*.

Here is a simple chat message, from user "telemakos". 

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
A chat message can also be a **reply** to another chat message. In this case, the JSON contains an optional element `inReplyTo`: 

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

### Join channel message

User can move between channels in the server using join channel messages. If a channel with the specified name does not exist, server creates that channel.

When the last user leaves the channel, it is closed. The channel "main" is an exception. It is always created when the server starts, and is never closed. Clients connecting to the server are automatically joining the main channel.

```JSON
{
	"type": 2,
	"channel" : "ohjelmointi-4",
}
```

Users should keep the channel names short since displaying long channel names is not convenient. Currently the server does not limit channel or nick (user) name lengths.

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

Server may also host a *bot channel*. Bot channel loads messages from a text file. A channel gets it's name from the file name. 

If a user joins the channel, the bot channel starts to send chat messages to that channel. When the last user leaves the bot channel, no messages are sent.

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

Build the server (in the root source directory):

```console
mvn package
```

## Running 

First edit the settings in `chatserver.properties` if necessary.

Then run (assuming the server has been built) the server:

```console
java -jar target/ChatServer-0.0.1-SNAPSHOT-jar-with-dependencies.jar chatserver.properties
```

Where the first parameter is the configuration file you wish to use to run the server.

You can stop the server in a controlled way from the console with `/quit` command. An error message with shutdown required flag is sent to all connected clients before the server shuts down.

You can use the command `/status` on the server console to print out the current channels and sessions on the server.

## More information

* (c) Antti Juustila 2020-2023, All rights reserved.
* MIT License (see LICENSE)
* INTERACT Research Unit, University of Oulu, Finland
