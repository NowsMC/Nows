package space.nows.mcnows.integration.geb.event;

import foo.zaaarf.geb.api.IEvent;
import space.nows.mcnows.api.NowsContext;

/** Fired after all Nows mod entrypoints have run. */
public record NowsEntrypointsCompletedEvent(NowsContext context, int count) implements IEvent {
}
