package ingredient;

/**
 * Represents the physical sections of a supermarket.
 * Used by AisleMapped implementors so the GroceryEngine can
 * group and sort the shopping list in store-traversal order.
 *
 * The ordinal of each constant reflects the recommended walking order
 * through a typical supermarket layout.
 */
public enum AisleSection {
    PRODUCE,        // fruit & vegetables - usually first after the entrance
    MEAT,           // butcher/protein counter
    DAIRY,          // refrigerated dairy aisle
    DRY_GOODS       // pasta, rice, canned goods, cereals, etc.
}