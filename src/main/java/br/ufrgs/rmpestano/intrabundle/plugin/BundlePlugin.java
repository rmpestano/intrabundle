package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.facet.BundleFacet;
import br.ufrgs.rmpestano.intrabundle.i18n.ResourceBundle;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;

/**
 *
 */
@Alias("bundle")
@RequiresFacet(BundleFacet.class)
public class BundlePlugin implements Plugin {
    @Inject
    private ShellPrompt prompt;

    @Inject
    OSGiModule bundle;

    @Inject
    @br.ufrgs.rmpestano.intrabundle.annotation.Current
    Instance<ResourceBundle> resourceBundle;


    @DefaultCommand
    public void defaultCommand(@PipeIn String in, PipeOut out) {
        out.println("default command");
    }

    @Command
    public void usesDeclarativeServices(@PipeIn String in, PipeOut out) {
           out.println(bundle.getUsesDeclarativeServices().toString());
    }

    @Command(help = "prints module activator path")
    public void activator(@PipeIn String in, PipeOut out) {
            out.println((bundle.getActivator() != null ? bundle.getActivator().getFullyQualifiedName() : resourceBundle.get().getString("osgi.no-activator")));
    }

    @Command(help = "list bundle exported packages")
    public void exportedPackages(PipeOut out){
        if(bundle.getExportedPackages().isEmpty()){
            out.println(resourceBundle.get().getString("module.noExportedPackages"));
        }
        else{
            for (String s : bundle.getExportedPackages()) {
                out.println(s);
            }
        }
    }

    @Command(help = "list bundle imported packages")
    public void importedPackages(PipeOut out){
        if(bundle.getImportedPackages().isEmpty()){
            out.println(resourceBundle.get().getString("module.noImportedPackages"));
        }
        else{
            for (String s : bundle.getImportedPackages()) {
                out.println(s);
            }
        }
    }

    @Command(help = "true if bundle exported packages contains only interfaces, false if it contains one or more classes")
    public void publishesInterfaces(@PipeIn String in, PipeOut out, @Option String... args) {
        out.println(bundle.getPublishesInterfaces().toString());
    }


}
