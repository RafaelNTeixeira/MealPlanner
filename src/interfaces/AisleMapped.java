package interfaces;

import ingredient.AisleSection;

/**
 * Implemented by any entity that belongs to a specific supermarket aisle.
 * Enables the GroceryEngine to group and sort the final shopping list
 * by store section, minimising back-tracking in the aisles.
 */
public interface AisleMapped {

    /**
     * @return the {@link AisleSection} where this item is found in the supermarket
     */
    AisleSection getSupermarketSection();
}