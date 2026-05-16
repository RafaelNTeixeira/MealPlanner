package pantry;

import ingredient.Ingredient;

import java.util.*;

/**
 * Tracks the user's current home stock of ingredients.
 *
 * Responsibilities:
 *   - Store and update ingredient quantities on hand.
 *   - Given a list of required ingredients (e.g. from GroceryEngine after
 *     scaling), compute what still needs to be bought by subtracting stock.
 *   - Deduct ingredients from stock after a shopping trip or after cooking.
 *
 * Unit handling:
 *   Units are recorded when an Ingredient is added via {@link #addStock(Ingredient)}.
 *   Stock loaded from a raw Map (e.g. MockData) has no unit metadata; those
 *   entries display a "-" in the unit column. In both cases arithmetic is
 *   correct - units are purely cosmetic here.
 *
 * Thread safety: not thread-safe; designed for single-threaded use.
 */
public class PantryTracker {

    // ---------------------------------------------------------------
    // Internal storage
    // ---------------------------------------------------------------

    /** Ingredient name -> quantity currently in stock. */
    private final Map<String, Double> stock;

    /**
     * Ingredient name -> unit string (populated when stock is added via
     * {@link #addStock(Ingredient)}; absent for raw-map-loaded entries).
     */
    private final Map<String, String> units;

    // ---------------------------------------------------------------
    // Constructors
    // ---------------------------------------------------------------

    /** Creates an empty pantry. */
    public PantryTracker() {
        this.stock = new LinkedHashMap<>();
        this.units = new LinkedHashMap<>();
    }

    /**
     * Creates a pantry preloaded from a raw name->quantity map (e.g. MockData).
     * Units are not known for entries loaded this way; they display as "-".
     *
     * @param initialStock map of ingredient name to quantity on hand
     */
    public PantryTracker(Map<String, Double> initialStock) {
        this();
        for (Map.Entry<String, Double> entry : initialStock.entrySet()) {
            if (entry.getValue() < 0) {
                throw new IllegalArgumentException(
                        "Stock quantity cannot be negative for: " + entry.getKey());
            }
            stock.put(entry.getKey(), entry.getValue());
        }
    }

    // ---------------------------------------------------------------
    // Stock management
    // ---------------------------------------------------------------

    /**
     * Adds a quantity of an ingredient to stock (e.g. after a shopping trip).
     * Also records the ingredient's unit for display purposes.
     * If the ingredient is already in stock, the quantities are summed.
     *
     * @param ingredient the ingredient to add
     */
    public void addStock(Ingredient ingredient) {
        String name = ingredient.getName();
        stock.merge(name, ingredient.getQuantity(), Double::sum);
        units.putIfAbsent(name, ingredient.getUnit());
    }

