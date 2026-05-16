package ingredient;

import interfaces.AisleMapped;
import interfaces.Nutritional;

/**
 * Abstract base for every ingredient in the system.
 *
 * Nutritional values (calories, protein) are stored per-unit so that
 * they scale correctly when {@code quantity} is changed by the engine
 * (e.g. when a Recipe adjusts its portions and multiplies ingredient amounts).
 *
 * Each concrete subclass fixes its {@link AisleSection} by implementing
 * {@code getSupermarketSection()}, which lets the GroceryEngine sort the
 * final shopping list without any conditional logic.
 */
public abstract class Ingredient implements Nutritional, AisleMapped {

    // ---------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------

    /** Display name, e.g. "Chicken Breast", "Brown Rice" */
    protected String name;

    /**
     * How much of this ingredient is needed.
     * The unit is given by {@link #unit} (e.g. 200 grams, 3 pieces).
     */
    protected double quantity;

    /** Unit of measurement: "g", "ml", "pieces", "tbsp", etc. */
    protected String unit;

    /** Kilocalories per single unit (e.g. per gram, per piece). */
    protected double caloriesPerUnit;

    /** Grams of protein per single unit. */
    protected double proteinPerUnit;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    /**
     * @param name            display name of the ingredient
     * @param quantity        amount required
     * @param unit            unit of measurement
     * @param caloriesPerUnit kcal per one unit
     * @param proteinPerUnit  grams of protein per one unit
     */
    protected Ingredient(String name,
                         double quantity,
                         String unit,
                         double caloriesPerUnit,
                         double proteinPerUnit) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Ingredient name must not be blank.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive. Got: " + quantity);
        }
        this.name             = name;
        this.quantity         = quantity;
        this.unit             = unit;
        this.caloriesPerUnit  = caloriesPerUnit;
        this.proteinPerUnit   = proteinPerUnit;
    }

    // ---------------------------------------------------------------
    // Nutritional implementation
    // ---------------------------------------------------------------

    /** Total kcal for the current quantity of this ingredient. */
    @Override
    public double getCalories() {
        return caloriesPerUnit * quantity;
    }

    /** Total protein (g) for the current quantity of this ingredient. */
    @Override
    public double getProtein() {
        return proteinPerUnit * quantity;
    }

    // ---------------------------------------------------------------
    // Quantity helpers
    // ---------------------------------------------------------------

    public double getQuantity() { return quantity; }

    /**
     * Directly sets the quantity. Called by a Recipe when it scales
     * its ingredient list to a new number of servings.
     */
    public void setQuantity(double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive. Got: " + quantity);
        }
        this.quantity = quantity;
    }

    // ---------------------------------------------------------------
    // Basic getters
    // ---------------------------------------------------------------

    public String getName()            { return name; }
    public String getUnit()            { return unit; }
    public double getCaloriesPerUnit() { return caloriesPerUnit; }
    public double getProteinPerUnit()  { return proteinPerUnit; }

    // ---------------------------------------------------------------
    // Object overrides
    // ---------------------------------------------------------------

    @Override
    public String toString() {
        return String.format("%-25s %6.1f %-6s [%s] | %.0f kcal | %.1f g protein",
                name, quantity, unit,
                getSupermarketSection(),
                getCalories(), getProtein());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Ingredient)) return false;
        Ingredient other = (Ingredient) o;
        // Two ingredients are "the same item" if they share name and unit.
        // Quantities are merged, not compared, by the GroceryEngine.
        return name.equalsIgnoreCase(other.name) && unit.equalsIgnoreCase(other.unit);
    }

    @Override
    public int hashCode() {
        return (name.toLowerCase() + "|" + unit.toLowerCase()).hashCode();
    }
}