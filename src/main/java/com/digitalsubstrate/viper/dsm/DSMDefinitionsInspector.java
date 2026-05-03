// Copyright (c) Digital Substrate 2026, All rights reserved.

package com.digitalsubstrate.viper.dsm;

import com.digitalsubstrate.viper.NameSpace;
import com.digitalsubstrate.viper.TypeName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.UUID;

public final class DSMDefinitionsInspector {

    private final DSMDefinitions definitions;

    private final HashMap<TypeName, DSMConcept> concepts = new HashMap<>();
    private final HashMap<TypeName, DSMClub> clubs = new HashMap<>();
    private final HashMap<TypeName, DSMEnumeration>  enumerations = new HashMap<>();
    private final HashMap<TypeName, DSMStructure> structures = new HashMap<>();
    private final HashMap<String, DSMAttachment>attachments = new HashMap<>();
    private final HashMap<UUID, DSMFunctionPool> functionPools = new HashMap<>();
    private final HashMap<UUID, DSMAttachmentFunctionPool> attachmentFunctionPools = new HashMap<>();
    private final HashSet<NameSpace> nameSpaces = new HashSet<>();
    private final HashMap<String, HashSet<TypeName>> identifiers = new HashMap<>();

    public DSMDefinitionsInspector(DSMDefinitions definitions) {
        this.definitions = definitions;
        for (var e : this.definitions.concepts) {
            concepts.put(e.typeName, e);
            registerTypeName(e.typeName);
        }

        for (var e : this.definitions.clubs) {
            clubs.put(e.typeName, e);
            registerTypeName(e.typeName);
        }

        for (var e : this.definitions.enumerations) {
            enumerations.put(e.typeName, e);
            registerTypeName(e.typeName);
        }

        for (var e : this.definitions.structures) {
            structures.put(e.typeName, e);
            registerTypeName(e.typeName);
        }

        for (var e : this.definitions.attachments) {
            attachments.put(e.getIdentifier(), e);
        }

        for (var e : this.definitions.functionPools) {
            functionPools.put(e.uuid, e);
        }

        for (var e : this.definitions.attachmentFunctionPools) {
            attachmentFunctionPools.put(e.uuid, e);
        }
    }

    public DSMDefinitions getDefinitions() {
        return definitions;
    }

    public HashMap<TypeName, DSMConcept> getConcepts() {
        return concepts;
    }

    public HashMap<TypeName, DSMClub> getClubs() {
        return clubs;
    }

    public HashMap<TypeName, DSMEnumeration> getEnumerations() {
        return enumerations;
    }

    public HashMap<TypeName, DSMStructure> getStructures() {
        return structures;
    }

    public HashMap<String, DSMAttachment> getAttachments() {
        return attachments;
    }

    public HashMap<UUID, DSMFunctionPool> getFunctionPools() {
        return functionPools;
    }

    public HashMap<UUID, DSMAttachmentFunctionPool> getAttachmentFunctionPools() {
        return attachmentFunctionPools;
    }

    public HashSet<NameSpace> getNameSpaces() {
        return nameSpaces;
    }

    void registerTypeName(TypeName typeName) {
        nameSpaces.add(typeName.nameSpace);
        final var typeNames = identifiers.computeIfAbsent(typeName.name, k -> new HashSet<TypeName>());
        typeNames.add(typeName);
    }
}
