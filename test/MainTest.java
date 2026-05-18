import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;

/**
 * Smoke test for Main - verifies the entry point runs end-to-end
 * without throwing any exception, covering the full wiring of
 * MockData -> GroceryEngine -> PantryTracker in one pass.
 */
class MainTest {

    @Test
    void main_runsEndToEndWithoutException() {
        // Suppress stdout so test output stays clean
        PrintStream original = System.out;
        System.setOut(new PrintStream(new ByteArrayOutputStream()));
        try {
            assertDoesNotThrow(() -> Main.main(new String[]{}));
        } finally {
            System.setOut(original);
        }
    }
}