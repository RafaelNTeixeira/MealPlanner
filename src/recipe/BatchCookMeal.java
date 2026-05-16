package recipe;

import ingredient.Ingredient;

import java.util.List;

/**
 * A meal designed for batch cooking - prepared in large quantities
 * and portioned out across the week (e.g. stews, curries, rice dishes).
 *
 * Adds a {@code prepTimeMinutes} field, since batch meals typically
 * require more upfront effort. This is purely informational for now
 * but can feed into a weekly planning view later.
 */
public class BatchCookMeal extends Recipe {

    /** Estimated preparation + cooking time in minutes for the base recipe. */
    private final int prepTimeMinutes;

    /**
     * @param name             display name of the recipe
     * @param ingredients      ingredients expressed for 1 serving
     * @param prepTimeMinutes  estimated prep/cook time in minutes
     */
    public BatchCookMeal(String name, List<Ingredient> ingredients, int prepTimeMinutes) {
        super(name, ingredients);
        if (prepTimeMinutes <= 0) {
            throw new IllegalArgumentException("Prep time must be positive. Got: " + prepTimeMinutes);
        }
        this.prepTimeMinutes = prepTimeMinutes;
    }

    @Override
    public String getRecipeType() {
        return "Batch Cook Meal";
    }

    public int getPrepTimeMinutes() { return prepTimeMinutes; }

    @Override
    public String toString() {
        return super.toString().stripTrailing() + String.format("%n    Prep time: %d min%n", prepTimeMinutes);
    }
}