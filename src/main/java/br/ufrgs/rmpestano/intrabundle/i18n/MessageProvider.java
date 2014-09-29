package br.ufrgs.rmpestano.intrabundle.i18n;

import br.ufrgs.rmpestano.intrabundle.annotation.CurrentLocale;
import br.ufrgs.rmpestano.intrabundle.event.LocaleChangeEvent;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ResourceBundle;

@Singleton
public class MessageProvider implements Serializable {


    @Inject
    @CurrentLocale
    Instance<String> localeLanguage;

    private ResourceBundle currentBundle;

    public String getMessage(String key) {
        return getCurrentBundle().getString(key);
    }

    public String getMessage(String key, Object... params) {
        return MessageFormat.format(getCurrentBundle().getString(key), params);
    }

    public ResourceBundle getCurrentBundle() {
        if (currentBundle == null) {
            try {
                currentBundle = ResourceBundle.getBundle("messages_"+ localeLanguage.get());
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Could not find bundle:"+"messages_"+ localeLanguage.get()+".properties");
            }
        }
        return currentBundle;
    }


    public void onLocaleChange(@Observes LocaleChangeEvent localeChangeEvent){
        currentBundle = null;
    }

}
