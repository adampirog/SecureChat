# Secure chat

Code contains the Java implementation for client and server side of secure chat application.
Application supports end to end encryption of messages with **xor** and **caesar** ciphers.
Key exchange is performed using the Diffie–Hellman algorithm.

## System requirements
* Java 8
* Jackson library
* Maven (not obligatory)

All the dependency details are listed in Mavens file pom.xml  
## How to build
### Maven
The easiest way is to build the project with Maven support. In codes root folder(containing pom.xml) execute command:
```
mvn package
```
Then you can start Server / Client with commands:
```
java -jar target/Server-jar-with-dependencies.jar
java -jar target/Client-jar-with-dependencies.jar
```
### Manual compilation

You can also build the project manually with javac compiler, but it does require external [Jakcosn](https://jar-download.com/artifacts/com.fasterxml.jackson.core) jar. 


## Users manual
Client application suppports following commands
* !logout - logs out and closes the client app
* !to *\<username>*  - sets the recipient to *\<username>*
* !encryption *\<method>* - sets the encryption method to *\<method>*

Supported encryption methods:
* Ceazar cipher - command *caesar*
* XOR cipher - command *xor*
* Unencrypted message - command *none*

### Simple uscase scenario

**Send a message, choose a specific encryption method**
```
!to Bob
!encryption caesar
Hello Bob.
```

This encryption method will remain default till the next change.

**Send a simple message**
```
!to Bob
Hello Bob.
```
When the encryption method is not explicitly specified, default encryption is performed (last one that was specified).



