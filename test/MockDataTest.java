import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.lang.reflect.Constructor;

/**
 * Smoke tests for MockData - verifies every factory method returns the correct
 * concrete type and that the plan/pantry maps are non-empty.
 *
 * The private constructor is exercised via reflection to reach 100% class coverage.
 */
class MockDataTest {

    // ---------------------------------------------------------------
    // Ingredient factories - type checks
    // ---------------------------------------------------------------

    @Test void banana_returnsProduceWithCorrectName() {
        Produce p = MockData.banana();
        assertNotNull(p);
        assertEquals("Banana", p.getName());
    }

    @Test void spinach_returnsProduce()         { assertInstanceOf(Produce.class,   MockData.spinach()); }
    @Test void broccoli_returnsProduce()        { assertInstanceOf(Produce.class,   MockData.broccoli()); }
    @Test void cherryTomatoes_returnsProduce()  { assertInstanceOf(Produce.class,   MockData.cherryTomatoes()); }
    @Test void chickenBreast_returnsMeat()      { assertInstanceOf(Meat.class,      MockData.chickenBreast()); }
    @Test void salmonFillet_returnsMeat()       { assertInstanceOf(Meat.class,      MockData.salmonFillet()); }
    @Test void groundBeef_returnsMeat()         { assertInstanceOf(Meat.class,      MockData.groundBeef()); }
    @Test void wholeMilk_returnsDairy()         { assertInstanceOf(Dairy.class,     MockData.wholeMilk()); }
    @Test void greekYogurt_returnsDairy()       { assertInstanceOf(Dairy.class,     MockData.greekYogurt()); }
    @Test void cheddarCheese_returnsDairy()     { assertInstanceOf(Dairy.class,     MockData.cheddarCheese()); }
    @Test void brownRice_returnsDryGoods()      { assertInstanceOf(DryGoods.class,  MockData.brownRice()); }
    @Test void rolledOats_returnsDryGoods()     { assertInstanceOf(DryGoods.class,  MockData.rolledOats()); }
    @Test void wheyProtein_returnsDryGoods()    { assertInstanceOf(DryGoods.class,  MockData.wheyProtein()); }
    @Test void peanutButter_returnsDryGoods()   { assertInstanceOf(DryGoods.class,  MockData.peanutButter()); }
    @Test void cannedChickpeas_returnsDryGoods(){ assertInstanceOf(DryGoods.class,  MockData.cannedChickpeas()); }

    // ---------------------------------------------------------------
    // Recipe factories - type checks
    // ---------------------------------------------------------------

    @Test void chickenRiceBowl_returnsBatchCookMeal()  { assertInstanceOf(BatchCookMeal.class,  MockData.chickenRiceBowl()); }
    @Test void beefStirFry_returnsBatchCookMeal()      { assertInstanceOf(BatchCookMeal.class,  MockData.beefStirFry()); }
    @Test void salmonBowl_returnsBatchCookMeal()       { assertInstanceOf(BatchCookMeal.class,  MockData.salmonBowl()); }
    @Test void yogurtBanana_returnsQuickSnack()        { assertInstanceOf(QuickSnack.class,     MockData.yogurtBanana()); }
    @Test void peanutButterOats_returnsQuickSnack()    { assertInstanceOf(QuickSnack.class,     MockData.peanutButterOats()); }
    @Test void chocolateShake_returnsProteinShake()    { assertInstanceOf(ProteinShake.class,   MockData.chocolateShake()); }
    @Test void vanillaShake_returnsProteinShake()      { assertInstanceOf(ProteinShake.class,   MockData.vanillaShake()); }

    // ---------------------------------------------------------------
    // Factory freshness - each call returns a distinct instance
    // ---------------------------------------------------------------

    @Test
    void ingredientFactories_returnFreshInstancesOnEachCall() {
        assertNotSame(MockData.banana(), MockData.banana());
    }

    @Test
    void recipeFactories_returnFreshInstancesOnEachCall() {
        assertNotSame(MockData.chickenRiceBowl(), MockData.chickenRiceBowl());
    }

    // ---------------------------------------------------------------
    // weeklyPlan and pantryStock
    // ---------------------------------------------------------------

    @Test
    void weeklyPlan_isNotEmpty() {
        assertFalse(MockData.weeklyPlan().isEmpty());
    }

    @Test
    void weeklyPlan_containsSevenEntries() {
        assertEquals(7, MockData.weeklyPlan().size());
    }

    @Test
    void pantryStock_isNotEmpty() {
        assertFalse(MockData.pantryStock().isEmpty());
    }

    @Test
    void pantryStock_containsFifteenEntries() {
        assertEquals(15, MockData.pantryStock().size());
    }

    // ---------------------------------------------------------------
    // Private constructor - exercised via reflection for 100% coverage
    // ---------------------------------------------------------------

    @Test
    void privateConstructor_isAccessibleViaReflection() throws Exception {
        Constructor<MockData> ctor = MockData.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance()); // covers the unreachable private constructor
    }
}