    /**
     * Adds a raw quantity to an existing stock entry (unit-agnostic).
     * Useful for restocking without a full Ingredient object.
     *
     * @param name     ingredient name (must match exactly)
     * @param quantity amount to add (must be > 0)
     */
    public void addStock(String name, double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Added quantity must be positive. Got: " + quantity);
        }
        stock.merge(name, quantity, Double::sum);
    }

    /**
     * Deducts a quantity from stock (e.g. after cooking a meal).
     * Stock floors at 0 - it never goes negative.
     *
     * @param name     ingredient name
     * @param quantity amount to consume (must be > 0)
     */
    public void consumeStock(String name, double quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Consumed quantity must be positive. Got: " + quantity);
        }
        double current = stock.getOrDefault(name, 0.0);
        stock.put(name, Math.max(0.0, current - quantity));
    }

    /**
     * Convenience overload - deducts the ingredient's current quantity from stock.
     *
     * @param ingredient the ingredient (and its quantity) to consume
     */
    public void consumeStock(Ingredient ingredient) {
        consumeStock(ingredient.getName(), ingredient.getQuantity());
    }

    /**
     * Deducts every ingredient in the list from stock.
     * Useful after cooking a full recipe.
     *
     * @param ingredients the list of ingredients to consume
     */
    public void consumeAll(List<Ingredient> ingredients) {
        for (Ingredient ingredient : ingredients) {
            consumeStock(ingredient);
        }
    }

    // ---------------------------------------------------------------
    // Stock queries
    // ---------------------------------------------------------------

    /**
     * @param name ingredient name
     * @return quantity currently in stock (0.0 if not found)
     */
    public double getStock(String name) {
        return stock.getOrDefault(name, 0.0);
    }

    /**
     * @param name ingredient name
     * @return {@code true} if at least some stock exists (quantity > 0)
     */
    public boolean isInStock(String name) {
        return stock.getOrDefault(name, 0.0) > 0.0;
    }

    /**
     * @return an unmodifiable view of the full stock map
     */
    public Map<String, Double> getAllStock() {
        return Collections.unmodifiableMap(stock);
    }

    // ---------------------------------------------------------------
    // Core feature - compute what still needs to be bought
    // ---------------------------------------------------------------

    /**
     * Given a list of required ingredients (already scaled to the weekly plan),
     * returns a {@link ShoppingNeed} for each ingredient that is not fully
     * covered by the current pantry stock.
     *
     * Three possible outcomes per ingredient:
     * <ul>
     *   <li><b>Fully covered</b>  - stock >= required -> excluded from result</li>
     *   <li><b>Partially covered</b> - 0 < stock < required -> added with the delta</li>
     *   <li><b>Not in stock</b>   - stock = 0 -> added with full required quantity</li>
     * </ul>
     *
     * This method does NOT mutate pantry stock. Call {@link #consumeAll} or
     * {@link #consumeStock(Ingredient)} separately when items are actually used.
     *
     * @param required ingredients needed for the weekly plan (scaled quantities)
     * @return ordered list of shopping needs, preserving ingredient list order
     */
    public List<ShoppingNeed> computeShoppingList(List<Ingredient> required) {
        List<ShoppingNeed> shoppingList = new ArrayList<>();

        for (Ingredient ingredient : required) {
            double inStock   = stock.getOrDefault(ingredient.getName(), 0.0);
            double needed    = ingredient.getQuantity();
            double toBuy     = needed - inStock;

            if (toBuy > 0) {
                String unit = units.getOrDefault(
                        ingredient.getName(),
                        ingredient.getUnit()
                );
                shoppingList.add(new ShoppingNeed(
                        ingredient.getName(),
                        needed,
                        inStock,
                        toBuy,
                        unit,
                        ingredient.getSupermarketSection().name()
                ));
            }
        }

        return shoppingList;
    }

    // ---------------------------------------------------------------
    // Display
    // ---------------------------------------------------------------

    /**
     * Prints the full pantry contents to stdout, grouped by in-stock vs empty.
     */
    public void printStock() {
        System.out.println("=================================================");
        System.out.println("  PANTRY STOCK");
        System.out.println("=================================================");

        List<Map.Entry<String, Double>> inStock = new ArrayList<>();
        List<Map.Entry<String, Double>> empty   = new ArrayList<>();

        for (Map.Entry<String, Double> entry : stock.entrySet()) {
            (entry.getValue() > 0 ? inStock : empty).add(entry);
        }

        System.out.println("  In Stock:");
        if (inStock.isEmpty()) {
            System.out.println("    (none)");
        } else {
            for (Map.Entry<String, Double> entry : inStock) {
                String unit = units.getOrDefault(entry.getKey(), "-");
                System.out.printf("    %-25s %6.1f %s%n",
                        entry.getKey(), entry.getValue(), unit);
            }
        }

        System.out.println("  Out of Stock:");
        if (empty.isEmpty()) {
            System.out.println("    (none)");
        } else {
            for (Map.Entry<String, Double> entry : empty) {
                System.out.printf("    %-25s  (0)%n", entry.getKey());
            }
        }

        System.out.println("=================================================");
    }

    // ---------------------------------------------------------------
    // ShoppingNeed
    // ---------------------------------------------------------------

    /**
     * Immutable snapshot of a single ingredient's shopping requirement.
     * Carries enough context to display a meaningful shopping list entry
     * without needing to reach back into the Ingredient hierarchy.
     */
    public static class ShoppingNeed {

        private final String name;
        private final double totalRequired;
        private final double inStock;
        private final double toBuy;
        private final String unit;
        private final String aisleSection;

        public ShoppingNeed(String name,
                            double totalRequired,
                            double inStock,
                            double toBuy,
                            String unit,
                            String aisleSection) {
            this.name          = name;
            this.totalRequired = totalRequired;
            this.inStock       = inStock;
            this.toBuy         = toBuy;
            this.unit          = unit;
            this.aisleSection  = aisleSection;
        }

        public String getName()          { return name; }
        public double getTotalRequired() { return totalRequired; }
        public double getInStock()       { return inStock; }
        public double getToBuy()         { return toBuy; }
        public String getUnit()          { return unit; }
        public String getAisleSection()  { return aisleSection; }

        @Override
        public String toString() {
            String coverage = inStock > 0
                    ? String.format("(have %.0f, need %.0f more)", inStock, toBuy)
                    : String.format("(buy %.0f)", toBuy);

            return String.format("%-25s %6.1f %-6s %-30s [%s]",
                    name, toBuy, unit, coverage, aisleSection);
        }
    }
}