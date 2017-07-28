package java.testing;

/**
 * Created by humblehound on 7/7/17.
 */
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestJunit {

    String message = "~~Hello World";

    @Test
    public void testPrintMessage() {
        System.out.println(message);
        assertEquals(1, 1);
    }

    @Test
    public void testPrintMessage2() {
        System.out.println(message);
        assertEquals(2, 1);
    }
}
