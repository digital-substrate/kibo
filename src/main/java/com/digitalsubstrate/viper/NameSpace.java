package com.digitalsubstrate.viper;

import java.util.Objects;
import java.util.UUID;

public class NameSpace {
    public final UUID uuid;
    public final String name;

    private static final UUID Zero = UUID.fromString("00000000-0000-0000-0000-000000000000");
    public static final NameSpace GLOBAL = new NameSpace();

    public NameSpace() {
        this.uuid = Zero;
        this.name = "";
    }

    public NameSpace(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public boolean isGlobal() {
        return uuid.equals(Zero);
    }

    public String representation(String identifier) {
        return isGlobal() ? identifier : name + "::" + identifier;
    }

    public String representationIn(NameSpace nameSpace, String identifier) {
        return nameSpace.uuid.equals(uuid) ? identifier : representation(identifier);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof NameSpace nameSpace)) return false;
        return Objects.equals(uuid, nameSpace.uuid);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(uuid);
    }

}
