package recipe;

import ingredient.Ingredient;

import java.util.List;

/**
 * A blended or mixed protein shake (e.g. whey + banana + oats + milk).
 *
 * Tracks a {@code flavorProfile} (e.g. "Chocolate", "Vanilla Berry") which
 * can be used to differentiate shakes in the weekly plan and eventually
 * drive flavour-based grouping in the pantry tracker.
 *
 * Shakes are inherently quick to prepare, so no prep-time field is needed —
 * that concern lives in BatchCookMeal.
 */
public class ProteinShake extends Recipe {

    /** Descriptive flavour label for the shake, e.g. "Chocolate Peanut Butter". */
    private final String flavorProfile;

    /**
     * @param name          display name of the recipe
     * @param ingredients   ingredients expressed for 1 serving
     * @param flavorProfile short flavour descriptor
     */
    public ProteinShake(String name, List<Ingredient> ingredients, String flavorProfile) {
        super(name, ingredients);
        if (flavorProfile == null || flavorProfile.isBlank()) {
            throw new IllegalArgumentException("Flavor profile must not be blank.");
        }
        this.flavorProfile = flavorProfile;
    }

    @Override
    public String getRecipeType() {
        return "Protein Shake";
    }

    public String getFlavorProfile() { return flavorProfile; }

    @Override
    public String toString() {
        return super.toString().stripTrailing() + String.format("%n    Flavor: %s%n", flavorProfile);
    }
}