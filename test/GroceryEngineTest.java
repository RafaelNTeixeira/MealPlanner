import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.*;

/**
 * Tests for GroceryEngine and its inner NutritionalSummary value object.
 *
 * Key branch coverage targets:
 *  - buildGroceryList: scale -> extract -> deduplicate -> sort
 *  - computeWeeklyNutrition: already-scaled branch (skip) vs. not-scaled branch (re-scale)
 *  - printGroceryList / printShoppingList: smoke-tested via stdout capture
 */
class GroceryEngineTest {

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    /** Plan with two recipes sharing NO ingredients - clean scaling/sorting test. */
    private Map<Recipe, Integer> simplePlan() {
        Map<Recipe, Integer> plan = new LinkedHashMap<>();
        plan.put(new BatchCookMeal("Chicken Rice",
                Arrays.asList(
                        new Meat("Chicken",  150, "g", 1.65,  0.31),
                        new DryGoods("Rice",  80, "g", 3.62, 0.075)
                ), 30), 4);
        plan.put(new QuickSnack("Yogurt Bowl",
                Arrays.asList(
                        new Dairy("Yogurt",   150, "g", 0.59,  0.10),
                        new Produce("Banana", 120, "g", 0.89, 0.011)
                ), false), 2);
        return plan;
    }

    /** Plan where both recipes contain Rice - deduplication test. */
    private Map<Recipe, Integer> planWithDuplicateIngredient() {
        Map<Recipe, Integer> plan = new LinkedHashMap<>();
        plan.put(new BatchCookMeal("Meal A",
                Arrays.asList(new DryGoods("Rice", 80, "g", 3.62, 0.075)), 30), 2);
        plan.put(new BatchCookMeal("Meal B",
                Arrays.asList(new DryGoods("Rice", 80, "g", 3.62, 0.075)), 20), 3);
        return plan;
    }

    // ---------------------------------------------------------------
    // Constructor validation
    // ---------------------------------------------------------------

    @Test
    void constructor_nullPlan_throws() {
        assertThrows(IllegalArgumentException.class, () -> new GroceryEngine(null));
    }

