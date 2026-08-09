package space.nows.mcnows.gradle;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

import javax.inject.Inject;

/** Public, intentionally small configuration surface for Nows development. */
public abstract class NowsExtension {
    private final Property<String> minecraftVersion;
    private final Property<String> nowsVersion;
    private final Property<Boolean> officialMappings;
    private final Property<Boolean> addGeb;
    private final Property<Boolean> addMixin;
    private final RegularFileProperty developmentClientJar;

    @Inject
    public NowsExtension(ObjectFactory objects) {
        minecraftVersion = objects.property(String.class).convention("26.2");
        nowsVersion = objects.property(String.class).convention(NowsGradlePluginDefaults.nowsVersion());
        officialMappings = objects.property(Boolean.class).convention(true);
        addGeb = objects.property(Boolean.class).convention(true);
        addMixin = objects.property(Boolean.class).convention(true);
        developmentClientJar = objects.fileProperty();
    }

    public Property<String> getMinecraftVersion() { return minecraftVersion; }
    public Property<String> getNowsVersion() { return nowsVersion; }
    public Property<Boolean> getOfficialMappings() { return officialMappings; }
    public Property<Boolean> getAddGeb() { return addGeb; }
    public Property<Boolean> getAddMixin() { return addMixin; }
    public RegularFileProperty getDevelopmentClientJar() { return developmentClientJar; }
}
