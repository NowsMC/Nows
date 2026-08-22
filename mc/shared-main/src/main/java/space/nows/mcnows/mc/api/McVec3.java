package space.nows.mcnows.mc.api;

/** Stable three-dimensional vector used by Nows APIs instead of version-specific Minecraft vector classes. */
public record McVec3(double x, double y, double z) {
    public static McVec3 of(double x, double y, double z) {
        return new McVec3(x, y, z);
    }
}