    @Test
    void constructor_emptyPlan_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new GroceryEngine(Collections.emptyMap()));
    }

    // ---------------------------------------------------------------
    // buildGroceryList - scaling
    // ---------------------------------------------------------------

    @Test
    void buildGroceryList_scalesIngredientsByServings() {
        Map<Recipe, Integer> plan = new LinkedHashMap<>();
        plan.put(new BatchCookMeal("Meal",
                Arrays.asList(new Meat("Chicken", 150, "g", 1.65, 0.31)), 30), 4);
        List<Ingredient> list = new GroceryEngine(plan).buildGroceryList();
        assertEquals(600.0, list.get(0).getQuantity(), 0.001); // 150 × 4
    }

    @Test
    void buildGroceryList_calledTwice_doesNotCompoundScaling() {
        Map<Recipe, Integer> plan = new LinkedHashMap<>();
        plan.put(new BatchCookMeal("Meal",
                Arrays.asList(new Meat("Chicken", 150, "g", 1.65, 0.31)), 30), 4);
        GroceryEngine engine = new GroceryEngine(plan);
        engine.buildGroceryList();
        List<Ingredient> list = engine.buildGroceryList();
        assertEquals(600.0, list.get(0).getQuantity(), 0.001); // still 4×, not 16×
    }

    // ---------------------------------------------------------------
    // buildGroceryList - deduplication
    // ---------------------------------------------------------------

    @Test
    void buildGroceryList_deduplicatesSameIngredientAcrossRecipes() {
        List<Ingredient> list =
                new GroceryEngine(planWithDuplicateIngredient()).buildGroceryList();
        assertEquals(1, list.size());
    }

    @Test
    void buildGroceryList_mergedQuantityIsCorrect() {
        // Meal A: 80 × 2 = 160 | Meal B: 80 × 3 = 240 -> total 400
        List<Ingredient> list =
                new GroceryEngine(planWithDuplicateIngredient()).buildGroceryList();
        assertEquals(400.0, list.get(0).getQuantity(), 0.001);
    }

    // ---------------------------------------------------------------
    // buildGroceryList - aisle sorting
    // ---------------------------------------------------------------

    @Test
    void buildGroceryList_isSortedByAisleSectionOrdinal() {
        List<Ingredient> list = new GroceryEngine(simplePlan()).buildGroceryList();
        for (int i = 0; i < list.size() - 1; i++) {
            assertTrue(
                    list.get(i).getSupermarketSection().ordinal()
                            <= list.get(i + 1).getSupermarketSection().ordinal(),
                    "List not sorted at index " + i
            );
        }
    }

    // ---------------------------------------------------------------
    // buildShoppingList - pantry integration
    // ---------------------------------------------------------------

    @Test
    void buildShoppingList_fullyStockedIngredient_excludedFromResult() {
        GroceryEngine engine = new GroceryEngine(simplePlan());
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Chicken", 9999.0);
        assertTrue(engine.buildShoppingList(pantry)
                .stream().noneMatch(n -> n.getName().equals("Chicken")));
    }

    @Test
    void buildShoppingList_unstockedIngredients_allAppearInResult() {
        GroceryEngine engine = new GroceryEngine(simplePlan());
        assertFalse(engine.buildShoppingList(new PantryTracker()).isEmpty());
    }

    // ---------------------------------------------------------------
    // computeWeeklyNutrition - both branches
    // ---------------------------------------------------------------

    @Test
    void computeWeeklyNutrition_withoutPriorScaling_scalesOnTheFly() {
        // Recipes start at 1 serving -> "currentServings != servings" branch IS taken
        GroceryEngine engine = new GroceryEngine(simplePlan());
        GroceryEngine.NutritionalSummary s = engine.computeWeeklyNutrition();
        assertTrue(s.getTotalCalories() > 0);
        assertTrue(s.getTotalProtein()  > 0);
    }

    @Test
    void computeWeeklyNutrition_afterBuildGroceryList_skipsDuplicateScaling() {
        // buildGroceryList already scaled -> "currentServings == servings" branch NOT taken
        GroceryEngine engine = new GroceryEngine(simplePlan());
        engine.buildGroceryList();
        GroceryEngine.NutritionalSummary s = engine.computeWeeklyNutrition();
        assertTrue(s.getTotalCalories() > 0);
        assertTrue(s.getTotalProtein()  > 0);
    }

    // ---------------------------------------------------------------
    // NutritionalSummary
    // ---------------------------------------------------------------

    @Test
    void nutritionalSummary_getters_returnCorrectValues() {
        GroceryEngine.NutritionalSummary s = new GroceryEngine.NutritionalSummary(2000, 150);
        assertEquals(2000.0, s.getTotalCalories(), 0.001);
        assertEquals(150.0,  s.getTotalProtein(),  0.001);
    }

    @Test
    void nutritionalSummary_toString_containsCaloriesAndProtein() {
        String str = new GroceryEngine.NutritionalSummary(2000, 150).toString();
        assertTrue(str.contains("2000"));
        assertTrue(str.contains("150"));
    }

    // ---------------------------------------------------------------
    // Print methods - smoke tests
    // ---------------------------------------------------------------

    @Test
    void printGroceryList_doesNotThrow() {
        assertDoesNotThrow(() -> new GroceryEngine(simplePlan()).printGroceryList());
    }

    @Test
    void printGroceryList_outputContainsExpectedHeaders() {
        String output = captureStdout(() -> new GroceryEngine(simplePlan()).printGroceryList());
        assertTrue(output.contains("FULL GROCERY LIST"));
        assertTrue(output.contains("Weekly totals"));
    }

    @Test
    void printShoppingList_doesNotThrow() {
        assertDoesNotThrow(() ->
                new GroceryEngine(simplePlan()).printShoppingList(new PantryTracker()));
    }

    @Test
    void printShoppingList_outputContainsItemCount() {
        String output = captureStdout(() ->
                new GroceryEngine(simplePlan()).printShoppingList(new PantryTracker()));
        assertTrue(output.contains("Items to buy"));
    }

    // ---------------------------------------------------------------
    // Helper - capture stdout
    // ---------------------------------------------------------------

    private String captureStdout(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        System.setOut(new PrintStream(buffer));
        try {
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString();
    }
}