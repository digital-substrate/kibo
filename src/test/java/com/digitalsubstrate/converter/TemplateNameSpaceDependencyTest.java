package com.digitalsubstrate.converter;

import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * The converter has always computed which namespaces a namespace reaches, and has
 * always thrown that away after sorting on it. These check it now arrives on the
 * Template Model, because the generated output cannot show it: no template reads it
 * yet, so a render proves nothing either way.
 */
public final class TemplateNameSpaceDependencyTest {

    private static final NameSpace A =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-0000000000a0"), "A");
    private static final NameSpace B =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-0000000000b0"), "B");
    private static final NameSpace C =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-0000000000c0"), "C");

    private static DSMConcept concept(NameSpace nameSpace, String name) {
        return new DSMConcept(new TypeName(nameSpace, name), null, "", UUID.randomUUID());
    }

    /** A structure in {@code home} whose one field references {@code target}'s concept. */
    private static DSMStructure referencing(NameSpace home, String name, NameSpace target, String concept) {
        final var fields = new ArrayList<DSMStructureField>();
        fields.add(new DSMStructureField("f",
                new DSMTypeReference(new TypeName(target, concept), DSMTypeReferenceDomain.CONCEPT),
                null, ""));
        return new DSMStructure(new TypeName(home, name), fields, "", UUID.randomUUID());
    }

    private static TemplateNameSpaceView convert(DSMDefinitions definitions) throws Exception {
        return new TemplateNameSpaceView(
                new Converter("test", definitions, "Unit", Target.CPP).convert());
    }

    @Test
    public void aNameSpaceCarriesWhatItReaches() throws Exception {
        final var definitions = new DSMDefinitions();
        definitions.concepts.add(concept(A, "CA"));
        definitions.concepts.add(concept(B, "CB"));
        definitions.structures.add(referencing(B, "SB", A, "CA"));

        final var view = convert(definitions);
        assertEquals("[]", view.dependenciesOf("A"));
        assertEquals("[A]", view.dependenciesOf("B"));
    }

    @Test
    public void aDependencyIsBuiltBeforeTheNameSpaceThatNeedsIt() throws Exception {
        final var definitions = new DSMDefinitions();
        definitions.concepts.add(concept(A, "CA"));
        definitions.concepts.add(concept(B, "CB"));
        definitions.concepts.add(concept(C, "CC"));
        definitions.structures.add(referencing(C, "SC", B, "CB"));
        definitions.structures.add(referencing(B, "SB", A, "CA"));

        final var view = convert(definitions);
        assertEquals("[A]", view.dependenciesOf("B"));
        assertEquals("[B]", view.dependenciesOf("C"));
        assertTrue("a dependency must precede its dependent",
                   view.positionOf("A") < view.positionOf("B"));
        assertTrue(view.positionOf("B") < view.positionOf("C"));
    }

    @Test
    public void aNameSpaceThatReachesNothingCarriesNothing() throws Exception {
        final var definitions = new DSMDefinitions();
        definitions.concepts.add(concept(A, "CA"));
        definitions.concepts.add(concept(B, "CB"));

        final var view = convert(definitions);
        assertEquals("[]", view.dependenciesOf("A"));
        assertEquals("[]", view.dependenciesOf("B"));
    }
}
