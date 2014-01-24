/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package br.ufrgs.rmpestano.intrabundle;

import org.apache.velocity.app.Velocity;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.JavaSourceFacet;
import org.jboss.forge.resources.java.JavaResource;
import org.jboss.forge.shell.Shell;
import org.jboss.forge.shell.events.PickupResource;
import org.jboss.forge.shell.plugins.*;

import javax.enterprise.event.Event;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;
import java.util.Properties;

/**
 * @author rmpestano Oct 23, 2011 1:16:47 PM
 */
@Alias("intrabundle")
@RequiresFacet(JavaSourceFacet.class)
@Help("TODO")
public class Intrabundle implements Plugin {

    @Inject
    private Project project;
    @Inject
    BeanManager beanManager;
    @Inject
    private Event<PickupResource> pickup;
    @Inject
    @Current
    private JavaResource resource;
    @Inject
    private Shell shell;

    static {
        Properties properties = new Properties();
        properties.setProperty("resource.loader", "class");
        properties.setProperty("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        Velocity.init(properties);
    }


}
