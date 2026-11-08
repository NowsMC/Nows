# Consumer fixture

A separate Gradle build that uses Nows the way an outside mod does: through `NowsGradlePlugin` and the produced
artifacts, with one source for every `mc/<version>` adapter. It calls the documented API from its entrypoint and checks
an observable result for each call inside the running game, then writes one `NOWS_FIXTURE api=... PASS|FAIL|...` line
per API and a `NOWS_FIXTURE_SUMMARY` line to `nows-fixture-results.txt` in the game directory.

`settings.gradle.kts` resolves the Nows artifacts from this working tree with a composite build and dependency
substitution, so nothing is published and no GPG key is needed.

```bash
./gradlew -p fixtures/consumer jar -Pminecraft_version=1.20.1
```

`scripts/verify.ps1` builds the fixture for every adapter, starts the real client with it and reads the results.

Some areas cannot be observed from the main menu and are reported as `NOT_EXERCISED` with the reason, never skipped
silently: sending over the network (no adapter installs a transport), the player, server and level ticks, block entity
and menu registration, and on Minecraft 26.x native item stacks, which that version builds only after a world is joined.
`-Dnows.fixture.skip=<api,api>` marks calls as `SKIPPED` for diagnosis, and a skipped call still fails the gate.
