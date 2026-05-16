package data;

import ingredient.*;
import recipe.*;

import java.util.*;

/**
 * Static factory that provides pre-built sample data for development and testing.
 *
 * Organised into four layers that mirror the real runtime flow:
 *   1. Ingredients  - base 1-serving quantities with realistic macro values
 *   2. Recipes      - one of each subtype, composed of the ingredients above
 *   3. Weekly plan  - a Map<Recipe, Integer> (recipe -> desired servings for the week)
 *   4. Pantry stock - a Map<String, Double> representing what is already at home
 *
 * All public methods return fresh, independent instances so tests can mutate
 * freely without affecting each other.
 *
 * Nutritional values are approximated from standard food databases (per gram / per ml).
 */
public class MockData {

    // ---------------------------------------------------------------
    // Prevent instantiation - this is a pure static factory
    // ---------------------------------------------------------------
    private MockData() {}

    // ================================================================
    //  1. INGREDIENTS
    // ================================================================

    // --- Produce ---

    public static Produce banana() {
        // 1 medium banana ~ 120 g | ~89 kcal/100g | ~1.1 g protein/100g
        return new Produce("Banana", 120, "g", 0.89, 0.011);
    }

    public static Produce spinach() {
        // 1 handful ~ 30 g | ~23 kcal/100g | ~2.9 g protein/100g
        return new Produce("Spinach", 30, "g", 0.23, 0.029);
    }

    public static Produce broccoli() {
        // 1 portion ~ 80 g | ~34 kcal/100g | ~2.8 g protein/100g
        return new Produce("Broccoli", 80, "g", 0.34, 0.028);
    }

    public static Produce cherry_tomatoes() {
        // 1 portion ~ 100 g | ~18 kcal/100g | ~0.9 g protein/100g
        return new Produce("Cherry Tomatoes", 100, "g", 0.18, 0.009);
    }

    // --- Meat ---

    public static Meat chickenBreast() {
        // 1 portion ~ 150 g | ~165 kcal/100g | ~31 g protein/100g
        return new Meat("Chicken Breast", 150, "g", 1.65, 0.310);
    }

    public static Meat salmonFillet() {
        // 1 portion ~ 130 g | ~208 kcal/100g | ~20 g protein/100g
        return new Meat("Salmon Fillet", 130, "g", 2.08, 0.200);
    }

    public static Meat groundBeef() {
        // 1 portion ~ 120 g | ~250 kcal/100g | ~26 g protein/100g
        return new Meat("Ground Beef", 120, "g", 2.50, 0.260);
    }

    // --- Dairy ---

    public static Dairy wholeMilk() {
        // 1 glass ~ 200 ml | ~61 kcal/100ml | ~3.2 g protein/100ml
        return new Dairy("Whole Milk", 200, "ml", 0.61, 0.032);
    }

    public static Dairy greekYogurt() {
        // 1 portion ~ 150 g | ~59 kcal/100g | ~10 g protein/100g
        return new Dairy("Greek Yogurt", 150, "g", 0.59, 0.100);
    }

    public static Dairy cheddarCheese() {
        // 1 serving ~ 30 g | ~403 kcal/100g | ~25 g protein/100g
        return new Dairy("Cheddar Cheese", 30, "g", 4.03, 0.250);
    }

    // --- Dry Goods ---

    public static DryGoods brownRice() {
        // 1 portion (dry) ~ 80 g | ~362 kcal/100g | ~7.5 g protein/100g
        return new DryGoods("Brown Rice", 80, "g", 3.62, 0.075);
    }

    public static DryGoods rolledOats() {
        // 1 portion ~ 60 g | ~389 kcal/100g | ~13 g protein/100g
        return new DryGoods("Rolled Oats", 60, "g", 3.89, 0.130);
    }

    public static DryGoods wheyProtein() {
        // 1 scoop ~ 30 g | ~373 kcal/100g | ~75 g protein/100g
        return new DryGoods("Whey Protein Powder", 30, "g", 3.73, 0.750);
    }

    public static DryGoods peanutButter() {
        // 2 tablespoons ~ 32 g | ~588 kcal/100g | ~25 g protein/100g
        return new DryGoods("Peanut Butter", 32, "g", 5.88, 0.250);
    }

    public static DryGoods cannedChickpeas() {
        // 1 portion ~ 120 g | ~164 kcal/100g | ~8.9 g protein/100g
        return new DryGoods("Canned Chickpeas", 120, "g", 1.64, 0.089);
    }

    // ================================================================
    //  2. RECIPES
    // ================================================================

    /**
     * Chicken & Rice Bowl - classic high-protein batch meal.
     * Estimated prep + cook time: 45 min.
     */
    public static BatchCookMeal chickenRiceBowl() {
        return new BatchCookMeal(
                "Chicken & Rice Bowl",
                Arrays.asList(chickenBreast(), brownRice(), broccoli(), spinach()),
                45
        );
    }

