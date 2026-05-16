package interfaces;

/**
 * Implemented by any entity that exposes macro-nutritional data.
 * Both Ingredients and Recipes implement this, allowing the engine
 * to aggregate totals at any level of the hierarchy.
 */
public interface Nutritional {

    /**
     * @return total kilocalories for this item at its current quantity/servings
     */
    double getCalories();

    /**
     * @return total grams of protein for this item at its current quantity/servings
     */
    double getProtein();
}