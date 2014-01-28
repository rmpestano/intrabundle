package br.ufrgs.rmpestano.intrabundle.model;

import org.jboss.forge.project.Project;

import java.io.Serializable;

/**
 * Created by rmpestano on 1/28/14.
 */
public interface OSGiModule extends Serializable,Project {

    Long getLinesOfCode();

    Boolean getUsesDeclarativeServices();
}
