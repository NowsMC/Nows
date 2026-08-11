package space.nows.mcnows.mc.api.recipe;

@FunctionalInterface
public interface RecipeViewerLayoutFactory<T> {
    RecipeViewerLayout build(T recipe, RecipeViewerLayout.Builder layout);
}
