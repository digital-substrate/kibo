package com.digitalsubstrate.converter;

import com.digitalsubstrate.template.TemplateDefinitions;
import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;
import com.digitalsubstrate.viper.dsm.*;

import org.junit.Test;

import java.util.ArrayList;
import java.util.UUID;

import static org.junit.Assert.assertEquals;

/**
 * A type a unit declares, read from the model rather than from a unit: its {@code qualified}
 * spelling names its module, as for a field that names the type, so that a template rendered
 * once at the package root writes a name that resolves there. Inside the unit it stays bare.
 */
public final class TemplateNamedBindingTypeTest {

    private static final NameSpace MODEL =
            new NameSpace(UUID.fromString("00000000-0000-0000-0000-0000000000a0"), "ModelA");

    private static TemplateDefinitions convert(Target target) throws Exception {
        final var definitions = new DSMDefinitions();
        definitions.concepts.add(new DSMConcept(new TypeName(MODEL, "Material"), null, "", UUID.randomUUID()));
        final var fields = new ArrayList<DSMStructureField>();
        fields.add(new DSMStructureField("f", new DSMTypeReference(new TypeName(NameSpace.GLOBAL, "int32"),
                                                                   DSMTypeReferenceDomain.PRIMITIVE), null, ""));
        definitions.structures.add(new DSMStructure(new TypeName(MODEL, "Shade"), fields, "", UUID.randomUUID()));
        return new Converter("test", definitions, "Unit", target).convert();
    }

    @Test
    public void aDeclaredTypeIsQualifiedByItsModuleFromOutsideEveryUnit() throws Exception {
        final var python = convert(Target.PYTHON);
        assertEquals("model_a.MaterialKey", python.concepts.get(0).getBindingType().getQualified());
        assertEquals("model_a.Shade", python.structures.get(0).getBindingType().getQualified());

        final var typescript = convert(Target.TYPESCRIPT);
        assertEquals("model_a.MaterialKey", typescript.concepts.get(0).getBindingType().getQualified());
    }

    @Test
    public void insideItsUnitTheTypeStaysBare() throws Exception {
        final var python = convert(Target.PYTHON);
        assertEquals("MaterialKey", python.concepts.get(0).getBindingType().getAnnotation());
        assertEquals("MaterialKey", python.concepts.get(0).getBindingType().getTypeInNamespace());
        assertEquals("ModelA_Material", python.concepts.get(0).getBindingType().getProxy());
    }

    @Test
    public void aNativeBindingHasNoBindingSpaceToQualifyIn() throws Exception {
        final var cpp = convert(Target.CPP);
        assertEquals("MaterialKey", cpp.concepts.get(0).getBindingType().getQualified());
    }
}
