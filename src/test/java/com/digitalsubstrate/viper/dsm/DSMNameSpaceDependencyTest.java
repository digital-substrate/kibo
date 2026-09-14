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

    private static DSMFunctionPool pool(String name, DSMType returnType, DSMType... parameters) {
        final var prototype = new ArrayList<DSMFunctionPrototypeParameter>();
        var i = 0;
        for (var type : parameters)
            prototype.add(new DSMFunctionPrototypeParameter("p" + i++, type));

        final var functions = new ArrayList<DSMFunction>();
        functions.add(new DSMFunction(new DSMFunctionPrototype("f", prototype, returnType), ""));
        return new DSMFunctionPool(UUID.randomUUID(), name, functions, "");
    }

    @Test
    public void aPoolReachesTheNameSpacesOfItsParameters() {
        final var definitions = new DSMDefinitions();
        final var conceptA = concept(NS_A, "CA");
        final var conceptB = concept(NS_B, "CB");
        definitions.concepts.add(conceptA);
        definitions.concepts.add(conceptB);

        final var spanning = pool("Projector", DSMTypeReference.Void,
                                  new DSMTypeKey(conceptA.typeReference),
                                  new DSMTypeKey(conceptB.typeReference));
        definitions.functionPools.add(spanning);

        final var reached = collected(definitions).dependencies(spanning);
        assertEquals(2, reached.size());
        assertTrue(reached.contains(NS_A));
        assertTrue(reached.contains(NS_B));
    }

    @Test
    public void aPoolReachesTheNameSpaceOfItsReturnType() {
        final var definitions = new DSMDefinitions();
        final var structureA = structure(NS_A, "SA");
        definitions.structures.add(structureA);

        final var returning = pool("Maker", structureA.typeReference);
        definitions.functionPools.add(returning);

        assertTrue(collected(definitions).dependencies(returning).contains(NS_A));
    }

    /**
     * A pool belongs to no namespace, so there is nothing to subtract: the exclusion that
     * keeps a namespace out of its own dependency set has no counterpart here, and a pool
     * naming only primitives depends on nothing at all.
     */
    @Test
    public void aPoolNamingNoNameSpacedTypeDependsOnNothing() {
        final var definitions = new DSMDefinitions();
        definitions.concepts.add(concept(NS_A, "CA"));

        final var plain = pool("Tools", DSMTypeReference.Int64, DSMTypeReference.Int64);
        definitions.functionPools.add(plain);

        assertEquals(0, collected(definitions).dependencies(plain).size());
    }

    @Test
    public void anAttachmentPoolReachesTheNameSpaceOfItsKey() {
        final var definitions = new DSMDefinitions();
        final var conceptA = concept(NS_A, "CA");
        definitions.concepts.add(conceptA);

        final var functions = new ArrayList<DSMAttachmentFunction>();
        final var parameters = new ArrayList<DSMFunctionPrototypeParameter>();
        parameters.add(new DSMFunctionPrototypeParameter("k", new DSMTypeKey(conceptA.typeReference)));
        functions.add(new DSMAttachmentFunction(
                true, new DSMFunctionPrototype("clear", parameters, DSMTypeReference.Void), ""));

        final var linkModel = new DSMAttachmentFunctionPool(UUID.randomUUID(), "LinkModel", functions, "");
        definitions.attachmentFunctionPools.add(linkModel);

        assertTrue(collected(definitions).dependencies(linkModel).contains(NS_A));
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
