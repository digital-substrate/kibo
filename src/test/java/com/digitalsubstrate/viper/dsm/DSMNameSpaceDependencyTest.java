package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;

import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public final class DSMNameSpaceDependencyTest {

    private static final NameSpace NS_A =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-00000000000a"), "NA");
    private static final NameSpace NS_B =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-00000000000b"), "NB");

    private static DSMConcept concept(NameSpace nameSpace, String name) {
        return new DSMConcept(new TypeName(nameSpace, name), null, "", UUID.randomUUID());
    }

    private static DSMStructure structure(NameSpace nameSpace, String name) {
        final var fields = new ArrayList<DSMStructureField>();
        fields.add(new DSMStructureField("x", DSMTypeReference.Int64, null, ""));
        return new DSMStructure(new TypeName(nameSpace, name), fields, "", UUID.randomUUID());
    }

    private static DSMNameSpaceDependency collected(DSMDefinitions definitions) {
        final var dependency = new DSMNameSpaceDependency();
        dependency.collect(new DSMDefinitionsInspector(definitions));
        return dependency;
    }

    @Test
    public void attachmentDocumentTypeIsANameSpaceDependency() {
        final var definitions = new DSMDefinitions();
        final var document = structure(NS_A, "SD");
        definitions.structures.add(document);
        definitions.concepts.add(concept(NS_A, "CA"));

        final var conceptB = concept(NS_B, "CB");
        definitions.concepts.add(conceptB);
        definitions.attachments.add(new DSMAttachment(new TypeName(NS_B, "att"),
                                                      conceptB.typeReference,
                                                      document.typeReference,
                                                      "",
                                                      UUID.randomUUID()));

        final var dependency = collected(definitions);
        assertTrue(dependency.dependencies(NS_B).contains(NS_A));
        assertTrue(dependency.dependencies(NS_A).isEmpty());

        final var sorted = dependency.sorted();
        assertTrue(sorted.indexOf(NS_A) < sorted.indexOf(NS_B));
    }

    @Test
    public void attachmentKeyTypeIsANameSpaceDependency() {
        final var definitions = new DSMDefinitions();
        final var conceptA = concept(NS_A, "CA");
        definitions.concepts.add(conceptA);
        definitions.structures.add(structure(NS_B, "SB"));

        definitions.attachments.add(new DSMAttachment(new TypeName(NS_B, "att"),
                                                      conceptA.typeReference,
                                                      DSMTypeReference.String,
                                                      "",
                                                      UUID.randomUUID()));

        final var dependency = collected(definitions);
        assertTrue(dependency.dependencies(NS_B).contains(NS_A));

        final var sorted = dependency.sorted();
        assertTrue(sorted.indexOf(NS_A) < sorted.indexOf(NS_B));
    }

    @Test
    public void attachmentKeyInsideTheDocumentTypeIsANameSpaceDependency() {
        final var definitions = new DSMDefinitions();
        final var conceptA = concept(NS_A, "CA");
        definitions.concepts.add(conceptA);

        final var conceptB = concept(NS_B, "CB");
        definitions.concepts.add(conceptB);
        definitions.attachments.add(new DSMAttachment(new TypeName(NS_B, "att"),
                                                      conceptB.typeReference,
                                                      new DSMTypeKey(conceptA.typeReference),
                                                      "",
                                                      UUID.randomUUID()));

        final var dependency = collected(definitions);
        assertTrue(dependency.dependencies(NS_B).contains(NS_A));
    }

    @Test
    public void anAttachmentOnItsOwnNameSpaceAddsNoDependency() {
        final var definitions = new DSMDefinitions();
        final var conceptA = concept(NS_A, "CA");
        definitions.concepts.add(conceptA);
        definitions.attachments.add(new DSMAttachment(new TypeName(NS_A, "att"),
                                                      conceptA.typeReference,
                                                      DSMTypeReference.String,
                                                      "",
                                                      UUID.randomUUID()));

        final var dependency = collected(definitions);
        assertEquals(0, dependency.dependencies(NS_A).size());
    }

    @Test
    public void aNameSpaceHoldingOnlyAnAttachmentIsKnown() {
        final var definitions = new DSMDefinitions();
        final var conceptA = concept(NS_A, "CA");
        definitions.concepts.add(conceptA);
        definitions.attachments.add(new DSMAttachment(new TypeName(NS_B, "att"),
                                                      conceptA.typeReference,
                                                      DSMTypeReference.String,
                                                      "",
                                                      UUID.randomUUID()));

        final var inspector = new DSMDefinitionsInspector(definitions);
        assertTrue(inspector.getNameSpaces().contains(NS_B));

        final var dependency = new DSMNameSpaceDependency();
        dependency.collect(inspector);
        assertTrue(dependency.dependencies(NS_B).contains(NS_A));

        final var sorted = dependency.sorted();
        assertTrue(sorted.contains(NS_B));
        assertTrue(sorted.indexOf(NS_A) < sorted.indexOf(NS_B));
    }

    @Test
    public void anAnyConceptKeyNamesNoNameSpace() {
        final var definitions = new DSMDefinitions();
        final var conceptA = concept(NS_A, "CA");
        definitions.concepts.add(conceptA);
        definitions.attachments.add(new DSMAttachment(new TypeName(NS_A, "att"),
                                                      conceptA.typeReference,
                                                      new DSMTypeKey(DSMTypeReference.AnyConcept),
                                                      "",
                                                      UUID.randomUUID()));

        final var dependency = collected(definitions);
        assertEquals(0, dependency.dependencies(NS_A).size());
    }
}
