package com.digitalsubstrate.template;

import com.digitalsubstrate.converter.TargetLayout;
import com.digitalsubstrate.viper.dsm.DSMFunctionPool;

import java.util.ArrayList;

public final class TemplateFunctionPool {

    final public DSMFunctionPool dsmFunctionPool;
    final public ArrayList<TemplateFunction> functions;

    public TemplateFunctionPool(ArrayList<TemplateFunction> functions, DSMFunctionPool dsmFunctionPool) {
        this.functions = functions;
        this.dsmFunctionPool = dsmFunctionPool;
    }

    private TemplateDefinitions model;
    private TemplateIncludePaths include;

    /**
     * The whole model, for what a pool does not own.
     *
     * <p>A pool is a unit like a namespace — it holds only functions — so a template
     * rendering one needs the same two things a namespace template needs: the banner,
     * and a way to reach artefacts that are not its own.
     */
    public TemplateDefinitions getModel() {
        return model;
    }

    /** Where this pool's own artefacts are found: {@code <p.include.FunctionPoolBridges>}. */
    public TemplateIncludePaths getInclude() {
        return include;
    }

    /** The include guard of each of this pool's artefacts: {@code <p.guard.Pool>}. */
    public TemplateIncludePaths getGuard() {
        return include.guards();
    }

    public void setModel(TemplateDefinitions model, TargetLayout layout) {
        this.model = model;
        this.include = new TemplateIncludePaths(layout, TargetLayout.Scope.UNIT, model.getNamespace(), getName());
    }

    /**
     * The namespaces this pool's signatures reach.
     *
     * <p>A pool is a unit, and like any unit it needs the dependencies of what it emits.
     * What it emits is function declarations, so it fills
     * {@code dependencies.functions} — {@code <p.dependencies.functions:{d|#include "<d.include.Data>"}>}
     * — and a pool whose signatures name no namespaced type includes nothing.
     */
    public final TemplateDependencies dependencies = new TemplateDependencies();

    public TemplateDependencies getDependencies() {
        return dependencies;
    }

    /** The name as this target spells it: the DSM name, unless the project spells it otherwise. */
    public String getName() {
        return TemplateTool.spell(dsmFunctionPool.name);
    }

    /** The DSM name, the one the runtime knows: write it where a name is sent to the runtime. */
    public String getDsmName() {
        return dsmFunctionPool.name;
    }

    public String getUuid() {
        return dsmFunctionPool.uuid.toString();
    }

    // Components
    public ArrayList<TemplateFunction> getFunctions() {
        return functions;
    }

    // Documentation
    public Boolean getHasDocumentation() {
        return !dsmFunctionPool.documentation.isEmpty();
    }

    public String getDocumentation() {
        return dsmFunctionPool.documentation;
    }

    // Type
    public String getType() {
        return "FunctionPool";
    }
}
