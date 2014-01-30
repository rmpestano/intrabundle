package br.ufrgs.rmpestano.intrabundle.plugin;

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
    OSGiProject project;

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
             out.println(module.toString()+":"+(module.getActivator() != null ? module.getActivator().getFullyQualifiedName() : resourceBundle.get().getString("osgi.no-activator")));
        }
    }

    @Command(value ="listExportedPackages")
    public void listExportedPackages(@PipeIn String in, PipeOut out){
        OSGiModule choice = choiceModule();
        out.println(resourceBundle.get().getString("module.listExported",choice));
        if(choice.getExportedPackages().isEmpty()){
          out.println(resourceBundle.get().getString("module.noExportedPackages"));
        }
        else{
            for (String s : choice.getExportedPackages()) {
                out.println(s);
            }
        }

    }

    @Command(value ="listImportedPackages")
    public void listImportedPackages(@PipeIn String in, PipeOut out){
        OSGiModule choice = choiceModule();
        out.println(resourceBundle.get().getString("module.listImported",choice));
        if(choice.getImportedPackages().isEmpty()){
            out.println(resourceBundle.get().getString("module.noImportedPackages"));
        }
        else{
            for (String s : choice.getImportedPackages()) {
                out.println(s);
            }
        }

    }

    @Command("moduleDependencies")
    public void moduleDependencies(@PipeIn String in, PipeOut out){
        OSGiModule choice = choiceModule();
        out.println(resourceBundle.get().getString("module.dependencies",choice));
        if(project.getModulesDependencies().get(choice).isEmpty()){
            out.println(resourceBundle.get().getString("module.noDependency"));
        }
        else{
            for (OSGiModule m : project.getModulesDependencies().get(choice)) {
                out.println(m.toString());
            }
        }

    }

    private OSGiModule choiceModule(){
           return prompt.promptChoiceTyped(resourceBundle.get().getString("module.choice"), getModules());
    }

    public List<OSGiModule> getModules() {
        return project.getModules();
    }

}
