package net.n2oapp.platform.jaxrs;

import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RestMessageTest {

    @Test
    public void testEqualsHashCode() {
        RestMessage.Error error1 = new RestMessage.Error("123");
        RestMessage.ConstraintViolationError error2 = new RestMessage.ConstraintViolationError();
        error2.setMessage("123");
        assertNotEquals(error2, error1);
        assertNotEquals(error1, error2);
        Set<Object> set = new HashSet<>();
        for (int i = 0; i < 10000; i++)
            set.add(i);
        set.add(error1);
        set.add(error2);
        assertTrue(set.contains(error1));
        assertTrue(set.contains(error2));
    }

}