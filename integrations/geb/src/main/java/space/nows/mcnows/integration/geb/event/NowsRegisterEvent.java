package space.nows.mcnows.integration.geb.event;

import foo.zaaarf.geb.api.IEvent;
import space.nows.mcnows.api.NowsContext;

/** Fired after services/listeners are installed and before normal entrypoints run. */
public record NowsRegisterEvent(NowsContext context) implements IEvent {
}
