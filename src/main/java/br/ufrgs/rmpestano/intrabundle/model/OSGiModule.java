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
public class OSGiModule extends BaseProject {
    private DirectoryResource projectRoot = null;
    private final ProjectFactory factory;
    private Long totalLoc;

    public OSGiModule(final ProjectFactory factory, final DirectoryResource dir) {
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
        return "OSGiModule [" + getProjectRoot() + "]";
    }

    public Long getLinesOfCode() {

        this.totalLoc = new Long(0L);
        return countLines(getProjectRoot());
    }

    private Long countLines(DirectoryResource projectRoot) {
        for (Resource<?> resource : projectRoot.listResources()) {
              if(resource instanceof FileResource<?> && resource.getName().endsWith(".java")){
                  try{
                      this.totalLoc +=  getFileLines((FileResource<?>)resource);
                  }catch (Exception e){
                      e.printStackTrace();
                  }
              }
              else if(resource instanceof DirectoryResource){
                  this.totalLoc = countLines((DirectoryResource)resource);
              }
        }
        return totalLoc;
    }

    private Long getFileLines(FileResource<?> resource) throws IOException {
        RandomAccessFile file = new RandomAccessFile(new File(resource.getFullyQualifiedName()),"r");
        Long total = new Long(0);
        String line;
        while ((line = file.readLine()) != null){
              total++;
        }
        return total;
    }
}
