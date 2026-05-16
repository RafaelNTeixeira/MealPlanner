package engine;

import ingredient.Ingredient;
import pantry.PantryTracker;
import recipe.Recipe;

import java.util.*;
import java.util.stream.Collectors;

public class GroceryEngine {

    private final Map<Recipe, Integer> weeklyPlan;

    public GroceryEngine(Map<Recipe, Integer> weeklyPlan) {
        if (weeklyPlan == null || weeklyPlan.isEmpty())
            throw new IllegalArgumentException("Weekly plan must not be null or empty.");
        this.weeklyPlan = new LinkedHashMap<>(weeklyPlan);
    }

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

    public List<PantryTracker.ShoppingNeed> buildShoppingList(PantryTracker pantry) {
        return pantry.computeShoppingList(buildGroceryList());
    }

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
    // NutritionalSummary value object
    // ---------------------------------------------------------------

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
            return String.format("  Weekly totals → %.0f kcal | %.1f g protein", totalCalories, totalProtein);
        }
    }
}