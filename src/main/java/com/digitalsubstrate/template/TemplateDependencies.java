package com.digitalsubstrate.template;

import java.util.ArrayList;

/**
 * What a namespace reaches, separated by the kind of declaration that reaches it.
 *
 * <p>A unit's dependencies are not one list, and treating them as one produces a wrong
 * include that compiles. `Projection` reaches `ModelC` through an attachment's document
 * type and through nothing else; its types header therefore includes `ModelC` for a use
 * it does not make, and nothing reports it. Its field-addressing header would include
 * both drivers for fields it does not name, since a path names a position and not a type.
 *
 * <p>So an artefact asks for the dependencies of <em>what it emits</em>:
 * {@code <u.dependencies.types:…>} in a types template,
 * {@code <u.dependencies.attachments:…>} in an attachments one. {@code all} is the union,
 * which is what the emission order is built on — that is a property of the namespace, not
 * of any one artefact.
 */
public final class TemplateDependencies {

    /** Reached by concept parents, club members and structure fields. */
    public final ArrayList<TemplateNameSpace> types = new ArrayList<>();

    /** Reached by an attachment's key or document type. */
    public final ArrayList<TemplateNameSpace> attachments = new ArrayList<>();

    /** Everything the namespace reaches, in emission order. */
    public final ArrayList<TemplateNameSpace> all = new ArrayList<>();

    public ArrayList<TemplateNameSpace> getTypes() {
        return types;
    }

    public ArrayList<TemplateNameSpace> getAttachments() {
        return attachments;
    }

    public ArrayList<TemplateNameSpace> getAll() {
        return all;
    }
}
