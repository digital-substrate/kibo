package com.digitalsubstrate.kibo;

import com.beust.jcommander.converters.IParameterSplitter;

import java.util.List;

/** A repeatable option whose value may hold commas, as a DSM type does: one value per occurrence. */
public final class WholeValue implements IParameterSplitter {
    @Override
    public List<String> split(String value) {
        return List.of(value);
    }
}
