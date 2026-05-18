import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the Ingredient abstract class (exercised via Produce)
 * and the four concrete subclasses (aisle section per type).
 */
class IngredientTest {

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private Produce apple() {
        return new Produce("Apple", 100, "g", 0.52, 0.003);
    }

    // ---------------------------------------------------------------
    // Constructor validation
    // ---------------------------------------------------------------

    @Test
    void constructor_blankName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Produce("  ", 100, "g", 0.52, 0.003));
    }

    @Test
    void constructor_nullName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Produce(null, 100, "g", 0.52, 0.003));
    }

    @Test
    void constructor_zeroQuantity_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Produce("Apple", 0, "g", 0.52, 0.003));
    }

    @Test
    void constructor_negativeQuantity_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new Produce("Apple", -5, "g", 0.52, 0.003));
    }

    // ---------------------------------------------------------------
    // Getters
    // ---------------------------------------------------------------

    @Test
    void getters_returnFieldsSetByConstructor() {
        Produce p = new Produce("Apple", 100, "g", 0.52, 0.003);
        assertEquals("Apple", p.getName());
        assertEquals(100,    p.getQuantity(),        0.001);
        assertEquals("g",    p.getUnit());
        assertEquals(0.52,   p.getCaloriesPerUnit(),  0.001);
        assertEquals(0.003,  p.getProteinPerUnit(),   0.001);
    }

    // ---------------------------------------------------------------
    // Nutritional calculations
    // ---------------------------------------------------------------

    @Test
    void getCalories_returnsQuantityTimesCaloriesPerUnit() {
        Produce p = new Produce("Apple", 200, "g", 0.52, 0.003);
        assertEquals(104.0, p.getCalories(), 0.001);
    }

    @Test
    void getProtein_returnsQuantityTimesProteinPerUnit() {
        Produce p = new Produce("Apple", 200, "g", 0.52, 0.003);
        assertEquals(0.6, p.getProtein(), 0.001);
    }

    // ---------------------------------------------------------------
    // setQuantity
    // ---------------------------------------------------------------

    @Test
    void setQuantity_updatesQuantityAndRecalculatesNutrition() {
        Produce p = apple();
        p.setQuantity(250);
        assertEquals(250, p.getQuantity(), 0.001);
        assertEquals(250 * 0.52, p.getCalories(), 0.001);
    }

    @Test
    void setQuantity_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> apple().setQuantity(0));
    }

    @Test
    void setQuantity_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> apple().setQuantity(-10));
    }

    // ---------------------------------------------------------------
    // equals
    // ---------------------------------------------------------------

    @Test
    void equals_sameReference_returnsTrue() {
        Produce p = apple();
        assertEquals(p, p);
    }

    @Test
    void equals_null_returnsFalse() {
        assertNotEquals(null, apple());
    }

    @Test
    void equals_nonIngredient_returnsFalse() {
        assertNotEquals("Apple", apple());
    }

    @Test
    void equals_sameNameAndUnit_differentQuantity_returnsTrue() {
        Produce p1 = new Produce("Apple", 100, "g", 0.52, 0.003);
        Produce p2 = new Produce("Apple", 999, "g", 9.99, 9.99);
        assertEquals(p1, p2);
    }

    @Test
    void equals_differentName_returnsFalse() {
        Produce p1 = new Produce("Apple",  100, "g", 0.52, 0.003);
        Produce p2 = new Produce("Banana", 100, "g", 0.52, 0.003);
        assertNotEquals(p1, p2);
    }

    @Test
    void equals_differentUnit_returnsFalse() {
        Produce p1 = new Produce("Apple", 100, "g",  0.52, 0.003);
        Produce p2 = new Produce("Apple", 100, "ml", 0.52, 0.003);
        assertNotEquals(p1, p2);
    }

    @Test
    void equals_caseInsensitiveNameAndUnit() {
        Produce p1 = new Produce("apple", 100, "g", 0.52, 0.003);
        Produce p2 = new Produce("APPLE", 100, "G", 0.52, 0.003);
        assertEquals(p1, p2);
    }

    // ---------------------------------------------------------------
    // hashCode
    // ---------------------------------------------------------------

    @Test
    void hashCode_equalIngredients_haveSameHash() {
        Produce p1 = new Produce("Apple", 100, "g", 0.52, 0.003);
        Produce p2 = new Produce("Apple", 999, "g", 9.99, 9.99);
        assertEquals(p1.hashCode(), p2.hashCode());
    }

    // ---------------------------------------------------------------
    // toString
    // ---------------------------------------------------------------

    @Test
    void toString_containsNameAndAisleSection() {
        String s = apple().toString();
        assertTrue(s.contains("Apple"));
        assertTrue(s.contains("PRODUCE"));
    }

    // ---------------------------------------------------------------
    // AisleSection per concrete subclass
    // ---------------------------------------------------------------

    @Test
    void produce_sectionIsProduce() {
        assertEquals(AisleSection.PRODUCE,
                new Produce("X", 1, "g", 1, 1).getSupermarketSection());
    }

    @Test
    void meat_sectionIsMeat() {
        assertEquals(AisleSection.MEAT,
                new Meat("X", 1, "g", 1, 1).getSupermarketSection());
    }

    @Test
    void dairy_sectionIsDairy() {
        assertEquals(AisleSection.DAIRY,
                new Dairy("X", 1, "ml", 1, 1).getSupermarketSection());
    }

    @Test
    void dryGoods_sectionIsDryGoods() {
        assertEquals(AisleSection.DRY_GOODS,
                new DryGoods("X", 1, "g", 1, 1).getSupermarketSection());
    }
}