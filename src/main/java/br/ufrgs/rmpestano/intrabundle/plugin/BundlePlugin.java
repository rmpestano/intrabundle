package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.facet.BundleFacet;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModuleImpl;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;

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



    @DefaultCommand
    public void defaultCommand(@PipeIn String in, PipeOut out) {
        out.println(((OSGiModuleImpl)bundle).getProjectRoot().toString());
    }

    @Command(help = "true if bundles exported packages contains only interfaces, false if it contains one or more classes")
    public void publishesInterfaces(@PipeIn String in, PipeOut out, @Option String... args) {
        out.println(bundle.getPublishesInterfaces().toString());
    }


}
