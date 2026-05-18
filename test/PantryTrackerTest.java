import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.*;

/**
 * Tests for PantryTracker and its inner ShoppingNeed value object.
 * Covers all public methods, both constructors, and every toString branch.
 */
class PantryTrackerTest {

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    @Test
    void emptyConstructor_createsEmptyPantry() {
        PantryTracker pantry = new PantryTracker();
        assertEquals(0.0, pantry.getStock("Anything"));
        assertFalse(pantry.isInStock("Anything"));
    }

    @Test
    void mapConstructor_loadsStockCorrectly() {
        Map<String, Double> stock = new LinkedHashMap<>();
        stock.put("Chicken", 300.0);
        stock.put("Rice",    200.0);
        PantryTracker pantry = new PantryTracker(stock);
        assertEquals(300.0, pantry.getStock("Chicken"), 0.001);
        assertEquals(200.0, pantry.getStock("Rice"),    0.001);
    }

    @Test
    void mapConstructor_negativeQuantity_throws() {
        Map<String, Double> stock = new HashMap<>();
        stock.put("Chicken", -10.0);
        assertThrows(IllegalArgumentException.class, () -> new PantryTracker(stock));
    }

    // ---------------------------------------------------------------
    // addStock(Ingredient)
    // ---------------------------------------------------------------

