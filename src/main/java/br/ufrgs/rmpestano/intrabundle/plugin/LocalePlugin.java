package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.annotation.CurrentLocale;
import br.ufrgs.rmpestano.intrabundle.event.LocaleChangeEvent;
import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.model.enums.LocaleEnum;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.Alias;
import org.jboss.forge.shell.plugins.DefaultCommand;
import org.jboss.forge.shell.plugins.Plugin;

import javax.enterprise.event.Event;
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
    MessageProvider provider;

    @Inject
    Event<LocaleChangeEvent> localeEvent;

    private String localeLanguage;

    @DefaultCommand
    public void defaultCommand() {
        boolean answer = prompt.promptBoolean(provider.getMessage("locale.change", getLocaleLanguage()),true);
        if(answer){
            String choice = prompt.promptChoiceTyped(provider.getMessage("locale.choice"),LocaleEnum.getAll());
            localeEvent.fire(new LocaleChangeEvent());
            localeLanguage = choice;
        }
    }

    @Produces
    @CurrentLocale
    public String getLocaleLanguage() {
        if(localeLanguage == null){
            localeLanguage = LocaleEnum.getByLocale(Locale.getDefault());
        }
        return localeLanguage;
    }


}
