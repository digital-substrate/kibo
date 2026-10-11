package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TargetNames;
import com.digitalsubstrate.template.TemplateBindingType;
import com.digitalsubstrate.template.TemplateDefinitions;
import com.digitalsubstrate.template.TemplateStructureField;
import com.digitalsubstrate.template.TemplateTool;
import com.digitalsubstrate.template.TemplateTupleFunction;
import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * A type's name is its class in a delegating binding and, after a "_", its type suffix. A tuple and
 * a variant say how many members follow, so a nested list reads one way. A DSM name holding the
 * separators can still make two types alike: kibo stops, naming both and the directive that names
 * one of them.
 */
public final class TemplateTypeNameTest {

    private static final NameSpace APP =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-0000000000b0"), "App");
    private static final NameSpace N =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-0000000000b1"), "N");

    @After
    public void noDirective() {
        TemplateTool.setTargetNames(TargetNames.none());
    }

    private static DSMTypeReference primitive(String name) {
        return new DSMTypeReference(new TypeName(NameSpace.GLOBAL, name), DSMTypeReferenceDomain.PRIMITIVE);
    }

    private static DSMTypeReference structure(NameSpace nameSpace, String name) {
        return new DSMTypeReference(new TypeName(nameSpace, name), DSMTypeReferenceDomain.STRUCTURE);
    }

    private static DSMTypeTuple tuple(DSMType... types) {
        return new DSMTypeTuple(new ArrayList<>(List.of(types)));
    }

    private static DSMStructureField field(String name, DSMType type) {
        return new DSMStructureField(name, type, new DSMLiteralValue(DSMLiteralDomain.NONE, ""), "");
    }

    private static DSMStructure struct(NameSpace nameSpace, String name, DSMStructureField... fields) {
        return new DSMStructure(new TypeName(nameSpace, name), new ArrayList<>(List.of(fields)), "", UUID.randomUUID());
    }

    /** One structure App::S holding the two tuples a nested list made alike. */
    private static DSMDefinitions nestedTuples() {
        final var int64 = primitive(DSMLexicon.Int64);
        final var float_ = primitive(DSMLexicon.Float);
        final var definitions = new DSMDefinitions();
        definitions.structures.add(struct(APP, "S",
                field("a", tuple(tuple(int64, float_), float_)),
                field("b", tuple(tuple(int64, float_, float_)))));
        return definitions;
    }

    /** Structures of N whose names hold the separator, so that two tuples of two members are written alike. */
    private static DSMDefinitions separatorInNames() {
        final var x = field("x", primitive(DSMLexicon.Int64));
        final var definitions = new DSMDefinitions();
        for (var name : List.of("A", "B", "C", "A_and_N_B", "B_and_N_C"))
            definitions.structures.add(struct(N, name, x));
        definitions.structures.add(struct(N, "S",
                field("p", tuple(structure(N, "A_and_N_B"), structure(N, "C"))),
                field("q", tuple(structure(N, "A"), structure(N, "B_and_N_C")))));
        return definitions;
    }

    private static TemplateDefinitions convert(DSMDefinitions definitions, Target target) throws Exception {
        return new Converter("test", definitions, "Model", target).convert();
    }

    private static List<TemplateBindingType> fieldsOf(TemplateDefinitions model, String structure) {
        return model.structures.stream().filter(s -> s.getName().equals(structure)).findFirst().orElseThrow()
                .getFields().stream().map(TemplateStructureField::getBindingType).toList();
    }

    @Test
    public void aNestedListReadsOneWay() throws Exception {
        final var model = convert(nestedTuples(), Target.PYTHON);
        final var names = model.tupleFunctions.stream().map(f -> f.getBindingType().getProxy()).sorted().toList();
        assertEquals(List.of(
                "Tuple1_of_Tuple3_of_int64_and_float_and_float",
                "Tuple2_of_Tuple2_of_int64_and_float_and_float",
                "Tuple2_of_int64_and_float",
                "Tuple3_of_int64_and_float_and_float"), names);
    }

