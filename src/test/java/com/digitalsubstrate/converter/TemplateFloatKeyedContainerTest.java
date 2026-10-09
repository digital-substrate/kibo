package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TemplateStructure;
import com.digitalsubstrate.template.TemplateStructureField;
import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * A set whose element, or a map whose key, std::less would order as IEEE 754 does -- a float or a
 * double outside any structure -- is generated with Viper::StaticLess: with std::less a NaN makes
 * the tree undefined. A structure, a key or an integer keeps std::less.
 */
public final class TemplateFloatKeyedContainerTest {

    private static final NameSpace CORE =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-0000000000a0"), "Core");

    private static DSMTypeReference primitive(String name) {
        return new DSMTypeReference(new TypeName(NameSpace.GLOBAL, name), DSMTypeReferenceDomain.PRIMITIVE);
    }

    private static DSMStructureField field(String name, DSMType type) {
        return new DSMStructureField(name, type, new DSMLiteralValue(DSMLiteralDomain.NONE, ""), "");
    }

    @Test
    public void aFloatKeyTakesStaticLessAndNothingElseDoes() throws Exception {
        final var point = new DSMTypeReference(new TypeName(CORE, "Point"), DSMTypeReferenceDomain.STRUCTURE);
        final var definitions = new DSMDefinitions();
        definitions.structures.add(new DSMStructure(new TypeName(CORE, "Point"),
                new ArrayList<>(List.of(field("x", primitive(DSMLexicon.Double)))), "", UUID.randomUUID()));
        definitions.structures.add(new DSMStructure(new TypeName(CORE, "Holder"), new ArrayList<>(List.of(
                field("doubles", new DSMTypeSet(primitive(DSMLexicon.Double))),
                field("optionals", new DSMTypeSet(new DSMTypeOptional(primitive(DSMLexicon.Float)))),
                field("byVec", new DSMTypeMap(new DSMTypeVec(primitive(DSMLexicon.Float), 3), primitive(DSMLexicon.Int32))),
                field("byName", new DSMTypeMap(primitive(DSMLexicon.String), primitive(DSMLexicon.Double))),
                field("points", new DSMTypeSet(point)),
                field("ints", new DSMTypeSet(primitive(DSMLexicon.Int32))),
                field("listsOfDoubles", new DSMTypeSet(new DSMTypeVector(primitive(DSMLexicon.Double)))))),
                "", UUID.randomUUID()));

        final var model = new Converter("test", definitions, "Unit", Target.CPP).convert();
        final TemplateStructure holder = model.structures.stream()
                .filter(s -> s.getName().equals("Holder")).findFirst().orElseThrow();
        final var types = holder.getFields().stream().map(TemplateStructureField::getType).toList();

        assertEquals(List.of(
                "std::set<double, Viper::StaticLess>",
                "std::set<std::optional<float>, Viper::StaticLess>",
                "std::map<std::array<float, 3>, std::int32_t, Viper::StaticLess>",
                "std::map<std::string, double>",
                "std::set<core::Point>",
                "std::set<std::int32_t>",
                "std::set<std::vector<double>, Viper::StaticLess>"), types);

        final var byVec = holder.getFields().get(2).getField();
        assertEquals("std::set<std::array<float, 3>, Viper::StaticLess>", byVec.getKeySetType());
        assertEquals("std::set<std::string>", holder.getFields().get(3).getField().getKeySetType());
    }
}
