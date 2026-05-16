package ingredient;

/**
 * Dry and shelf-stable goods: pasta, rice, canned items, oats, flour, etc.
 * Mapped to the {@link AisleSection#DRY_GOODS} aisle.
 */
public class DryGoods extends Ingredient {

    public DryGoods(String name,
                    double quantity,
                    String unit,
                    double caloriesPerUnit,
                    double proteinPerUnit) {
        super(name, quantity, unit, caloriesPerUnit, proteinPerUnit);
    }

    @Override
    public AisleSection getSupermarketSection() {
        return AisleSection.DRY_GOODS;
    }
}