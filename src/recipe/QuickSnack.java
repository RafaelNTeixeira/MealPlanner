package recipe;

import ingredient.Ingredient;

import java.util.List;

/**
 * A light snack that requires minimal preparation (e.g. overnight oats,
 * yogurt with fruit, rice cakes with peanut butter).
 *
 * Tracks whether the snack requires any cooking at all via
 * {@code requiresCooking}. No-cook snacks can be flagged as ready-to-eat
 * in future planning views.
 */
public class QuickSnack extends Recipe {

    /**
     * {@code true} if the snack needs any heat or cooking step;
     * {@code false} for completely raw or pre-made snacks.
     */
    private final boolean requiresCooking;

    /**
     * @param name            display name of the recipe
     * @param ingredients     ingredients expressed for 1 serving
     * @param requiresCooking whether any cooking step is needed
     */
    public QuickSnack(String name, List<Ingredient> ingredients, boolean requiresCooking) {
        super(name, ingredients);
        this.requiresCooking = requiresCooking;
    }

    @Override
    public String getRecipeType() {
        return "Quick Snack";
    }

    public boolean isRequiresCooking() { return requiresCooking; }

    @Override
    public String toString() {
        return super.toString().stripTrailing() + String.format("%n    Requires cooking: %s%n", requiresCooking ? "Yes" : "No");
    }
}