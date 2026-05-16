package ingredient;

/**
 * Meat, poultry, fish, and seafood.
 * Mapped to the {@link AisleSection#MEAT} aisle.
 */
public class Meat extends Ingredient {

    public Meat(String name,
                double quantity,
                String unit,
                double caloriesPerUnit,
                double proteinPerUnit) {
        super(name, quantity, unit, caloriesPerUnit, proteinPerUnit);
    }

    @Override
    public AisleSection getSupermarketSection() {
        return AisleSection.MEAT;
    }
}