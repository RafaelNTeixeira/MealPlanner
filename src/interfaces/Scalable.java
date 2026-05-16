package interfaces;

/**
 * Implemented by anything whose portions can be adjusted.
 * In this project, Recipes are the primary Scalable entities.
 */
public interface Scalable {

    /**
     * Adjusts the entity to the given number of servings.
     * Implementations are expected to mutate their internal state
     * (e.g. scale ingredient quantities) proportionally.
     *
     * @param servings the target number of servings (must be > 0)
     */
    void adjustPortions(int servings);
}