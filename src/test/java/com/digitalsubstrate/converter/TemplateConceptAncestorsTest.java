package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TemplateConcept;
import com.digitalsubstrate.template.TemplateConceptInNamespace;
import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import org.junit.Test;

import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * A key widens to every ancestor, not only to its parent: the model lists them, the parent
 * first, each named from the descendant's namespace -- qualified when it lives in another.
 */
public final class TemplateConceptAncestorsTest {

    private static final NameSpace CORE =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-0000000000a0"), "Core");
    private static final NameSpace WOVEN =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-0000000000b0"), "Woven");

    private static DSMConcept concept(NameSpace nameSpace, String name, NameSpace parentNameSpace, String parent) {
        final var reference = parent == null ? null
                : new DSMTypeReference(new TypeName(parentNameSpace, parent), DSMTypeReferenceDomain.CONCEPT);
        return new DSMConcept(new TypeName(nameSpace, name), reference, "", UUID.randomUUID());
    }

    @Test
    public void everyAncestorIsListedParentFirstAndNamedFromTheDescendant() throws Exception {
        final var definitions = new DSMDefinitions();
        definitions.concepts.add(concept(CORE, "Thing", null, null));
        definitions.concepts.add(concept(WOVEN, "Derived", CORE, "Thing"));
        definitions.concepts.add(concept(WOVEN, "Twisted", WOVEN, "Derived"));

        final var model = new Converter("test", definitions, "Unit", Target.CPP).convert();
        final TemplateConcept twisted = model.concepts.stream()
                .filter(c -> c.getName().equals("Twisted")).findFirst().orElseThrow();
        assertEquals("Derived, core::Thing", twisted.getStrictAncestorsInNamespace().stream()
                .map(TemplateConceptInNamespace::getNameInNamespace).collect(Collectors.joining(", ")));
    }
}
