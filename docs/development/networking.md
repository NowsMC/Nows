# Network Channels

`integrations/network` exposes a small networking surface over Minecraft's existing Netty stack without importing version-specific Minecraft packet classes.

Declare channels in metadata, then use `NowsNetworking` from the runtime context:

```java
NowsNetworking networking = NowsNetworking.service(context);
networking.registerHandler("my_mod:main", NetworkDirection.CLIENTBOUND, (packet, payload) -> {
    int bytes = payload.size();
    ByteBuf buffer = payload.buffer();
});
```

Payloads are backed by Netty `ByteBuf`. Nows expects Minecraft launcher/runtime libraries to provide Netty and does not bundle a second copy.

Sending goes through `NetworkTransport`, which is installed by version-specific code.

## Known limitation: sending does not work yet

No `mc/<version>` adapter installs a `NetworkTransport`, so on every supported Minecraft version `canSend(...)` and `send(...)` return `false` and nothing reaches the other side. The first dropped message per channel and direction is logged as a warning. Check the result of `send`.

Declaring channels, registering handlers and `receive(...)` work and are tested. Delivery over a real connection is not implemented and has not been run on any version.