    @Test
    public void theSuffixIsTheNameAfterAnUnderscore() throws Exception {
        final var definitions = new DSMDefinitions();
        final var user = new DSMConcept(new TypeName(APP, "User"), null, "", UUID.randomUUID());
        definitions.concepts.add(user);
        definitions.structures.add(struct(APP, "Point", field("x", primitive(DSMLexicon.Double))));
        definitions.structures.add(struct(APP, "S",
                field("number", primitive(DSMLexicon.Int64)),
                field("anything", DSMTypeReference.Any),
                field("point", structure(APP, "Point")),
                field("user", new DSMTypeKey(user.typeReference)),
                field("someone", new DSMTypeKey(DSMTypeReference.AnyConcept)),
                field("index", new DSMTypeMap(primitive(DSMLexicon.Int64), new DSMTypeVector(primitive(DSMLexicon.String)))),
                field("pair", tuple(primitive(DSMLexicon.Int64), primitive(DSMLexicon.Float))),
                field("vec", new DSMTypeVec(primitive(DSMLexicon.Float), 3))));
        for (var target : Target.values()) {
            TemplateTool.setTargetNames(TargetNames.none());
            final var model = convert(definitions, target);
            final var suffixes = fieldsOf(model, "S").stream().map(TemplateBindingType::getTypeSuffix).toList();
            assertEquals(target.identifier, List.of(
                    "_int64", "_Any", "_App_Point", "_App_UserKey", "_AnyConceptKey",
                    "_Map_of_int64_to_Vector_of_string", "_Tuple2_of_int64_and_float", "_Vec3_of_float"), suffixes);
            if (target != Target.CPP)
                for (var binding : fieldsOf(model, "S"))
                    assertEquals("_" + binding.getProxy(), binding.getTypeSuffix());
        }
        final var tuples = convert(definitions, Target.CPP).tupleFunctions.stream()
                .map(TemplateTupleFunction::getTypeSuffix).toList();
        assertEquals(List.of("_Tuple2_of_int64_and_float"), tuples);
    }

    @Test
    public void twoTypesWrittenAlikeStopWithTheDirective() throws Exception {
        for (var target : Target.values()) {
            try {
                convert(separatorInNames(), target);
                fail(target.identifier + ": expected the conversion to stop");
            } catch (TypeNamesException e) {
                assertEquals(target.identifier, String.join("\n",
                        "2 types are written Tuple2_of_N_A_and_N_B_and_N_C:",
                        "  tuple<N::A, N::B_and_N_C>",
                        "  tuple<N::A_and_N_B, N::C>",
                        "Name all but one of them, in kibo.toml:",
                        "  [names.types]",
                        "  \"tuple<N::A_and_N_B, N::C>\" = \"Tuple2_of_N_A_and_N_B_and_N_C_2\"",
                        "or kibo's --type-name \"tuple<N::A_and_N_B, N::C>=Tuple2_of_N_A_and_N_B_and_N_C_2\""),
                        e.getMessage());
            }
        }
    }

    @Test
    public void theDirectiveNamesTheTypeAndItsSuffix() throws Exception {
        TemplateTool.setTargetNames(TargetNames.of(Map.of(), Map.of(),
                Map.of("tuple<N::A_and_N_B,N::C>", "Pair_of_AB_and_C")));
        final var fields = fieldsOf(convert(separatorInNames(), Target.PYTHON), "S");
        assertEquals(List.of("Pair_of_AB_and_C", "Tuple2_of_N_A_and_N_B_and_N_C"),
                     fields.stream().map(TemplateBindingType::getProxy).toList());
        assertEquals(List.of("_Pair_of_AB_and_C", "_Tuple2_of_N_A_and_N_B_and_N_C"),
                     fields.stream().map(TemplateBindingType::getTypeSuffix).toList());
    }

    @Test
    public void aDirectiveForATypeTheModelLacksStops() throws Exception {
        TemplateTool.setTargetNames(TargetNames.of(Map.of(), Map.of(), Map.of("tuple<int8>", "Lone")));
        try {
            convert(nestedTuples(), Target.PYTHON);
            fail("expected the conversion to stop");
        } catch (TypeNamesException e) {
            assertTrue(e.getMessage(), e.getMessage().startsWith("--type-name tuple<int8>: the model has no such type."));
        }
    }
}
