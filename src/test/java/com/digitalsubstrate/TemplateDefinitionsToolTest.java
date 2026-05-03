package com.digitalsubstrate;

import com.digitalsubstrate.template.TemplateTool;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public final class TemplateDefinitionsToolTest {

  @Test
  public void sc() {
    assertEquals("Simple", TemplateTool.sc("Simple"));
    assertEquals("Simple_Test", TemplateTool.sc("SimpleTest"));
    assertEquals("Simple_Test_ACRONYMS", TemplateTool.sc("SimpleTestACRONYMS"));
    assertEquals("Simple_Test_P3D", TemplateTool.sc("SimpleTestP3D"));
    assertEquals("Simple_Test_3D", TemplateTool.sc("SimpleTest3D"));
  }

  @Test
  public void usc() {
    assertEquals("SIMPLE", TemplateTool.usc("Simple"));
    assertEquals("SIMPLE_TEST", TemplateTool.usc("SimpleTest"));
    assertEquals("SIMPLE_TEST_ACRONYMS", TemplateTool.usc("SimpleTestACRONYMS"));
    assertEquals("SIMPLE_TEST_P3D", TemplateTool.usc("SimpleTestP3D"));
    assertEquals("SIMPLE_TEST_3D", TemplateTool.usc("SimpleTest3D"));
  }

  @Test
  public void lsc() {
    assertEquals("simple", TemplateTool.lsc("Simple"));
    assertEquals("simple_test", TemplateTool.lsc("SimpleTest"));
    assertEquals("simple_test_acronyms", TemplateTool.lsc("SimpleTestACRONYMS"));
    assertEquals("simple_test_p3d", TemplateTool.lsc("SimpleTestP3D"));
    assertEquals("simple_test_3d", TemplateTool.lsc("SimpleTest3D"));
  }
}
