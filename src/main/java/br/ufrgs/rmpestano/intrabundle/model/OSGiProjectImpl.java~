package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.maven.MavenCoreFacet;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.ParserException;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.project.BaseProject;
import org.jboss.forge.project.Facet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.FacetNotFoundException;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

import javax.enterprise.inject.Typed;
import java.util.*;

/**
 * Created by rmpestano on 1/22/14.
 */
@Typed
public class OSGiProjectImpl extends BaseProject implements OSGiProject,Project {
    private DirectoryResource projectRoot = null;
    private final ProjectFactory factory;
    private final ResourceFactory resourceFactory;
    private List<OSGiModule> modules;
    private Map<OSGiModule, List<OSGiModule>> moduleDependenciesMap;
    private Long linesOfCode;
    private Long linesOfTestCode;
    protected String version;
    protected String revision;
    protected ProjectUtils projectUtils;


    public OSGiProjectImpl() {
        factory = null;
        resourceFactory = null;
        projectRoot = null;
    }

    public OSGiProjectImpl(final ProjectFactory factory, final ResourceFactory resourceFactory, final DirectoryResource dir, ProjectUtils projectUtils) {
        this.factory = factory;
        this.projectRoot = dir;
        this.resourceFactory = resourceFactory;
        this.projectUtils = projectUtils;
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

    public List<OSGiModule> getModules() {
        if (modules == null) {
            modules = new ArrayList<OSGiModule>();
            Resource<?> srcDir = projectUtils.getProjectSourcePath(getProjectRoot());
            if(srcDir != null && srcDir.exists()){
               initalizeModules(modules,(DirectoryResource)srcDir);
            }
        }
        return modules;
    }

    public void setModules(List<OSGiModule> modules) {
        this.modules = modules;
    }

    private Map<OSGiModule,List<OSGiModule>> initalizeModulesDependencies() {
        Map<OSGiModule,List<OSGiModule>> dependencies = new HashMap<OSGiModule, List<OSGiModule>>();
        for (OSGiModule m1 : getModules()) {
            dependencies.put(m1,new ArrayList<OSGiModule>());
            if(m1.getImportedPackages().isEmpty()){
                //has no module dependencies, go to next module
                continue;
            }
            for (OSGiModule m2 : getModules()) {
                if(m2.equals(m1)){
                    continue;
                }
                if(!m2.getExportedPackages().isEmpty()){
                    for (String s : m2.getExportedPackages()) {
                        if(m1.getImportedPackages().contains(s)){
                            dependencies.get(m1).add(m2);
                            break;
                        }
                    }
                }


            }
        }
        return dependencies;
    }

    /**
     * initialize osgi modules under root dir
     * @param root
     * @return
     */
    private void initalizeModules(List<OSGiModule> modules, DirectoryResource root) {
        modules.addAll(findModulesRecursively(root));
    }

    private Collection<? extends OSGiModule> findModulesRecursively(Resource<?> root) {

        List<OSGiModule> result = new ArrayList<OSGiModule>();
        List<Resource<?>> children = root.listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource instanceof DirectoryResource;
            }
        });

        if(children != null && !children.isEmpty()){
            for (Resource<?> child : children) {
                DirectoryResource directoryResource = child.reify(DirectoryResource.class);
                if(directoryResource != null && projectUtils.isOsgiBundle(directoryResource)){
                    result.add(new OSGiModuleImpl(factory, resourceFactory,directoryResource));
                }
                else{
                    result.addAll(findModulesRecursively(child));
                }
            }
        }

        return result;
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

    public Map<OSGiModule, List<OSGiModule>> getModulesDependencies() {
        if(moduleDependenciesMap == null){
            moduleDependenciesMap = initalizeModulesDependencies();
        }
        return moduleDependenciesMap;
    }

    @Override
    public Long getLinesOfCode() {
        if(linesOfCode == null){
            linesOfCode = new Long(0);
            for (OSGiModule module : modules) {
                linesOfCode += module.getLinesOfCode();
            }
        }
        return linesOfCode;
    }

    @Override
    public Long getLinesOfTestCode() {
        if(linesOfTestCode == null){
            linesOfTestCode = new Long(0);
            linesOfTestCode = countProjectTestLines(projectRoot);
        }
        return linesOfTestCode;
    }

    /**
     * count lines of .java files that import test related classes
     *
     * @param projectRoot
     * @return
     */
    private Long countProjectTestLines(DirectoryResource projectRoot) {
        for (Resource<?> resource : projectRoot.listResources()) {
            if (resource instanceof FileResource<?> && resource.getName().endsWith(".java")) {
                try {
                    JavaSource source = null;
                    try {
                        source = JavaParser.parse(resource.getResourceInputStream());
                    } catch (ParserException e) {
                        //intentional
                        continue;
                    }
                    if( source.hasImport("junit.framework") || source.hasImport("junit.framework.Assert")
                    ||  source.hasImport("org.junit")  || source.hasImport("org.junit.Assert")
                    ||  source.hasImport("org.testng") || source.hasImport("org.testng.annotations.Test")
                    ||  source.hasImport("org.testng.Assert")|| source.hasImport("org.testng.annotations")
                            ) {
                        this.linesOfTestCode += projectUtils.countFileLines((FileResource<?>) resource);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resource instanceof DirectoryResource) {
                this.linesOfTestCode = countProjectTestLines((DirectoryResource) resource);
            }
        }
        return linesOfTestCode;
    }


    @Override
    public String getVersion() {
        if(version == null && projectUtils.isMavenProject(projectRoot)) {
            MavenCoreFacet mavenCoreFacet = this.getFacet(MavenCoreFacet.class);
            version = mavenCoreFacet.getMavenProject().getVersion();
        }
        return version;
    }


    @Override
    public String getRevision() {
        if(revision == null && (projectUtils.isGitProject(this)))  {
            revision = projectUtils.getProjectGitHeadRevision(this);
        }
        return revision;
    }
}
