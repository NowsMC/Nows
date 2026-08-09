package space.nows.mcnows.integration.geb.event;

import foo.zaaarf.geb.api.IEvent;
import space.nows.mcnows.api.NowsContext;

/** Fired just before control is handed to Minecraft's main class. */
public record NowsMinecraftStartingEvent(
        NowsContext context,
        String mainClassName,
        int forwardedArgumentCount
) implements IEvent {
}
