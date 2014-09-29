package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.event.ProjectChangeEvent;
import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.jasper.JasperManager;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.util.BundleFinder;
import br.ufrgs.rmpestano.intrabundle.util.Constants;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;

import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.Date;
import java.util.List;

/**
 * Created by rmpestano on 3/17/14.
 */
@Alias("osgi-scan")
public class OSGiScanPlugin implements Plugin {

    @Inject
    MessageProvider provider;

    @Inject
    BundleFinder bundleFinder;

    @Inject
    JasperManager jasperManager;

    @Inject
    ShellPrompt prompt;

    @Inject
    Shell shell;
    private List<Integer> moduleLevels;

    @Inject
    Event<ProjectChangeEvent> projectChangeEvent;


    @DefaultCommand(help = "Search for OSGi bundles in directories. You can pass the number of directories to go down as command parameter(2 is default value) ")
    public void scan(@PipeIn String in, PipeOut out, @Option(required = true, defaultValue = "2", help = "Number of directory levels to search for OSGi bundles, default is 2") Integer levels) {
        out.println(ShellColor.YELLOW, provider.getMessage("osgi.scan",levels));
        long initialTime = new Date().getTime();
        OSGiProject projectFound = bundleFinder.scan(shell.getCurrentDirectory(), levels);
        out.println(provider.getMessage("osgi.scan.time",((new Double(new Long(new Date().getTime())) - new Double(new Long(initialTime)))/1000)));
        if (projectFound == null) {
            out.println(ShellColor.RED, provider.getMessage("osgi.scan.noBundlesFound"));
        } else {
            projectChangeEvent.fire(new ProjectChangeEvent(projectFound));
            Boolean generateReport = prompt.promptBoolean(provider.getMessage("osgi.scan.bundlesFound", projectFound.getModules().size()));
            if (generateReport) {
                jasperManager.reportFromProject(projectFound, Constants.REPORT.GENERAL, Constants.REPORT.METRICS);
            }


        }

    }

}
