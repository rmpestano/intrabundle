package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.facet.BundleFacet;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModuleImpl;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;

import javax.inject.Inject;
import java.util.Arrays;

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

    @Command
    public void command(@PipeIn String in, PipeOut out, @Option String... args) {
        if (args == null)
            out.println("Executed named command without args.");
        else
            out.println("Executed named command with args: " + Arrays.asList(args));
    }

    @Command
    public void prompt(@PipeIn String in, PipeOut out) {
        if (prompt.promptBoolean("Do you like writing Forge plugins?"))
            out.println("I am happy.");
        else
            out.println("I am sad.");
    }
}
