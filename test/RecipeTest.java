import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Tests for the Recipe abstract class (exercised via BatchCookMeal)
 * and the unique behaviour of QuickSnack and ProteinShake.
 */
class RecipeTest {

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private BatchCookMeal sampleMeal() {
        return new BatchCookMeal("Test Meal",
                Arrays.asList(
                        new Meat("Chicken", 100, "g", 2.0,  0.30),
                        new DryGoods("Rice",  80, "g", 3.5, 0.07)
                ), 30);
    }

    // ---------------------------------------------------------------
    // Recipe constructor validation
    // ---------------------------------------------------------------

    @Test
    void constructor_blankName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BatchCookMeal("  ",
                        Arrays.asList(new Produce("X", 1, "g", 1, 1)), 30));
    }

    @Test
    void constructor_nullName_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BatchCookMeal(null,
                        Arrays.asList(new Produce("X", 1, "g", 1, 1)), 30));
    }

    @Test
    void constructor_nullIngredients_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BatchCookMeal("Meal", null, 30));
    }

    @Test
    void constructor_emptyIngredients_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BatchCookMeal("Meal", Collections.emptyList(), 30));
    }

    // ---------------------------------------------------------------
    // Recipe getters
    // ---------------------------------------------------------------

    @Test
    void getName_returnsCorrectName() {
        assertEquals("Test Meal", sampleMeal().getName());
    }

    @Test
    void getCurrentServings_startsAtOne() {
        assertEquals(1, sampleMeal().getCurrentServings());
    }

    @Test
    void getIngredients_returnsPopulatedList() {
        assertEquals(2, sampleMeal().getIngredients().size());
    }

    // ---------------------------------------------------------------
    // Nutritional aggregation
    // ---------------------------------------------------------------

    @Test
    void getCalories_sumsAcrossAllIngredients() {
        // Chicken: 100g * 2.0 = 200, Rice: 80g * 3.5 = 280  ->  total 480
        assertEquals(480.0, sampleMeal().getCalories(), 0.001);
    }

    @Test
    void getProtein_sumsAcrossAllIngredients() {
        // Chicken: 100 * 0.30 = 30, Rice: 80 * 0.07 = 5.6  ->  total 35.6
        assertEquals(35.6, sampleMeal().getProtein(), 0.001);
    }

    // ---------------------------------------------------------------
    // adjustPortions
    // ---------------------------------------------------------------

    @Test
    void adjustPortions_scalesIngredientQuantities() {
        BatchCookMeal meal = sampleMeal();
        meal.adjustPortions(4);
        assertEquals(400.0, meal.getIngredients().get(0).getQuantity(), 0.001); // 100 * 4
        assertEquals(320.0, meal.getIngredients().get(1).getQuantity(), 0.001); //  80 * 4
    }

    @Test
    void adjustPortions_updatesCurrentServings() {
        BatchCookMeal meal = sampleMeal();
        meal.adjustPortions(3);
        assertEquals(3, meal.getCurrentServings());
    }

    @Test
    void adjustPortions_calledTwice_doesNotCompound() {
        BatchCookMeal meal = sampleMeal();
        meal.adjustPortions(4);
        meal.adjustPortions(4); // must still be 4× base, not 16×
        assertEquals(400.0, meal.getIngredients().get(0).getQuantity(), 0.001);
    }

    @Test
    void adjustPortions_scaledNutritionIsCorrect() {
        BatchCookMeal meal = sampleMeal();
        meal.adjustPortions(3);
        // Chicken 300g * 2.0 = 600, Rice 240g * 3.5 = 840 -> 1440
        assertEquals(1440.0, meal.getCalories(), 0.001);
    }

    @Test
    void adjustPortions_zero_throws() {
        assertThrows(IllegalArgumentException.class, () -> sampleMeal().adjustPortions(0));
    }

    @Test
    void adjustPortions_negative_throws() {
        assertThrows(IllegalArgumentException.class, () -> sampleMeal().adjustPortions(-1));
    }

    // ---------------------------------------------------------------
    // Recipe toString
    // ---------------------------------------------------------------

    @Test
    void toString_containsRecipeTypeAndName() {
        String s = sampleMeal().toString();
        assertTrue(s.contains("Batch Cook Meal"));
        assertTrue(s.contains("Test Meal"));
    }

    // ---------------------------------------------------------------
    // BatchCookMeal specifics
    // ---------------------------------------------------------------

    @Test
    void batchCookMeal_getRecipeType() {
        assertEquals("Batch Cook Meal", sampleMeal().getRecipeType());
    }

    @Test
    void batchCookMeal_getPrepTimeMinutes() {
        assertEquals(30, sampleMeal().getPrepTimeMinutes());
    }

    @Test
    void batchCookMeal_zeroPrepTime_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BatchCookMeal("Meal",
                        Arrays.asList(new Produce("X", 1, "g", 1, 1)), 0));
    }

    @Test
    void batchCookMeal_negativePrepTime_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new BatchCookMeal("Meal",
                        Arrays.asList(new Produce("X", 1, "g", 1, 1)), -10));
    }

    @Test
    void batchCookMeal_toString_containsPrepTime() {
        assertTrue(sampleMeal().toString().contains("30"));
    }

    // ---------------------------------------------------------------
    // QuickSnack specifics
    // ---------------------------------------------------------------

    @Test
    void quickSnack_getRecipeType() {
        QuickSnack snack = new QuickSnack("Snack",
                Arrays.asList(new Dairy("Yogurt", 150, "g", 0.59, 0.1)), false);
        assertEquals("Quick Snack", snack.getRecipeType());
    }

    @Test
    void quickSnack_requiresCooking_true_reflectedInToString() {
        QuickSnack snack = new QuickSnack("Snack",
                Arrays.asList(new DryGoods("Oats", 60, "g", 3.89, 0.13)), true);
        assertTrue(snack.isRequiresCooking());
        assertTrue(snack.toString().contains("Yes"));
    }

    @Test
    void quickSnack_requiresCooking_false_reflectedInToString() {
        QuickSnack snack = new QuickSnack("Snack",
                Arrays.asList(new Dairy("Yogurt", 150, "g", 0.59, 0.1)), false);
        assertFalse(snack.isRequiresCooking());
        assertTrue(snack.toString().contains("No"));
    }

    // ---------------------------------------------------------------
    // ProteinShake specifics
    // ---------------------------------------------------------------

    @Test
    void proteinShake_getRecipeType() {
        ProteinShake shake = new ProteinShake("Shake",
                Arrays.asList(new DryGoods("Whey", 30, "g", 3.73, 0.75)), "Chocolate");
        assertEquals("Protein Shake", shake.getRecipeType());
    }

    @Test
    void proteinShake_getFlavorProfile() {
        ProteinShake shake = new ProteinShake("Shake",
                Arrays.asList(new DryGoods("Whey", 30, "g", 3.73, 0.75)), "Vanilla");
        assertEquals("Vanilla", shake.getFlavorProfile());
    }

    @Test
    void proteinShake_blankFlavor_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProteinShake("Shake",
                        Arrays.asList(new DryGoods("Whey", 30, "g", 3.73, 0.75)), "  "));
    }

    @Test
    void proteinShake_nullFlavor_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new ProteinShake("Shake",
                        Arrays.asList(new DryGoods("Whey", 30, "g", 3.73, 0.75)), null));
    }

    @Test
    void proteinShake_toString_containsFlavor() {
        ProteinShake shake = new ProteinShake("Shake",
                Arrays.asList(new DryGoods("Whey", 30, "g", 3.73, 0.75)), "Chocolate");
        assertTrue(shake.toString().contains("Chocolate"));
    }
}