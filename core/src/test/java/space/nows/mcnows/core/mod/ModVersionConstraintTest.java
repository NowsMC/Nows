package space.nows.mcnows.core.mod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModVersionConstraintTest {
    @Test
    void matchesCommonConstraintForms() {
        assertTrue(ModVersionConstraint.matches("*", "1.2.3"));
        assertTrue(ModVersionConstraint.matches(">=1.2.0", "1.2.3"));
        assertTrue(ModVersionConstraint.matches("[1.0.0,2.0.0)", "1.2.3"));
        assertFalse(ModVersionConstraint.matches("<1.0.0", "1.2.3"));
        assertFalse(ModVersionConstraint.matches("[1.0.0,2.0.0)", "2.0.0"));
    }
}