    @Test
    void addStock_ingredient_setsInitialQuantity() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock(new Meat("Chicken", 150, "g", 1.65, 0.31));
        assertEquals(150.0, pantry.getStock("Chicken"), 0.001);
    }

    @Test
    void addStock_ingredient_calledTwice_accumulatesQuantity() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock(new Meat("Chicken", 150, "g", 1.65, 0.31));
        pantry.addStock(new Meat("Chicken", 100, "g", 1.65, 0.31));
        assertEquals(250.0, pantry.getStock("Chicken"), 0.001);
    }

    @Test
    void addStock_ingredient_recordsUnitForDisplay() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock(new Meat("Chicken", 150, "g", 1.65, 0.31));
        List<PantryTracker.ShoppingNeed> needs = pantry.computeShoppingList(
                Arrays.asList(new Meat("Chicken", 400, "g", 1.65, 0.31)));
        assertEquals("g", needs.get(0).getUnit());
    }

    @Test
    void addStock_ingredient_putIfAbsent_doesNotOverwriteUnit() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock(new Meat("Chicken", 150, "g", 1.65, 0.31));
        pantry.addStock(new Meat("Chicken", 100, "g", 1.65, 0.31));
        List<PantryTracker.ShoppingNeed> needs = pantry.computeShoppingList(
                Arrays.asList(new Meat("Chicken", 500, "g", 1.65, 0.31)));
        assertEquals("g", needs.get(0).getUnit());
    }

    // ---------------------------------------------------------------
    // addStock(String, double)
    // ---------------------------------------------------------------

    @Test
    void addStock_string_setsQuantity() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 200.0);
        assertEquals(200.0, pantry.getStock("Rice"), 0.001);
    }

    @Test
    void addStock_string_accumulates() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 100.0);
        pantry.addStock("Rice", 150.0);
        assertEquals(250.0, pantry.getStock("Rice"), 0.001);
    }

    @Test
    void addStock_string_zero_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new PantryTracker().addStock("Rice", 0));
    }

    @Test
    void addStock_string_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new PantryTracker().addStock("Rice", -5));
    }

    // ---------------------------------------------------------------
    // consumeStock(String, double)
    // ---------------------------------------------------------------

    @Test
    void consumeStock_string_reducesQuantity() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 300.0);
        pantry.consumeStock("Rice", 100.0);
        assertEquals(200.0, pantry.getStock("Rice"), 0.001);
    }

    @Test
    void consumeStock_string_floorsAtZero() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 50.0);
        pantry.consumeStock("Rice", 200.0);
        assertEquals(0.0, pantry.getStock("Rice"), 0.001);
    }

    @Test
    void consumeStock_unknownIngredient_remainsZero() {
        PantryTracker pantry = new PantryTracker();
        pantry.consumeStock("Ghost", 50.0);
        assertEquals(0.0, pantry.getStock("Ghost"), 0.001);
    }

    @Test
    void consumeStock_string_zero_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new PantryTracker().consumeStock("Rice", 0));
    }

    @Test
    void consumeStock_string_negative_throws() {
        assertThrows(IllegalArgumentException.class,
                () -> new PantryTracker().consumeStock("Rice", -5));
    }

    // ---------------------------------------------------------------
    // consumeStock(Ingredient)
    // ---------------------------------------------------------------

    @Test
    void consumeStock_ingredient_reducesQuantity() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Chicken", 300.0);
        pantry.consumeStock(new Meat("Chicken", 150, "g", 1.65, 0.31));
        assertEquals(150.0, pantry.getStock("Chicken"), 0.001);
    }

    // ---------------------------------------------------------------
    // consumeAll
    // ---------------------------------------------------------------

    @Test
    void consumeAll_reducesEveryIngredientInList() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Chicken", 300.0);
        pantry.addStock("Rice",    200.0);
        pantry.consumeAll(Arrays.asList(
                new Meat("Chicken",   150, "g", 1.65,  0.31),
                new DryGoods("Rice",   80, "g", 3.62, 0.075)
        ));
        assertEquals(150.0, pantry.getStock("Chicken"), 0.001);
        assertEquals(120.0, pantry.getStock("Rice"),    0.001);
    }

    // ---------------------------------------------------------------
    // isInStock
    // ---------------------------------------------------------------

    @Test
    void isInStock_positiveQuantity_returnsTrue() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 100.0);
        assertTrue(pantry.isInStock("Rice"));
    }

    @Test
    void isInStock_zeroQuantity_returnsFalse() {
        Map<String, Double> stock = new HashMap<>();
        stock.put("Rice", 0.0);
        assertFalse(new PantryTracker(stock).isInStock("Rice"));
    }

    @Test
    void isInStock_unknownIngredient_returnsFalse() {
        assertFalse(new PantryTracker().isInStock("Ghost"));
    }

    // ---------------------------------------------------------------
    // getAllStock
    // ---------------------------------------------------------------

    @Test
    void getAllStock_returnsUnmodifiableView() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 100.0);
        assertThrows(UnsupportedOperationException.class,
                () -> pantry.getAllStock().put("New", 1.0));
    }

    @Test
    void getAllStock_containsLoadedEntries() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 100.0);
        assertTrue(pantry.getAllStock().containsKey("Rice"));
    }

    // ---------------------------------------------------------------
    // computeShoppingList - three coverage scenarios
    // ---------------------------------------------------------------

    @Test
    void computeShoppingList_fullyCovered_itemExcludedFromResult() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 500.0);
        List<PantryTracker.ShoppingNeed> needs = pantry.computeShoppingList(
                Arrays.asList(new DryGoods("Rice", 200, "g", 3.62, 0.075)));
        assertTrue(needs.isEmpty());
    }

    @Test
    void computeShoppingList_partiallyCovered_returnsDelta() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 100.0);
        List<PantryTracker.ShoppingNeed> needs = pantry.computeShoppingList(
                Arrays.asList(new DryGoods("Rice", 300, "g", 3.62, 0.075)));
        assertEquals(1,     needs.size());
        assertEquals(200.0, needs.get(0).getToBuy(),         0.001);
        assertEquals(100.0, needs.get(0).getInStock(),       0.001);
        assertEquals(300.0, needs.get(0).getTotalRequired(), 0.001);
    }

    @Test
    void computeShoppingList_notInStock_returnsFullAmount() {
        List<PantryTracker.ShoppingNeed> needs = new PantryTracker().computeShoppingList(
                Arrays.asList(new DryGoods("Rice", 300, "g", 3.62, 0.075)));
        assertEquals(1,     needs.size());
        assertEquals(300.0, needs.get(0).getToBuy(),   0.001);
        assertEquals(0.0,   needs.get(0).getInStock(), 0.001);
    }

    @Test
    void computeShoppingList_fallsBackToIngredientUnit_whenNotInUnitsMap() {
        // Pantry loaded from raw map has no units entries -> must use ingredient.getUnit()
        Map<String, Double> stock = new HashMap<>();
        stock.put("Rice", 0.0);
        List<PantryTracker.ShoppingNeed> needs = new PantryTracker(stock).computeShoppingList(
                Arrays.asList(new DryGoods("Rice", 300, "g", 3.62, 0.075)));
        assertEquals("g", needs.get(0).getUnit());
    }

    @Test
    void computeShoppingList_doesNotMutatePantryStock() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 100.0);
        pantry.computeShoppingList(
                Arrays.asList(new DryGoods("Rice", 300, "g", 3.62, 0.075)));
        assertEquals(100.0, pantry.getStock("Rice"), 0.001);
    }

    // ---------------------------------------------------------------
    // printStock - branch coverage (both "(none)" paths)
    // ---------------------------------------------------------------

    @Test
    void printStock_mixedStock_doesNotThrow() {
        Map<String, Double> stock = new LinkedHashMap<>();
        stock.put("Chicken", 300.0);
        stock.put("Rice",      0.0);
        assertDoesNotThrow(() -> new PantryTracker(stock).printStock());
    }

    @Test
    void printStock_allInStock_showsNoneForOutOfStockSection() {
        PantryTracker pantry = new PantryTracker();
        pantry.addStock("Rice", 100.0);
        assertTrue(captureStdout(pantry::printStock).contains("(none)"));
    }

    @Test
    void printStock_allOutOfStock_showsNoneForInStockSection() {
        Map<String, Double> stock = new LinkedHashMap<>();
        stock.put("Rice", 0.0);
        assertTrue(captureStdout(new PantryTracker(stock)::printStock).contains("(none)"));
    }

    // ---------------------------------------------------------------
    // ShoppingNeed getters
    // ---------------------------------------------------------------

    @Test
    void shoppingNeed_getters_returnCorrectValues() {
        PantryTracker.ShoppingNeed need =
                new PantryTracker.ShoppingNeed("Rice", 300, 100, 200, "g", "DRY_GOODS");
        assertEquals("Rice",     need.getName());
        assertEquals(300.0,      need.getTotalRequired(), 0.001);
        assertEquals(100.0,      need.getInStock(),       0.001);
        assertEquals(200.0,      need.getToBuy(),         0.001);
        assertEquals("g",        need.getUnit());
        assertEquals("DRY_GOODS",need.getAisleSection());
    }

    // ---------------------------------------------------------------
    // ShoppingNeed.toString - both branches
    // ---------------------------------------------------------------

    @Test
    void shoppingNeed_toString_withPartialStock_showsHaveAndNeedMore() {
        PantryTracker.ShoppingNeed need =
                new PantryTracker.ShoppingNeed("Rice", 300, 100, 200, "g", "DRY_GOODS");
        String s = need.toString();
        assertTrue(s.contains("have"));
        assertTrue(s.contains("need"));
    }

    @Test
    void shoppingNeed_toString_withNoStock_showsBuy() {
        PantryTracker.ShoppingNeed need =
                new PantryTracker.ShoppingNeed("Rice", 300, 0, 300, "g", "DRY_GOODS");
        assertTrue(need.toString().contains("buy"));
    }

    // ---------------------------------------------------------------
    // Helper - capture stdout without polluting test output
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