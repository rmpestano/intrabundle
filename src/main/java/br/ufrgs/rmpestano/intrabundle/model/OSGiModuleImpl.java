package br.ufrgs.rmpestano.intrabundle.model;

import org.jboss.forge.project.BaseProject;
import org.jboss.forge.project.Facet;
import org.jboss.forge.project.facets.FacetNotFoundException;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;

import javax.enterprise.inject.Typed;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by rmpestano on 1/22/14.
 */
@Typed()
public class OSGiModuleImpl extends BaseProject implements OSGiModule {
    private DirectoryResource projectRoot = null;
    private final ProjectFactory factory;
    private Long totalLoc;
    private Boolean usesDeclarativeServices;

    public OSGiModuleImpl(final ProjectFactory factory, final DirectoryResource dir) {
        this.factory = factory;
        this.projectRoot = dir;
    }

    @Override
    public <F extends Facet> F getFacet(final Class<F> type) {
        try {
            return super.getFacet(type);
        } catch (FacetNotFoundException e) {
            factory.registerSingleFacet(this, type);
            return super.getFacet(type);
        }
    }

    @Override
    public DirectoryResource getProjectRoot() {
        return projectRoot;
    }

    @Override
    public boolean exists() {
        return (projectRoot != null) && projectRoot.exists();
    }

    @Override
    public String toString() {
        return getProjectRoot().toString();
    }


    private Long countModuleLines(DirectoryResource projectRoot) {
        for (Resource<?> resource : projectRoot.listResources()) {
            if (resource instanceof FileResource<?> && resource.getName().endsWith(".java")) {
                try {
                    this.totalLoc += countFileLines((FileResource<?>) resource);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resource instanceof DirectoryResource) {
                this.totalLoc = countModuleLines((DirectoryResource) resource);
            }
        }
        return totalLoc;
    }

    private Long countFileLines(FileResource<?> resource) throws IOException {
        RandomAccessFile file = new RandomAccessFile(new File(resource.getFullyQualifiedName()), "r");
        Long total = new Long(0);
        String line;
        while ((line = file.readLine()) != null) {
            total++;
        }
        return total;
    }

    private boolean usesDeclarativeServices() {
        Resource<?> OSGiInf = getProjectRoot().getChild("OSGI-INF");
        return OSGiInf.exists() && OSGiInf.getChild("service.xml").exists();
    }

    //getters

    public Boolean getUsesDeclarativeServices() {
        if (usesDeclarativeServices == null) {
            usesDeclarativeServices = usesDeclarativeServices();
        }
        return usesDeclarativeServices;
    }

    public Long getLinesOfCode() {
        if (totalLoc == null) {
            totalLoc = new Long(0L);
            totalLoc = countModuleLines(getProjectRoot());
        }
        return totalLoc;
    }
}
