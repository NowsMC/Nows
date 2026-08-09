package space.nows.mcnows.integration.geb.event;

import foo.zaaarf.geb.api.IEvent;
import space.nows.mcnows.api.NowsContext;

/** Fired after GEB dispatchers and declared listeners are ready. */
public record NowsBootstrapReadyEvent(NowsContext context) implements IEvent {
}
