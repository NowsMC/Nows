/*
 * Copyright 2026 TamKungZ_ (Nows MC — https://nows.space)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package space.nows.platform.core.mod;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void rejectsEmptyMalformedConstraints() {
        assertFalse(ModVersionConstraint.matches(",", "1.2.3"));
        assertFalse(ModVersionConstraint.matches(">=", "1.2.3"));
        assertFalse(ModVersionConstraint.matches("[,]", "1.2.3"));
    }

    @Test
    void comparesSemanticVersionsWithPrereleaseAndMetadata() {
        assertTrue(ModVersionConstraint.matches(">=1.4.2-beta.1", "1.4.2-beta.2+build.20240807"));
        assertTrue(ModVersionConstraint.matches(">=1.4.2", "1.4.2+a1b2c3d"));
        assertTrue(ModVersionConstraint.matches("==1.4.2+build.1", "1.4.2+build.2"));
        assertFalse(ModVersionConstraint.matches(">=1.4.2", "1.4.2-rc.1"));
    }

    @Test
    void comparesNumericVersionFamilies() {
        assertTrue(ModVersionConstraint.matches(">=10.0.19045.4000", "10.0.19045.4123"));
        assertTrue(ModVersionConstraint.matches(">=1.8.0_400", "1.8.0_401"));
        assertTrue(ModVersionConstraint.matches("[2024.08,2024.09)", "2024.08.07"));
        assertTrue(ModVersionConstraint.matches(">=24.04", "24.04.1"));
    }

    @Test
    void handlesSerialHashAndCodenameVersions() {
        assertTrue(ModVersionConstraint.matches(">=v2", "v99"));
        assertTrue(ModVersionConstraint.matches("==a1b2c3d", "a1b2c3d"));
        assertFalse(ModVersionConstraint.matches("==a1b2c3d", "a1b2c3e"));
        assertTrue(ModVersionConstraint.matches("==Sonoma", "sonoma"));
        assertTrue(ModVersionConstraint.matches(">=Jammy Jellyfish", "Noble Numbat"));
    }

    @Test
    void handlesDevelopmentAndBuildLabelsDeterministically() {
        assertTrue(ModVersionConstraint.matches(">=dev.1", "dev.2"));
        assertTrue(ModVersionConstraint.matches(">=build.15", "build.20240807"));
        assertTrue(ModVersionConstraint.matches(">=1.0.0-dev.1", "1.0.0-beta.1"));
        assertTrue(ModVersionConstraint.matches(">=1.0.0-rc.1", "1.0.0"));
        assertFalse(ModVersionConstraint.matches(">=1.0.0", "1.0.0-build.1"));
    }

    @Test
    void supportsLooseEqualityWildcards() {
        assertTrue(ModVersionConstraint.matches("1.2.x", "1.2.3"));
        assertTrue(ModVersionConstraint.matches("==1.2.*", "1.2.3+local.sha"));
        assertFalse(ModVersionConstraint.matches("1.2.x", "1.3.0"));
    }

    @Test
    void ignoresBuildMetadataAndQuotedCodenamesForOrdering() {
        assertEquals(0, ModVersionConstraint.compare("1.0.0+build.1", "1.0.0+build.2"));
        assertEquals(0, ModVersionConstraint.compare("22.04 \"Jammy Jellyfish\"", "22.04"));
        assertEquals(0, ModVersionConstraint.compare("1.0.0-final", "1.0.0"));
        assertTrue(ModVersionConstraint.compare("1.0.0", "1.0.0-beta.1") > 0);
    }
}
