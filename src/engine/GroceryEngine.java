package engine;

import ingredient.Ingredient;
import pantry.PantryTracker;
import recipe.Recipe;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Core engine responsible for processing a weekly meal plan into actionable lists.
 *
 * This class takes a map of recipes and their target servings, scales the ingredients
 * accordingly, deduplicates matching items across different recipes, and generates sorted
 * grocery and shopping lists. It also aggregates the total weekly nutritional
 * values for all planned meals.
 */
public class GroceryEngine {

    // ---------------------------------------------------------------
    // Fields
    // ---------------------------------------------------------------

    /** The meal plan for the week, mapping a specific {@link Recipe} to the
     * desired number of servings to prepare.
     */
    private final Map<Recipe, Integer> weeklyPlan;

    // ---------------------------------------------------------------
    // Constructor
    // ---------------------------------------------------------------

    /**
     * Constructs a new GroceryEngine with the provided weekly plan.
     *
     * @param weeklyPlan a map of recipes to their planned serving counts.
     * @throws IllegalArgumentException if the weekly plan is null or empty.
     */
    public GroceryEngine(Map<Recipe, Integer> weeklyPlan) {
        if (weeklyPlan == null || weeklyPlan.isEmpty())
            throw new IllegalArgumentException("Weekly plan must not be null or empty.");
        // Defensive copy using LinkedHashMap to preserve insertion/planning order
        this.weeklyPlan = new LinkedHashMap<>(weeklyPlan);
    }

    // ---------------------------------------------------------------
    // List Generation Methods
    // ---------------------------------------------------------------

    /**
     * Builds a comprehensive, scaled, deduplicated, and sorted grocery list
     * based on the weekly plan.
     * * The process follows four steps:
     * 1. Scales every recipe to the requested serving count.
     * 2. Extracts all ingredients from the scaled recipes.
     * 3. Deduplicates ingredients by merging quantities of items sharing the same name and unit.
     * 4. Sorts the final list by supermarket aisle section.
     *
     * @return a consolidated and sorted list of {@link Ingredient}s needed for the week.
     */
    public List<Ingredient> buildGroceryList() {

        // 1. Scale every recipe to its requested serving count
        for (Map.Entry<Recipe, Integer> entry : weeklyPlan.entrySet()) {
            entry.getKey().adjustPortions(entry.getValue());
        }

        // 2. Extract all ingredients from all scaled recipes
        List<Ingredient> allIngredients = new ArrayList<>();
        for (Recipe recipe : weeklyPlan.keySet()) {
            allIngredients.addAll(recipe.getIngredients());
        }

        // 3. Deduplicate — merge quantities for same name+unit
        Map<String, Ingredient> mergedMap       = new LinkedHashMap<>();
        Map<String, Double>     totalQuantities = new LinkedHashMap<>();

        for (Ingredient ingredient : allIngredients) {
            String key = ingredient.getName().toLowerCase() + "|" + ingredient.getUnit().toLowerCase();
            if (!mergedMap.containsKey(key)) {
                mergedMap.put(key, ingredient);
                totalQuantities.put(key, ingredient.getQuantity());
            } else {
                totalQuantities.merge(key, ingredient.getQuantity(), Double::sum);
            }
        }

        for (Map.Entry<String, Ingredient> entry : mergedMap.entrySet()) {
            entry.getValue().setQuantity(totalQuantities.get(entry.getKey()));
        }

        // 4. Sort by AisleSection ordinal
        return mergedMap.values().stream()
                .sorted(Comparator.comparingInt(i -> i.getSupermarketSection().ordinal()))
                .collect(Collectors.toList());
    }

    /**
     * Builds a final shopping list by subtracting ingredients that are already
     * available in the user's pantry from the total grocery list.
     *
     * @param pantry the {@link PantryTracker} containing the user's current inventory.
     * @return a list of {@link PantryTracker.ShoppingNeed}s representing exactly what needs to be bought.
     */
    public List<PantryTracker.ShoppingNeed> buildShoppingList(PantryTracker pantry) {
        return pantry.computeShoppingList(buildGroceryList());
    }

    // ---------------------------------------------------------------
    // Nutritional Calculation
    // ---------------------------------------------------------------

    /**
     * Computes the aggregated nutritional data (calories and protein) for the
     * entire weekly plan.
     * * Ensures that recipes are scaled to their target servings before calculations
     * are made to maintain accurate totals.
     *
     * @return a {@link NutritionalSummary} containing total weekly calories and protein.
     */
    public NutritionalSummary computeWeeklyNutrition() {
        double totalCalories = 0;
        double totalProtein  = 0;
        for (Map.Entry<Recipe, Integer> entry : weeklyPlan.entrySet()) {
            Recipe recipe   = entry.getKey();
            int    servings = entry.getValue();
            if (recipe.getCurrentServings() != servings) recipe.adjustPortions(servings);
            totalCalories += recipe.getCalories();
            totalProtein  += recipe.getProtein();
        }
        return new NutritionalSummary(totalCalories, totalProtein);
    }

    // ---------------------------------------------------------------
    // Output Helpers
    // ---------------------------------------------------------------

    /**
     * Prints the full grocery list to the console, neatly categorized by
     * supermarket aisle, followed by the weekly nutritional summary.
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

        System.out.println();
        System.out.println(computeWeeklyNutrition());
        System.out.println("=================================================");
    }

    /**
     * Prints the finalized shopping list to the console after deducting
     * items found in the pantry. Categorized by supermarket aisle.
     *
     * @param pantry the {@link PantryTracker} to check against.
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
    // Nested Classes
    // ---------------------------------------------------------------

    /**
     * A simple value object to hold the aggregated nutritional totals
     * for the entire weekly meal plan.
     */
    public static class NutritionalSummary {

        /** Total kilocalories for the week. */
        private final double totalCalories;

        /** Total grams of protein for the week. */
        private final double totalProtein;

        /**
         * @param totalCalories total kcal for all planned recipes
         * @param totalProtein  total grams of protein for all planned recipes
         */
        public NutritionalSummary(double totalCalories, double totalProtein) {
            this.totalCalories = totalCalories;
            this.totalProtein  = totalProtein;
        }

        public double getTotalCalories() { return totalCalories; }
        public double getTotalProtein()  { return totalProtein; }

        @Override
        public String toString() {
            return String.format("  Weekly totals → %.0f kcal | %.1f g protein", totalCalories, totalProtein);
        }
    }
}