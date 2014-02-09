package br.ufrgs.rmpestano.intrabundle.model;

import org.jboss.forge.resources.FileResource;

import java.io.Serializable;
import java.util.List;

/**
 * Created by rmpestano on 1/28/14.
 */
public interface OSGiModule extends Serializable{

    Long getLinesOfCode();

    Long getLinesOfTestCode();

    Boolean getUsesDeclarativeServices();

    FileResource<?> getManifest();

    FileResource<?> getActivator();

    List<String> getImportedPackages();

    List<String> getExportedPackages();

    List<String> getRequiredBundles();

    Boolean getPublishesInterfaces();

    Boolean getDeclaresPermissions();

}
