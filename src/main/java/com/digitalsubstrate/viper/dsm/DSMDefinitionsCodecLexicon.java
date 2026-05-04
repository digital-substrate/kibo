package com.digitalsubstrate.viper.dsm;

public final class DSMDefinitionsCodecLexicon {

    public static class LiteralDomain {
        public static final byte None = 0;
        public static final byte Boolean = 1;
        public static final byte Integer = 2;
        public static final byte Float = 3;
        public static final byte Double = 4;
        public static final byte String = 5;
        public static final byte Uuid = 6;
        public static final byte EnumerationCase = 7;
    }

    public static class Literal {
        public static final byte List = 0;
        public static final byte Value = 1;
    }

    public static class DSMType {
        public static final byte Key = 0;
        public static final byte Vec = 1;
        public static final byte Mat = 2;
        public static final byte Tuple = 3;
        public static final byte Optional = 4;
        public static final byte Vector = 5;
        public static final byte Set = 6;
        public static final byte Map = 7;
        public static final byte Variant = 8;
        public static final byte XArray = 9;
        public static final byte Reference = 10;
    }

    public static class ReferenceDomain {
        public static final byte Any = 0;
        public static final byte Primitive = 1;
        public static final byte Concept = 2;
        public static final byte Club = 3;
        public static final byte AnyConcept = 4;
        public static final byte Enumeration = 5;
        public static final byte Structure = 6;
    }
}
