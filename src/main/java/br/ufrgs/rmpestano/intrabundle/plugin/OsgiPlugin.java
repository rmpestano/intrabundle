package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.annotation.OSGi;
import br.ufrgs.rmpestano.intrabundle.facet.OSGiFacet;
import br.ufrgs.rmpestano.intrabundle.i18n.ResourceBundle;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;

/**
 *
 */
@Alias("osgi")
@RequiresFacet(OSGiFacet.class)
public class OsgiPlugin implements Plugin {

    @Inject
    private ShellPrompt prompt;

    @Inject
    @OSGi
    Instance<OSGiProject> project;

    @Inject
    @br.ufrgs.rmpestano.intrabundle.annotation.Current
    Instance<ResourceBundle> resourceBundle;


    @DefaultCommand
    public void defaultCommand(@PipeIn String in, PipeOut out) {
        out.println(ShellColor.YELLOW, resourceBundle.get().getString("osgi.defaultCommand"));
    }

    @Command(value = "countBundles")
    public void countBundles(@PipeIn String in, PipeOut out) {
        out.println("Total number of bundles:" + getModules().size());
    }

    @Command(value = "listBundles")
    public void listBundles(@PipeIn String in, PipeOut out) {
        for (int i = 0; i < getModules().size(); i++) {
            out.println("bundle(" + i + "):" + getModules().get(i).getProjectRoot());
        }
    }

    @Command(value = "loc", help = "count lines of code of all bundles")
    public void loc(@PipeIn String in, PipeOut out) {
        long total = 0;
        for (int i = 0; i < getModules().size(); i++) {
            long loci = getModules().get(i).getLinesOfCode();
            out.println(getModules().get(i).getProjectRoot() + ":" + loci);
            total += loci;
        }
        out.println("Total lines of code:" + total);
    }

    @Command(value = "usesDeclarativeServices", help = "list modules that use declarative services")
    public void usesDeclarativeServices(@PipeIn String in, PipeOut out) {
        out.println(resourceBundle.get().getString("osgi.declarativeServices"));
        for (OSGiModule module: getModules()) {
            if(module.getUsesDeclarativeServices()){
                out.println(module.toString());
            }
        }
    }

    @Command(value = "listActivators", help = "list modules activator classes")
    public void listActivators(@PipeIn String in, PipeOut out) {
        out.println(resourceBundle.get().getString("osgi.listActivators"));
        for (OSGiModule module: getModules()) {
             out.println(module.toString()+":"+module.getActivator().getFullyQualifiedName());
        }
    }

    public List<OSGiModule> getModules() {
        return project.get().getModules();
    }

}
