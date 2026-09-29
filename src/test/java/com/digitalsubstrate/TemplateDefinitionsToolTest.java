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

  // The snake case of a model name is a naming specification shared with the Viper runtime:
  // the names it produces cross the dynamic space -- the attachment pool's function names, the
  // constants Definitions.inject() gives Python and Node -- and each end computes them on its
  // own. These vectors are the runtime's own (cpp-test-harness/Viper_StringHelper_test.cpp in
  // viper), copied as they stand: a change here is a change of names on the wire.
  private static final String[][] SNAKE_CASE_VECTORS = {
      {"SimpleTest", "simple_test"},
      {"SimpleTestACRONYMS", "simple_test_acronyms"},
      {"SimpleTestP3D", "simple_test_p3d"},
      {"SimpleTest3D", "simple_test_3d"},
      {"isEven", "is_even"},
      {"getOrDefault", "get_or_default"},
      {"appendGraphComment", "append_graph_comment"},
      {"ModelA", "model_a"},
      {"LinkModel", "link_model"},
      {"camelCase", "camel_case"},
      {"HTTPServer", "http_server"},
      {"parseHTTPResponse", "parse_http_response"},
      {"myURLParser", "my_url_parser"},
      {"userID", "user_id"},
      {"userIDs", "user_ids"},
      {"ABC", "abc"},
      {"vec3Curves", "vec_3_curves"},
      {"render2DAttributes", "render_2d_attributes"},
      {"Vertex2DAttributes", "vertex_2d_attributes"},
      {"get2DPoint", "get_2d_point"},
      {"utf8String", "utf_8_string"},
      {"propertiesExt2024", "properties_ext_2024"},
      {"Patchwork3D", "patchwork_3d"},
      {"P3DBridge", "p3d_bridge"},
      {"Compat12", "compat_12"},
      {"channel0", "channel_0"},
      {"f00", "f_00"},
      {"x2Y", "x_2y"},
      {"assign_material", "assign_material"},
      {"ss_to_si", "ss_to_si"},
      {"a_A", "a_a"},
      {"blob_lightmapUvs", "blob_lightmap_uvs"},
      {"bl_idname", "bl_idname"},
      {"commentBubbleVisible_InDetailsPanel", "comment_bubble_visible_in_details_panel"},
      {"IPv4Address", "i_pv_4_address"},
      {"", ""},
      {"a", "a"},
      {"A", "a"},
  };

  @Test
  public void lscFollowsTheSharedNamingVectors() {
    for (final var vector : SNAKE_CASE_VECTORS)
      assertEquals(vector[0], vector[1], TemplateTool.lsc(vector[0]));
  }
}
