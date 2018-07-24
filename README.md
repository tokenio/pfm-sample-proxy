## Sample: Merchant Checkout With Merchant Proxy

This sample code shows how to realize the
[Token Request Flow](https://developer.token.io/token-request) with
the [Token Merchant Proxy](https://github.com/tokenio/merchant-proxy). The Token Request
flow enables merchants to request account information from a user.
The Token Merchant Proxy is a wrapper server around
the [Java SDK](https://github.com/tokenio/sdk-java) using a simple HTTP API.

### Dependency
The sample requires the proxy as a dependency. Make sure it is configured properly. Refer
to this [page](https://github.com/tokenio/merchant-proxy) for more details about its usages.

To start the proxy:

`cd merchant-proxy`

`./gradlew build run`

### Usage
To build this sample, you need Java Development Kit (JDK) version 8 or later.

To build:
 
 `./gradlew shadowJar`.

To run:
 
 `java -jar app/build/libs/app-*.jar`

This starts up a server.

The server shows a web page at `localhost:3000`. The page has a checkout button.
Click the button to start the token request experience. Some banks require you to download the
Token App and link your accounts on the app first.

The server will:
1. Create a token request.
2. Redirect the user to Token for authentication.
3. Wait for a callback that contains a token id.
4. Fetch account, balance, transaction information using the token id.
