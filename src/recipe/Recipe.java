package recipe;

import ingredient.Ingredient;
import interfaces.Nutritional;
import interfaces.Scalable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base for all recipes in the system.
 *
 * A Recipe owns a list of Ingredients and knows how to scale them.
 * Nutritional totals are always derived live from the current (scaled)
 * ingredient quantities, so they stay consistent after any adjustPortions call.
 *
 * Design notes:
 * - Ingredients are supplied at BASE (1-serving) quantities in the constructor.
 * - A baseQuantities map is built at construction time so that every subsequent
 *   adjustPortions call scales from the original values, not from the
 *   already-scaled ones - avoiding compounding errors on repeated calls.
 * - currentServings tracks the active scale so callers can inspect it.
 */
public abstract class Recipe implements Scalable, Nutritional {

    // ---------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------

    /** Human-readable name of the recipe. */
    protected final String name;

    /**
     * Ingredient list at their CURRENT (scaled) quantities.
     * This is what the GroceryEngine reads when building the shopping list.
     */
    protected final List<Ingredient> ingredients;

    /**
     * Baseline quantities (1 serving) keyed by "name|unit".
     * Never mutated after construction - used as the source of truth
     * for every adjustPortions call.
     */
    private final Map<String, Double> baseQuantities;

    /** How many servings the ingredients are currently scaled to. */
    private int currentServings;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    /**
     * @param name        display name of the recipe
     * @param ingredients ingredient list expressed for exactly 1 serving
     */
    protected Recipe(String name, List<Ingredient> ingredients) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Recipe name must not be blank.");
        }
        if (ingredients == null || ingredients.isEmpty()) {
            throw new IllegalArgumentException("A recipe must have at least one ingredient.");
        }

        this.name            = name;
        this.ingredients     = new ArrayList<>(ingredients);
        this.currentServings = 1;

        // Snapshot baseline quantities so adjustPortions can always scale
        // from the original 1-serving values.
        this.baseQuantities = new LinkedHashMap<>();
        for (Ingredient ingredient : ingredients) {
            baseQuantities.put(key(ingredient), ingredient.getQuantity());
        }
    }

    // ---------------------------------------------------------------
    // Scalable implementation
    // ---------------------------------------------------------------

    /**
     * Scales every ingredient's quantity proportionally to the requested
     * number of servings, always relative to the 1-serving baseline.
     *
     * Calling adjustPortions(4) twice in a row still results in 4-serving
     * quantities - not 16-serving.
     *
     * @param servings target number of servings (must be > 0)
     */
    @Override
    public void adjustPortions(int servings) {
        if (servings <= 0) {
            throw new IllegalArgumentException("Servings must be positive. Got: " + servings);
        }
        for (Ingredient ingredient : ingredients) {
            double base = baseQuantities.get(key(ingredient));
            ingredient.setQuantity(base * servings);
        }
        this.currentServings = servings;
    }

    // ---------------------------------------------------------------
    // Nutritional implementation
    // ---------------------------------------------------------------

    /** Total kcal across all ingredients at their current (scaled) quantities. */
    @Override
    public double getCalories() {
        return ingredients.stream()
                .mapToDouble(Ingredient::getCalories)
                .sum();
    }

    /** Total protein (g) across all ingredients at their current (scaled) quantities. */
    @Override
    public double getProtein() {
        return ingredients.stream()
                .mapToDouble(Ingredient::getProtein)
                .sum();
    }

    // ---------------------------------------------------------------
    // Abstract hook - subclasses declare their recipe type label
    // ---------------------------------------------------------------

    /**
     * A short label describing the kind of recipe (e.g. "Batch Cook Meal").
     * Used in toString() and any future UI rendering.
     */
    public abstract String getRecipeType();

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------

    public String getName()              { return name; }
    public int getCurrentServings()      { return currentServings; }
    public List<Ingredient> getIngredients() { return ingredients; }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** Composite key used to match ingredients in baseQuantities. */
    private static String key(Ingredient ingredient) {
        return ingredient.getName().toLowerCase() + "|" + ingredient.getUnit().toLowerCase();
    }

    // ---------------------------------------------------------------
    // Object overrides
    // ---------------------------------------------------------------

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("[ %s ] \"%s\" - %d serving(s) | %.0f kcal | %.1f g protein%n",
                getRecipeType(), name, currentServings, getCalories(), getProtein()));
        for (Ingredient ingredient : ingredients) {
            sb.append("    ").append(ingredient).append(System.lineSeparator());
        }
        return sb.toString();
    }
}