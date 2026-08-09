package space.nows.mcnows.integration.geb.event;

import foo.zaaarf.geb.api.IEvent;
import space.nows.mcnows.api.NowsContext;

/** Fired before Nows starts running mod entrypoints. */
public record NowsEntrypointsStartingEvent(NowsContext context) implements IEvent {
}
