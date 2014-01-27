package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.event.LocaleChangeEvent;
import br.ufrgs.rmpestano.intrabundle.i18n.ResourceBundle;
import br.ufrgs.rmpestano.intrabundle.model.LocaleEnum;
import org.jboss.forge.project.facets.events.InstallFacets;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Locale;

/**
 *
 */
@Alias("locale")
@Singleton
public class LocalePlugin implements Plugin {

    @Inject
    private ShellPrompt prompt;

    @Inject
    @br.ufrgs.rmpestano.intrabundle.annotation.Current
    Instance<ResourceBundle> resourceBundle;

    @Inject
    Event<LocaleChangeEvent> localeEvent;

    @Inject
    private Event<InstallFacets> event;

    private String localeLanguage;

    @DefaultCommand
    public void defaultCommand(@PipeIn String in, PipeOut out) {
        boolean answer = prompt.promptBoolean(resourceBundle.get().getString("locale.change", getLocaleLanguage()),true);
        if(answer){
            String choice = prompt.promptChoiceTyped(resourceBundle.get().getString("locale.choice"),LocaleEnum.getAll());
            localeEvent.fire(new LocaleChangeEvent());
            localeLanguage = choice;
        }
    }

    @Produces
    @br.ufrgs.rmpestano.intrabundle.annotation.Current
    public String getLocaleLanguage() {
        if(localeLanguage == null){
            localeLanguage = LocaleEnum.getByLocale(Locale.getDefault());
        }
        return localeLanguage;
    }

    @Command
    public void prompt(@PipeIn String in, PipeOut out) {
        if (prompt.promptBoolean("Do you like writing Forge plugins?"))
            out.println("I am happy.");
        else
            out.println("I am sad.");
    }
}
