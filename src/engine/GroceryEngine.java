package engine;

import ingredient.Ingredient;
import pantry.PantryTracker;
import recipe.Recipe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core orchestration engine of the meal planner.
 *
 * Given a weekly plan (a map of Recipe -> desired servings), the engine:
 *   1. Polymorphically calls {@code adjustPortions()} on every recipe.
 *   2. Extracts all ingredients into a flat master list.
 *   3. Deduplicates ingredients that appear in multiple recipes by summing
 *      their quantities (e.g. Brown Rice from three different meals becomes
 *      one merged entry).
 *   4. Sorts the final list by {@link ingredient.AisleSection}
 *      ordinal, so the shopping list follows a natural store traversal order.
 *   5. Optionally integrates with a {@link PantryTracker} to subtract what is
 *      already at home and produce a minimal buy-only list.
 *
 * The engine is stateless between calls to {@link #buildGroceryList()} -
 * each call re-scales and re-merges from the original weekly plan.
 */
public class GroceryEngine {

    private final Map<Recipe, Integer> weeklyPlan;

    /**
     * @param weeklyPlan map of each recipe to the number of servings wanted
     *                   this week; must not be null or empty
     */
    public GroceryEngine(Map<Recipe, Integer> weeklyPlan) {
        if (weeklyPlan == null || weeklyPlan.isEmpty()) {
            throw new IllegalArgumentException("Weekly plan must not be null or empty.");
        }
        // Defensive copy so external mutation doesn't affect the engine
        this.weeklyPlan = new LinkedHashMap<>(weeklyPlan);
    }

    // ---------------------------------------------------------------
    // Core pipeline
    // ---------------------------------------------------------------

    /**
     * Executes the full grocery-list pipeline:
     * scale -> extract -> deduplicate -> sort.
     *
     * <p><b>Scaling</b>: {@code adjustPortions()} is called polymorphically on
     * every {@link Recipe} in the plan. Because Recipe is abstract and the call
     * is dispatched at runtime, BatchCookMeal, QuickSnack, and ProteinShake all
     * scale correctly without any instanceof checks here.</p>
     *
     * <p><b>Deduplication</b>: ingredients are keyed by {@code "name|unit"}.
     * The first occurrence of each key is kept as the representative object;
     * its quantity is updated to the running total. This avoids needing to
     * instantiate new (abstract) Ingredient objects.</p>
     *
     * <p><b>Sorting</b>: the merged list is sorted by
     * {@link ingredient.AisleSection#ordinal()}, which encodes
     * the recommended store-traversal order (PRODUCE -> MEAT -> DAIRY -> DRY_GOODS).
     * Within the same aisle, ingredients retain their merge-insertion order
     * (i.e. the order they first appeared across the recipes).</p>
     *
     * @return deduplicated, aisle-sorted list of ingredients for the week
     */
    public List<Ingredient> buildGroceryList() {

        // Step 1: scale every recipe to its requested serving count
        for (Map.Entry<Recipe, Integer> entry : weeklyPlan.entrySet()) {
            entry.getKey().adjustPortions(entry.getValue());
        }

        // Step 2: extract all ingredients from all scaled recipes
        List<Ingredient> allIngredients = new ArrayList<>();
        for (Recipe recipe : weeklyPlan.keySet()) {
            allIngredients.addAll(recipe.getIngredients());
        }

        // Step 3: deduplicate - merge quantities for same name+unit
        // mergedMap preserves the first Ingredient reference per key so we
        // retain the concrete subtype (and therefore the correct AisleSection).
        Map<String, Ingredient> mergedMap    = new LinkedHashMap<>();
        Map<String, Double>     totalQuantities = new LinkedHashMap<>();

        for (Ingredient ingredient : allIngredients) {
            String key = ingredient.getName().toLowerCase()
                    + "|"
                    + ingredient.getUnit().toLowerCase();

            if (!mergedMap.containsKey(key)) {
                mergedMap.put(key, ingredient);
                totalQuantities.put(key, ingredient.getQuantity());
            } else {
                totalQuantities.merge(key, ingredient.getQuantity(), Double::sum);
            }
        }

        // Apply the summed quantities to the representative Ingredient objects
        for (Map.Entry<String, Ingredient> entry : mergedMap.entrySet()) {
            entry.getValue().setQuantity(totalQuantities.get(entry.getKey()));
        }

        // Step 4: sort by AisleSection ordinal
        return mergedMap.values().stream()
                .sorted(Comparator.comparingInt(
                        i -> i.getSupermarketSection().ordinal()))
                .collect(Collectors.toList());
    }

    /**
     * Builds the full grocery list and then passes it through a
     * {@link PantryTracker} to subtract what is already at home.
     *
     * @param pantry the user's current pantry state
     * @return ordered list of {@link PantryTracker.ShoppingNeed} - only items
     *         not fully covered by the pantry appear here
     */
    public List<PantryTracker.ShoppingNeed> buildShoppingList(PantryTracker pantry) {
        List<Ingredient> groceryList = buildGroceryList();
        return pantry.computeShoppingList(groceryList);
    }

    // ---------------------------------------------------------------
    // Nutritional summary
    // ---------------------------------------------------------------

    /**
     * Computes the weekly macro totals from the scaled weekly plan.
     * Call after {@link #buildGroceryList()} to ensure recipes are scaled.
     *
     * @return a {@link NutritionalSummary} with total kcal and protein for
     *         the entire week
     */
    public NutritionalSummary computeWeeklyNutrition() {
        double totalCalories = 0;
        double totalProtein  = 0;

        for (Map.Entry<Recipe, Integer> entry : weeklyPlan.entrySet()) {
            // Recipes are already scaled by buildGroceryList(); if called
            // independently, scale on the fly here.
            Recipe recipe   = entry.getKey();
            int    servings = entry.getValue();
            if (recipe.getCurrentServings() != servings) {
                recipe.adjustPortions(servings);
            }
            totalCalories += recipe.getCalories();
            totalProtein  += recipe.getProtein();
        }

        return new NutritionalSummary(totalCalories, totalProtein);
    }

    // ---------------------------------------------------------------
    // Display helpers
    // ---------------------------------------------------------------

    /**
     * Prints the full aisle-sorted grocery list (before pantry subtraction).
     */
    public void printGroceryList() {
        List<Ingredient> list = buildGroceryList();

        System.out.println("=================================================");
        System.out.println("  FULL GROCERY LIST  (aisle order)");
        System.out.println("=================================================");

        String currentAisle = null;
        for (Ingredient ingredient : list) {
            String aisle = ingredient.getSupermarketSection().name();
            if (!aisle.equals(currentAisle)) {
                currentAisle = aisle;
                System.out.println("\n  [ " + currentAisle + " ]");
            }
            System.out.println("    " + ingredient);
        }

        NutritionalSummary summary = computeWeeklyNutrition();
        System.out.println();
        System.out.println(summary);
        System.out.println("=================================================");
    }

    /**
     * Prints only what still needs to be bought after accounting for pantry stock.
     *
     * @param pantry the user's current pantry state
     */
    public void printShoppingList(PantryTracker pantry) {
        List<PantryTracker.ShoppingNeed> list = buildShoppingList(pantry);

        System.out.println("=================================================");
        System.out.println("  SHOPPING LIST  (after pantry deduction)");
        System.out.println("=================================================");

        String currentAisle = null;
        for (PantryTracker.ShoppingNeed need : list) {
            if (!need.getAisleSection().equals(currentAisle)) {
                currentAisle = need.getAisleSection();
                System.out.println("\n  [ " + currentAisle + " ]");
            }
            System.out.println("    " + need);
        }

        System.out.println("\n  Items to buy: " + list.size());
        System.out.println("=================================================");
    }

    // ---------------------------------------------------------------
    // NutritionalSummary - value object
    // ---------------------------------------------------------------

    /**
     * Immutable weekly macro summary returned by {@link #computeWeeklyNutrition()}.
     */
    public static class NutritionalSummary {

        private final double totalCalories;
        private final double totalProtein;

        public NutritionalSummary(double totalCalories, double totalProtein) {
            this.totalCalories = totalCalories;
            this.totalProtein  = totalProtein;
        }

        public double getTotalCalories() { return totalCalories; }
        public double getTotalProtein()  { return totalProtein; }

        @Override
        public String toString() {
            return String.format("  Weekly totals -> %.0f kcal | %.1f g protein",
                    totalCalories, totalProtein);
        }
    }
}