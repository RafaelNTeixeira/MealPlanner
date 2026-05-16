package ingredient;

/**
 * Dairy and refrigerated items: milk, yogurt, cheese, eggs, butter, etc.
 * Mapped to the {@link AisleSection#DAIRY} aisle.
 */
public class Dairy extends Ingredient {

    public Dairy(String name,
                 double quantity,
                 String unit,
                 double caloriesPerUnit,
                 double proteinPerUnit) {
        super(name, quantity, unit, caloriesPerUnit, proteinPerUnit);
    }

    @Override
    public AisleSection getSupermarketSection() {
        return AisleSection.DAIRY;
    }
}