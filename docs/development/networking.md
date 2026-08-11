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

Sending goes through `NetworkTransport`, which is installed by version-specific code. Until a concrete transport is present, `send(...)` returns `false`.
