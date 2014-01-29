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
    private FileResource<?> activator;
    private FileResource<?> manifest;


    public OSGiModuleImpl() {
        factory = null;
    }

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

    private FileResource<?> findActivator() throws IOException {
        RandomAccessFile randomAccessFile;
        File f = new File(getManifest().getFullyQualifiedName());
        randomAccessFile = new RandomAccessFile(f, "r");

        String line;
        while((line = randomAccessFile.readLine()) != null){
            if (line.contains("Bundle-Activator:")) {
               break;
            }
        }
        if(line == null){
            return null;//no activator
        }
        String actvatorPath = line.trim().substring(line.indexOf("Bundle-Activator:") + 17);
        actvatorPath = actvatorPath.trim().replaceAll("\\.","/");
        if(!actvatorPath.startsWith("/")){
            actvatorPath = "/" +actvatorPath;
        }
        actvatorPath = "/src"+actvatorPath;
        Resource<?> activator = getProjectRoot().getChild(actvatorPath.concat(".java"));
        if(activator == null || !activator.exists()){
            throw new RuntimeException("Could not find activator class at "+getProjectRoot() + actvatorPath);
        }

        return (FileResource<?>) activator;

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

    @Override
    public FileResource<?> getActivator() {
        if (activator == null) {
            try {
                activator = findActivator();
            } catch (IOException e) {
                throw new RuntimeException("Could not find activator class");
            }
        }
        return activator;
    }

    @Override
    public FileResource<?> getManifest() {
        if (manifest == null) {
            manifest = findManifest();
        }
        return manifest;
    }

    private FileResource<?> findManifest() {
        Resource<?> metaInf = getProjectRoot().getChild("META-INF");
        if (metaInf == null || !metaInf.exists()) {
            throw new RuntimeException("OSGi project(" + getProjectRoot().getFullyQualifiedName() + ") without META-INF directory cannot be analysed by intrabundle");
        }
        Resource<?> manifest = metaInf.getChild("MANIFEST.MF");
        if (manifest == null || !manifest.exists()) {
            throw new RuntimeException("OSGi project(" + getProjectRoot().getFullyQualifiedName() + ") without MANIFEST.MF file cannot be analysed by intrabundle");
        }
        return (FileResource<?>) manifest;
    }

    public Long getLinesOfCode() {
        if (totalLoc == null) {
            totalLoc = new Long(0L);
            totalLoc = countModuleLines(getProjectRoot());
        }
        return totalLoc;
    }
}