    /**
     * Beef & Veggie Stir-Fry - quick weeknight batch meal.
     * Estimated prep + cook time: 35 min.
     */
    public static BatchCookMeal beefStirFry() {
        return new BatchCookMeal(
                "Beef & Veggie Stir-Fry",
                Arrays.asList(groundBeef(), brownRice(), broccoli(), cherry_tomatoes()),
                35
        );
    }

    /**
     * Salmon & Cheese Bowl - omega-3 rich batch meal.
     * Estimated prep + cook time: 30 min.
     */
    public static BatchCookMeal salmonBowl() {
        return new BatchCookMeal(
                "Salmon & Cheese Bowl",
                Arrays.asList(salmonFillet(), brownRice(), spinach(), cheddarCheese()),
                30
        );
    }

    /**
     * Yogurt & Banana - no-cook grab-and-go snack.
     */
    public static QuickSnack yogurtBanana() {
        return new QuickSnack(
                "Yogurt & Banana",
                Arrays.asList(greekYogurt(), banana()),
                false
        );
    }

    /**
     * Peanut Butter Oats - warm snack, requires brief cooking.
     */
    public static QuickSnack peanutButterOats() {
        return new QuickSnack(
                "Peanut Butter Oats",
                Arrays.asList(rolledOats(), peanutButter(), wholeMilk()),
                true
        );
    }

    /**
     * Chocolate Protein Shake - post-workout shake.
     */
    public static ProteinShake chocolateShake() {
        return new ProteinShake(
                "Chocolate Protein Shake",
                Arrays.asList(wheyProtein(), wholeMilk(), banana()),
                "Chocolate"
        );
    }

    /**
     * Vanilla Chickpea Shake - plant-forward shake variant.
     */
    public static ProteinShake vanillaShake() {
        return new ProteinShake(
                "Vanilla Chickpea Shake",
                Arrays.asList(wheyProtein(), wholeMilk(), cannedChickpeas()),
                "Vanilla"
        );
    }

    // ================================================================
    //  3. WEEKLY PLAN
    //     Maps each recipe to the number of servings wanted this week.
    //     Uses LinkedHashMap to preserve insertion order for display.
    // ================================================================

    /**
     * Returns a realistic weekly meal plan.
     *
     * <pre>
     *  Chicken & Rice Bowl      -> 5 servings  (Mon–Fri lunch)
     *  Beef & Veggie Stir-Fry   -> 3 servings  (Mon, Wed, Fri dinner)
     *  Salmon & Cheese Bowl     -> 2 servings  (Tue & Thu dinner)
     *  Yogurt & Banana          -> 5 servings  (weekday morning snack)
     *  Peanut Butter Oats       -> 2 servings  (weekend breakfast)
     *  Chocolate Protein Shake  -> 4 servings  (post-workout, 4 days)
     *  Vanilla Chickpea Shake   -> 3 servings  (lighter days)
     * </pre>
     */
    public static Map<Recipe, Integer> weeklyPlan() {
        Map<Recipe, Integer> plan = new LinkedHashMap<>();
        plan.put(chickenRiceBowl(),   5);
        plan.put(beefStirFry(),       3);
        plan.put(salmonBowl(),        2);
        plan.put(yogurtBanana(),      5);
        plan.put(peanutButterOats(),  2);
        plan.put(chocolateShake(),    4);
        plan.put(vanillaShake(),      3);
        return plan;
    }

    // ================================================================
    //  4. PANTRY STOCK
    //     Represents what the user already has at home.
    //     Key   -> ingredient name (must match Ingredient.getName() exactly)
    //     Value -> quantity on hand (in the same unit as the ingredient)
    // ================================================================

    /**
     * Returns the current home pantry stock.
     *
     * Quantities are intentionally partial - some items are fully stocked,
     * some partially stocked, and a few are absent entirely - so the
     * PantryTracker has meaningful subtractions to perform.
     */
    public static Map<String, Double> pantryStock() {
        Map<String, Double> pantry = new LinkedHashMap<>();

        // Produce
        pantry.put("Banana",          240.0);   // 2 servings worth
        pantry.put("Spinach",          60.0);   // 2 servings worth
        pantry.put("Broccoli",          0.0);   // out of stock
        pantry.put("Cherry Tomatoes", 100.0);   // 1 serving worth

        // Meat
        pantry.put("Chicken Breast",  300.0);   // 2 servings worth
        pantry.put("Salmon Fillet",     0.0);   // out of stock
        pantry.put("Ground Beef",     120.0);   // 1 serving worth

        // Dairy
        pantry.put("Whole Milk",      400.0);   // 2 servings worth
        pantry.put("Greek Yogurt",    150.0);   // 1 serving worth
        pantry.put("Cheddar Cheese",   30.0);   // 1 serving worth

        // Dry Goods
        pantry.put("Brown Rice",      240.0);   // 3 servings worth
        pantry.put("Rolled Oats",      60.0);   // 1 serving worth
        pantry.put("Whey Protein Powder", 90.0);// 3 servings worth
        pantry.put("Peanut Butter",     0.0);   // out of stock
        pantry.put("Canned Chickpeas",  0.0);   // out of stock

        return pantry;
    }
}