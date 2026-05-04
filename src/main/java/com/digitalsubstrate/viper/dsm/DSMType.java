package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;

public abstract class DSMType {

    public abstract String representation();
    public abstract String representationIn(NameSpace namespace);

}
