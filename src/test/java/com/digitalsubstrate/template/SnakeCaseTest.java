package com.digitalsubstrate.template;

import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

// The hard names of every DSM model at hand, with the spelling the rule gives them. A change to
// the rule that moves one of them moves a generated name: it is a decision, not a refactoring.
public final class SnakeCaseTest {

    private static final String[][] CORPUS = {
        // the DSM's own vocabulary, built in as atoms
        {"docUInt8", "doc_uint8"}, {"docUInt64", "doc_uint64"}, {"docInt8", "doc_int8"},
        {"docInt64", "doc_int64"}, {"docUUId", "doc_uuid"}, {"docXArray", "doc_xarray"},
        {"propertiesXArray", "properties_xarray"}, {"propertiesInt8", "properties_int8"},
        {"propertiesMapInt8String", "properties_map_int8_string"}, {"propertiesSeInt8", "properties_se_int8"},
        // a short number stays with its word, a long one stands alone, a unit is a word
        {"vec3Curves", "vec3_curves"}, {"cm2Factor", "cm2_factor"}, {"grainGeneratorMd4", "grain_generator_md4"},
        {"surfacesAxfCpa2", "surfaces_axf_cpa2"}, {"Excel2", "excel2"}, {"hdrF32", "hdr_f32"},
        {"ntscD1", "ntsc_d1"}, {"mapSize1024", "map_size_1024"}, {"propertiesExt2024", "properties_ext_2024"},
        {"Gray0625", "gray_0625"}, {"Gray125", "gray_125"},
        {"render2DAttributes", "render_2d_attributes"}, {"get3DImage", "get_3d_image"},
        {"Patchwork3D", "patchwork_3d"}, {"pixels2D", "pixels_2d"},
        // acronyms, wherever they stand
        {"P3DBridge", "p3d_bridge"}, {"HTTPServer", "http_server"}, {"parseHTTPResponse", "parse_http_response"},
        {"blobInputRGBProfile", "blob_input_rgb_profile"}, {"sliceLUT", "slice_lut"},
        {"hierarchicalLOD", "hierarchical_lod"}, {"useIESBrightness", "use_ies_brightness"},
        {"UVMapping", "uv_mapping"}, {"userIDs", "user_ids"}, {"MongoDBApiTest", "mongo_db_api_test"},
        // an underscore the author wrote is kept, each segment converted on its own
        {"f_E", "f_e"}, {"f_Klub", "f_klub"}, {"a_C1", "a_c1"}, {"f_o_Cl", "f_o_cl"},
        {"commentBubbleVisible_InDetailsPanel", "comment_bubble_visible_in_details_panel"},
        // no capital: kept as written
        {"f_uint8", "f_uint8"}, {"channel0", "channel0"}, {"rgba16f", "rgba16f"}, {"f_mat2x2", "f_mat2x2"},
        {"stereo3d", "stereo3d"}, {"", ""},
    };

    @Test
    public void theCorpusHardNames() {
        final var snake = SnakeCase.standard();
        for (var vector : CORPUS)
            assertEquals(vector[0], vector[1], snake.of(vector[0]));
    }

    @Test
    public void whatOnlyTheAuthorCanSplitIsAnAtom() {
        final var standard = SnakeCase.standard();
        assertEquals("i_pv4_address", standard.of("IPv4Address"));
        assertEquals("compression_y_co_cg", standard.of("compressionYCoCg"));

        final var project = SnakeCase.of(List.of("IPv4", "YCoCg", "openGL"), Map.of());
        assertEquals("ipv4_address", project.of("IPv4Address"));
        assertEquals("compression_ycocg", project.of("compressionYCoCg"));
        assertEquals("opengl", project.of("openGL"));
        assertEquals("doc_uint8", project.of("docUInt8"));
    }

    @Test
    public void aRenameWinsOverTheRule() {
        final var project = SnakeCase.of(List.of(), Map.of("vec3Curves", "curves_vec3"));
        assertEquals("curves_vec3", project.of("vec3Curves"));
        assertEquals("vec3_points", project.of("vec3Points"));
    }

    @Test
    public void aWordPythonReservesTakesAnUnderscore() {
        final var snake = SnakeCase.standard();
        assertEquals("annotations_", snake.of("Annotations"));
        assertEquals("from_", snake.of("from"));
        assertEquals("class_", snake.of("Class"));
        assertEquals("ANNOTATIONS", snake.upper("Annotations"));
        assertEquals("AND", snake.upper("and"));
        assertEquals("annotation", snake.of("Annotation"));
    }

    @Test
    public void upperIsTheSameProjection() {
        final var snake = SnakeCase.standard();
        assertEquals("P3D_BRIDGE", snake.upper("P3DBridge"));
        assertEquals("DOC_UINT8", snake.upper("docUInt8"));
        assertEquals("A", snake.upper("a"));
    }

    // A type whose name is already upper snake case meets a constant spelled with usnake.
    @Test
    public void aNameAlreadyUpperSnakeIsTold() {
        TemplateTool.setNaming(SnakeCase.standard());
        for (var name : List.of("RGB", "E", "S", "R_G_B", "HTTP2"))
            assertEquals(name, true, TemplateTool.isUpperSnake(name));
        for (var name : List.of("Rgb", "Colour", "rgb", "IPv4", "RGBColor", "Http2"))
            assertEquals(name, false, TemplateTool.isUpperSnake(name));
    }
}
