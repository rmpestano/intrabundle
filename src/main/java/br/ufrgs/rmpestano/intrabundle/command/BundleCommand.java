package br.ufrgs.rmpestano.intrabundle.command;

import br.ufrgs.rmpestano.intrabundle.facet.BundleFacet;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import org.jboss.forge.addon.facets.constraints.FacetConstraint;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.ui.annotation.Command;
import org.jboss.forge.addon.ui.context.UIContext;
import org.jboss.forge.addon.ui.output.UIOutput;

import javax.inject.Inject;
import java.util.List;

/**
 *
 */

@FacetConstraint(BundleFacet.class)
public class BundleCommand  {

    @Inject
    OSGiModule bundle;


    @Command(value = "usesDeclarativeServices")
    public void usesDeclarativeServices(final UIContext context, final UIOutput output) {
           output.out().println(bundle.getUsesDeclarativeServices() ? "yes":"no");
    }

    @Command(value = "usesBlueprint")
    public void usesBlueprint(final UIContext context, final UIOutput output) {
        output.out().println(bundle.getUsesBlueprint() ?"yes":"no");
    }

    @Command(value = "declaresPermissions")
    public void declaresPermissions(final UIContext context, final UIOutput output) {
        output.out().println(bundle.getDeclaresPermissions() ? "yes" : "no");
    }

    @Command(value = "activator", help = "prints module activator path")
    public void activator(final UIContext context, final UIOutput output) {
        output.out().println((bundle.getActivator() != null ? bundle.getActivator().getFullyQualifiedName() : "osgi.no-activator"));
    }

    @Command(value = "exportedPackages",help = "list bundle exported packages")
    public void exportedPackages(final UIContext context, final UIOutput output){
        if(bundle.getExportedPackages().isEmpty()){
            output.out().println("module.noExportedPackages");
        }
        else{
            for (String s : bundle.getExportedPackages()) {
                output.out().println(s);
            }
        }
    }

    @Command(value = "importedPackages",help = "list bundle imported packages")
    public void importedPackages(final UIContext context, final UIOutput output){
        if(bundle.getImportedPackages().isEmpty()){
            output.out().println("module.noImportedPackages");
        }
        else{
            for (String s : bundle.getImportedPackages()) {
                output.out().println(s);
            }
        }
    }

    @Command(value = "requiredBundles",help = "list bundle required bundles")
    public void requiredBundles(final UIContext context, final UIOutput output){
        if(bundle.getRequiredBundles().isEmpty()){
            output.out().println("module.noRequiredBundles");
        }
        else{
            for (String s : bundle.getRequiredBundles()) {
                output.out().println(s);
            }
        }
    }

    @Command(value = "publishesInterfaces", help = "true if bundle exported packages contains only interfaces, false if it contains one or more classes")
    public void publishesInterfaces(final UIContext context, final UIOutput output) {
        output.out().println(bundle.getPublishesInterfaces() ? "yes" : "no");
    }

    @Command(value = "loc",help = "Count bundle lines of code")
    public void loc(final UIContext context, final UIOutput output){
        output.out().println(bundle.getLinesOfCode() != null ? bundle.getLinesOfCode().toString() : "0");
    }

    @Command(value = "lot",help = "Count bundle lines of test code")
    public void lot(final UIContext context, final UIOutput output){
        output.out().println(bundle.getLinesOfTestCode() != null ? bundle.getLinesOfTestCode().toString() : "0");
    }

    @Command(value = "staleReferences",help = "List files possibly containing OSGi service stale references")
    public void staleReferences(final UIContext context, final UIOutput output){
        List<Resource<?>> staleReferences = bundle.getStaleReferences();
        if(!staleReferences.isEmpty()){
            output.out().println("bundle.listing-stale-references");
            for (Resource<?> staleReference : staleReferences) {
                output.out().println(staleReference.getFullyQualifiedName());
            }

        }
        else{
            output.out().println("bundle.noStaleReferences");
        }
    }


}
