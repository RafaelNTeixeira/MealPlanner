import data.MockData;
import engine.GroceryEngine;
import pantry.PantryTracker;
import recipe.Recipe;

import java.util.Map;

/**
 * Entry point - wires all features together and runs a demo using MockData.
 *
 * Flow:
 *   1. Load weekly plan and pantry stock from MockData.
 *   2. Create the GroceryEngine with the weekly plan.
 *   3. Print the full aisle-sorted grocery list (no pantry deduction).
 *   4. Print the pantry stock.
 *   5. Print the minimal shopping list (after pantry deduction).
 */
public class Main {

    public static void main(String[] args) {

        // 1. Load mock data
        Map<Recipe, Integer> weeklyPlan = MockData.weeklyPlan();
        PantryTracker pantry = new PantryTracker(MockData.pantryStock());

        // 2. Create the engine
        GroceryEngine engine = new GroceryEngine(weeklyPlan);

        // 3. Full grocery list (all ingredients, aisle-sorted)
        System.out.println();
        engine.printGroceryList();

        // 4. Current pantry state
        System.out.println();
        pantry.printStock();

        // 5. Shopping list - only what still needs to be bought
        System.out.println();
        engine.printShoppingList(pantry);
    }
}