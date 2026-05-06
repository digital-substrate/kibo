package com.digitalsubstrate.viper.dsm;

// Mirror of viper's Viper_JsonLexicon.cpp. Keys must stay byte-identical
// to that file — diverging would silently break interop.
public final class DSMDefinitionsJsonLexicon {

    private DSMDefinitionsJsonLexicon() {}

    public static final String Concepts                 = "concepts";
    public static final String Clubs                    = "clubs";
    public static final String Enumerations             = "enumerations";
    public static final String Structures               = "structures";
    public static final String Attachments              = "attachments";

    public static final String FunctionPools            = "function_pools";
    public static final String AttachmentFunctionPools  = "attachment_function_pools";
    public static final String IsMutable                = "is_mutable";

    public static final String Functions                = "functions";
    public static final String Prototype                = "prototype";
    public static final String Parameters               = "parameters";
    public static final String ReturnType               = "return_type";

    public static final String NamespaceUUID            = "namespace_uuid";
    public static final String NamespaceName            = "namespace_name";
    public static final String Name                     = "name";

    public static final String Value                    = "value";
    public static final String Documentation            = "documentation";

    public static final String Parent                   = "parent";
    public static final String Type                     = "type";
    public static final String RuntimeId                = "runtime_id";
    public static final String KeyType                  = "key_type";
    public static final String ElementType              = "element_type";
    public static final String DocumentType             = "document_type";
    public static final String Fields                   = "fields";
    public static final String DefaultValue             = "default_value";
    public static final String Members                  = "members";
    public static final String LiteralList              = "literal_list";
    public static final String LiteralValue             = "literal_value";

    public static final String None                     = "none";
    public static final String Boolean                  = "boolean";
    public static final String Integer                  = "integer";
    public static final String Float                    = "float";
    public static final String Double                   = "double";
    public static final String String                   = "string";
    public static final String UUId                     = "uuid";
    public static final String EnumerationCase          = "enumeration_case";

    public static final String ClassName                = "class_name";
    public static final String Types                    = "types";
    public static final String TypePrimitive            = "primitive";

    public static final String TypeKey                  = "key";
    public static final String TypeVec                  = "vec";
    public static final String Size                     = "size";

    public static final String TypeMat                  = "mat";
    public static final String Columns                  = "columns";
    public static final String Rows                     = "rows";

    public static final String TypeTuple                = "tuple";
    public static final String TypeOptional             = "optional";
    public static final String TypeVector               = "vector";
    public static final String TypeSet                  = "set";
    public static final String TypeMap                  = "map";
    public static final String TypeXArray               = "xarray";

    public static final String TypeAny                  = "any";
    public static final String TypeVariant              = "variant";

    public static final String TypeEnumeration          = "enumeration";
    public static final String TypeStructure            = "structure";

    public static final String TypeAnyConcept           = "any_concept";
    public static final String TypeConcept              = "concept";
    public static final String TypeClub                 = "club";
    public static final String TypeReference            = "reference";
    public static final String Domain                   = "domain";

    public static final String Any                      = "any";
    public static final String Primitive                = "primitive";
    public static final String Concept                  = "concept";
    public static final String Club                     = "club";
    public static final String AnyConcept               = "any_concept";
    public static final String Enumeration              = "enumeration";
    public static final String Structure                = "structure";
}
