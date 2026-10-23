package com.digitalsubstrate.template;

import com.digitalsubstrate.viper.dsm.DSMAttachmentFunction;
import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

// The interface a generated attachment function takes is the runtime's class name: a template
// writes `interfaceName` for the type and keeps `interfaceType` for the parameter's name.
public final class TemplateAttachmentFunctionTest {

    private static TemplateAttachmentFunction function(boolean isMutable) {
        return new TemplateAttachmentFunction(new DSMAttachmentFunction(isMutable, null, ""),
                                              "", "", new ArrayList<>(), "ValueVoid", null);
    }

    @Test
    public void aMutatingFunctionTakesTheCommitInterface() {
        var f = function(true);
        assertEquals("CommitAttachmentMutating", f.getInterfaceName());
        assertEquals("Mutating", f.getInterfaceType());
        assertEquals(true, f.getIsMutable());
    }

    @Test
    public void aGettingFunctionTakesTheGettingInterface() {
        var f = function(false);
        assertEquals("AttachmentGetting", f.getInterfaceName());
        assertEquals("Getting", f.getInterfaceType());
        assertEquals(false, f.getIsMutable());
    }
}
