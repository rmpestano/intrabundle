package br.ufrgs.rmpestano.intrabundle.plugin;

import br.ufrgs.rmpestano.intrabundle.annotation.CurrentLocale;
import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.model.enums.LocaleEnum;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.forge.test.SingletonAbstractShellTest;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;

import javax.inject.Inject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by rmpestano on 8/2/14.
 */
public class LocalePluginTest extends SingletonAbstractShellTest {


    @Inject
    MessageProvider provider;

    @Deployment
    public static JavaArchive getDeployment() {
        JavaArchive jar = SingletonAbstractShellTest.getDeployment()
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.i18n")
                .addPackages(true, "br.ufrgs.rmpestano.intrabundle.event")
                .addClass(LocaleEnum.class)
                .addClass(CurrentLocale.class)
                .addClass(LocalePlugin.class);
        //System.out.println(jar.toString(true));
        return jar;
    }

    @Test
    public void shouldGetWelcomeMessage(){
        assertNotNull(provider.getMessage("osgi.welcome"));
    }

    @Test
    public void shouldChangeLocaleToPT() throws Exception {
        resetOutput();
        queueInputLines("Y");
        queueInputLines("1");
        getShell().execute("locale");
        assertEquals(provider.getMessage("osgi.welcome"),"Projeto OSGi encotrado, digite osgi + tab para listar os comandos disponiveis");
    }

    @Test
    public void shouldChangeLocaleToEN() throws Exception {
        resetOutput();
        queueInputLines("Y");
        queueInputLines("2");
        getShell().execute("locale");
        assertEquals(provider.getMessage("osgi.welcome"),"OSGi project detected, type osgi + tab to list available commands");
    }
}
