package space.nows.mcnows.integration.geb.event;

import foo.zaaarf.geb.api.IEvent;
import space.nows.mcnows.api.NowsContext;
import space.nows.mcnows.core.mod.ModContainer;

/** Fired immediately after one mod entrypoint has completed. */
public record NowsModEntrypointCompletedEvent(
        NowsContext context,
        ModContainer mod,
        String className
) implements IEvent {
}
