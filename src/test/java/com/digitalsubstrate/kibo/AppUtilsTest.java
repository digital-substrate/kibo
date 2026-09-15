package com.digitalsubstrate.kibo;

import com.digitalsubstrate.converter.Target;
import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.DSMConcept;
import com.digitalsubstrate.viper.dsm.DSMDefinitions;

import org.junit.Test;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public final class AppUtilsTest {

    private static DSMDefinitions withNameSpace(String name) {
        final var definitions = new DSMDefinitions();
        final var nameSpace = new NameSpace(UUID.randomUUID(), name);
        definitions.concepts.add(new DSMConcept(new TypeName(nameSpace, "C"), null, "", UUID.randomUUID()));
        return definitions;
    }

    /**
     * A template may render both once per namespace and once for the model, and the two are
     * told apart only by the name they are prefixed with. Equal names mean one file, and the
     * second write replaces the first without a word.
     */
    @Test
    public void aNameSpaceCarryingTheModelsNameIsRefused() {
        try {
            AppUtils.generate(Target.of("cpp"), "", withNameSpace("Shadow"), "Shadow",
                              Path.of("."), Path.of("."), false);
            fail("expected the shadowing namespace to be refused");
        } catch (Exception e) {
            assertTrue(e.getMessage(), e.getMessage().contains("Shadow"));
            assertTrue(e.getMessage(), e.getMessage().contains("same files"));
        }
    }

    @Test
    public void aNameSpaceOfAnotherNameIsNotRefused() throws Exception {
        try {
            AppUtils.generate(Target.of("cpp"), "", withNameSpace("Unit"), "Model",
                              Path.of("no-such-template-directory"), Path.of("."), false);
        } catch (Exception e) {
            assertTrue("refusé pour la mauvaise raison : " + e.getMessage(),
                       !e.getMessage().contains("same files"));
        }
    }
}
