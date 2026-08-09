package space.nows.mcnows.integration.geb.event;

import foo.zaaarf.geb.api.IEvent;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.core.mod.ModContainer;

/** Fired immediately before one mod entrypoint instance is created and invoked. */
public record NowsModEntrypointStartingEvent(
        NowsContext context,
        ModContainer mod,
        String className
) implements IEvent {
}
