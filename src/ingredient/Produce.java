package ingredient;

/**
 * Fruit, vegetables, and fresh herbs.
 * Mapped to the {@link AisleSection#PRODUCE} aisle.
 */
public class Produce extends Ingredient {

    public Produce(String name,
                   double quantity,
                   String unit,
                   double caloriesPerUnit,
                   double proteinPerUnit) {
        super(name, quantity, unit, caloriesPerUnit, proteinPerUnit);
    }

    @Override
    public AisleSection getSupermarketSection() {
        return AisleSection.PRODUCE;
    }
}