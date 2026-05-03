package com.digitalsubstrate.template;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;

public final class TemplateDefinitions {

    private final String generated;
    private final String namespace;

    public final ArrayList<TemplateNameSpace> nameSpaces = new ArrayList<>();

    // Global definitions sort by TypeName.representation()
    public final ArrayList<TemplateConcept> concepts = new ArrayList<>();
    public final ArrayList<TemplateClub> clubs = new ArrayList<>();
    public final ArrayList<TemplateStructure> sortedStructures = new ArrayList<>();
    public final ArrayList<TemplateStructure> structures = new ArrayList<>();
    public final ArrayList<TemplateEnumeration> enumerations = new ArrayList<>();

    // Functions
    public final ArrayList<TemplateVecFunction> vecFunctions = new ArrayList<>();
    public final ArrayList<TemplateMatFunction> matFunctions = new ArrayList<>();
    public final ArrayList<TemplateTupleFunction> tupleFunctions = new ArrayList<>();
    public final ArrayList<TemplateOptionalFunction> optionalFunctions = new ArrayList<>();
    public final ArrayList<TemplateVectorFunction> vectorFunctions = new ArrayList<>();
    public final ArrayList<TemplateSetFunction> setFunctions = new ArrayList<>();
    public final ArrayList<TemplateMapFunction> mapFunctions = new ArrayList<>();
    public final ArrayList<TemplateXArrayFunction> xarrayFunctions = new ArrayList<>();
    public final ArrayList<TemplateVariantFunction> variantFunctions = new ArrayList<>();

    // Attachments
    public final ArrayList<TemplateAttachment> attachments = new ArrayList<>();

    // Pools
    public final ArrayList<TemplateFunctionPool> functionPools = new ArrayList<>();
    public final ArrayList<TemplateAttachmentFunctionPool> attachmentFunctionPools = new ArrayList<>();

    private ArrayList<TemplateAttachedKeyType> attachedKeyTypes;
    private ArrayList<TemplateAttachedDocumentType> attachedDocumentTypes;

    public TemplateDefinitions(String generated, String namespace) {
        this.generated = generated;
        this.namespace = namespace;
    }

    public String getGenerated() {
        return generated;
    }

    public String getNamespace() {
        return namespace;
    }

    // Components
    public ArrayList<TemplateStructure> getSortedStructures() {
        return sortedStructures;
    }

    public ArrayList<TemplateStructure> getStructures() {
        return structures;
    }

    public ArrayList<TemplateAttachedKeyType> getAttachedKeyTypes() {
        if (attachedKeyTypes == null) {
            final var types = new HashMap<String, TemplateAttachedKeyType>();
            for (var attachment : attachments)
                types.put(attachment.getKeyType().getTypeSuffix(), attachment.getKeyType());

            attachedKeyTypes = new ArrayList<>();
            for (String key : types.keySet())
                attachedKeyTypes.add(types.get(key));

            attachedKeyTypes.sort(Comparator.comparing(TemplateAttachedKeyType::getTypeSuffix));
        }
        return attachedKeyTypes;
    }

    public ArrayList<TemplateAttachedDocumentType> getAttachedDocumentTypes() {
        if (attachedDocumentTypes == null) {
            final var types = new HashMap<String, TemplateAttachedDocumentType>();
            for (var attachment : attachments)
                types.put(attachment.getDocumentType().getTypeSuffix(), attachment.getDocumentType());

            attachedDocumentTypes = new ArrayList<>();
            for (String key : types.keySet())
                attachedDocumentTypes.add(types.get(key));

            attachedDocumentTypes.sort(Comparator.comparing(TemplateAttachedDocumentType::getTypeSuffix));
        }
        return attachedDocumentTypes;
    }

    // AttachmentFunction Pool Field
    public String getAttachmentsPoolUuid() throws Exception {
        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        messageDigest.reset();
        String key = namespace + ".Pool.Attachments";
        messageDigest.update(key.getBytes(StandardCharsets.UTF_8));
        final byte[] data = messageDigest.digest();

        StringBuilder sb = new StringBuilder();
        for (byte b : data) sb.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        var hexDigest = sb.toString();

        StringBuilder uuid = new StringBuilder();
        uuid.append(hexDigest, 0, 8);
        uuid.append("-");
        uuid.append(hexDigest, 8, 12);
        uuid.append("-");
        uuid.append(hexDigest, 12, 16);
        uuid.append("-");
        uuid.append(hexDigest, 16, 20);
        uuid.append("-");
        uuid.append(hexDigest.substring(20));

        return uuid.toString();
    }
}
