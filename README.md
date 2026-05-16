# Meal Planner - Grocery Engine

A Java OOP project that takes a weekly meal plan and automatically produces a
deduplicated aisle-sorted grocery list, optionally subtracting what you already
have at home.

---

## What it does

Planning meals, scaling recipes for batch-cooking and writing grocery lists by hand is a tedious weekly chore. This engine automates that pipeline:

1. You define a weekly plan: a set of recipes and how many servings of each you want.
2. The engine scales every recipe to the requested serving count.
3. All ingredients across all recipes are merged and deduplicated (e.g. Brown Rice
   appearing in three meals becomes one combined entry).
4. The final list is sorted by supermarket aisle so you can shop in one clean pass:
   Produce -> Meat -> Dairy -> Dry Goods.
5. Optionally, your current pantry stock is subtracted - the engine only tells you
   what you actually need to buy.

---

## Project structure

```
src/
│
│  -- Interfaces --
├── Scalable.java           adjustPortions(int servings)
├── Nutritional.java        getCalories(), getProtein()
├── AisleMapped.java        getSupermarketSection()
│
│  -- Ingredient hierarchy --
├── AisleSection.java       Enum: PRODUCE, MEAT, DAIRY, DRY_GOODS
├── Ingredient.java         Abstract base class
├── Produce.java            Fruit, vegetables, herbs
├── Meat.java               Meat, poultry, fish
├── Dairy.java              Milk, yogurt, cheese, eggs
├── DryGoods.java           Rice, oats, pasta, canned goods
│
│  -- Recipe hierarchy --
├── Recipe.java             Abstract base class (holds List<Ingredient>)
├── BatchCookMeal.java      Large-format meals; adds prepTimeMinutes
├── QuickSnack.java         Light snacks; adds requiresCooking flag
├── ProteinShake.java       Blended shakes; adds flavorProfile
│
│  -- Engine & tracking --
├── GroceryEngine.java      Core orchestration: scale -> merge -> sort
├── PantryTracker.java      Tracks home stock; computes what to buy
│
│  -- Data & entry point --
├── MockData.java           Pre-built ingredients, recipes, plan, pantry
└── Main.java               Entry point
```

---

## Execution

### Requirements

- Java 11 or higher
- No external dependencies - standard library only

### Expected output

Running `Main` produces three sections:

```
=================================================
  FULL GROCERY LIST  (aisle order)
=================================================

  [ PRODUCE ]
    Banana                    840.0 g      [PRODUCE] | 748 kcal | 9.2 g protein
    Spinach                   ...
  [ MEAT ]
    ...
  [ DAIRY ]
    ...
  [ DRY_GOODS ]
    ...

  Weekly totals -> 14,820 kcal | 892.4 g protein
=================================================

=================================================
  PANTRY STOCK
=================================================
  In Stock:
    Banana                     240.0 g
    ...
  Out of Stock:
    Broccoli                   (0)
    ...
=================================================

=================================================
  SHOPPING LIST  (after pantry deduction)
=================================================

  [ PRODUCE ]
    Broccoli                   400.0 g    (buy 400)        [PRODUCE]
    ...

  Items to buy: 11
=================================================
```

---

## How to extend it

### Add a new ingredient type

Create a new class that extends `Ingredient` and returns the appropriate
`AisleSection` from `getSupermarketSection()`:

```java
public class FrozenGoods extends Ingredient {

    public FrozenGoods(String name, double quantity, String unit,
                       double caloriesPerUnit, double proteinPerUnit) {
        super(name, quantity, unit, caloriesPerUnit, proteinPerUnit);
    }

    @Override
    public AisleSection getSupermarketSection() {
        return AisleSection.DRY_GOODS; // or add FROZEN to the enum
    }
}
```

### Add a new recipe type

Extend `Recipe`, call `super(name, ingredients)` and implement `getRecipeType()`:

```java
public class SmoothieBowl extends Recipe {

    public SmoothieBowl(String name, List<Ingredient> ingredients) {
        super(name, ingredients);
    }

    @Override
    public String getRecipeType() { return "Smoothie Bowl"; }
}
```

### Add a new aisle

Open `AisleSection.java` and add a constant. Its position in the enum controls
where it appears in the sorted grocery list:

```java
public enum AisleSection {
    PRODUCE,
    MEAT,
    DAIRY,
    FROZEN,      // <- inserted between DAIRY and DRY_GOODS
    DRY_GOODS
}
```

### Define your own weekly plan

Skip `MockData` entirely and build your plan manually:

```java
Map<Recipe, Integer> myPlan = new LinkedHashMap<>();
myPlan.put(new BatchCookMeal("My Chilli",
        Arrays.asList(new Meat("Ground Beef", 150, "g", 2.50, 0.26),
                      new DryGoods("Canned Tomatoes", 200, "g", 0.24, 0.012)),
        40), 5);

GroceryEngine engine = new GroceryEngine(myPlan);
engine.printGroceryList();
```

### Track your own pantry

```java
PantryTracker pantry = new PantryTracker();
pantry.addStock("Ground Beef", 300.0);   // 300 g already at home
pantry.addStock("Canned Tomatoes", 400.0);

engine.printShoppingList(pantry);        // only shows what's missing
```

After cooking, deduct used ingredients so the pantry stays accurate:

```java
pantry.consumeStock("Ground Beef", 150.0);
```

---

## Class diagram (simplified)

```
«interface»          «interface»        «interface»
Scalable             Nutritional        AisleMapped
    │                    │                  │
    └──────────┬──────────┘                  │
               │                             │
           Recipe ◄────────────    Ingredient (abstract)
           (abstract)               ├── Produce
           ├── BatchCookMeal        ├── Meat
           ├── QuickSnack           ├── Dairy
           └── ProteinShake         └── DryGoods
                    │                       │
                    └──────────┬────────────┘
                               │
                         GroceryEngine
                               │
                         PantryTracker
```