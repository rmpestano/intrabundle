package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.jdt.ASTVisitors;
import br.ufrgs.rmpestano.intrabundle.jdt.StaleReferencesVisitor;
import br.ufrgs.rmpestano.intrabundle.util.Constants;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.jboss.forge.parser.JavaParser;
import org.jboss.forge.parser.ParserException;
import org.jboss.forge.parser.java.JavaSource;
import org.jboss.forge.project.BaseProject;
import org.jboss.forge.project.Facet;
import org.jboss.forge.project.Project;
import org.jboss.forge.project.facets.FacetNotFoundException;
import org.jboss.forge.project.services.ProjectFactory;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.*;

import javax.enterprise.inject.Typed;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.*;

/**
 * Created by rmpestano on 1/22/14.
 */
@Typed()
public class OSGiModuleImpl extends BaseProject implements OSGiModule, Project {
    private DirectoryResource projectRoot = null;
    private final ProjectFactory factory;
    private final ResourceFactory resourceFactory;
    private Long totalLoc;
    private Long totalTestLoc;//test lines of code
    private Boolean usesDeclarativeServices;
    private Boolean usesBlueprint;
    private FileResource<?> activator;
    private FileResource<?> manifest;

    private List<String> exportedPackages;
    private List<String> importedPackages;
    private List<String> requiredBundles;
    private Boolean publishesInterfaces;
    private Boolean declaresPermissions;
    private List<Resource<?>> staleReferences;
    private String version;
    private Set<String> packages;
    private Integer numberOfClasses;
    private Integer numberOfAbstractClasses;
    private Integer numberOfInterfaces;


    public OSGiModuleImpl() {
        factory = null;
        resourceFactory = null;
    }

    public OSGiModuleImpl(final ProjectFactory factory, final ResourceFactory resourceFactory, final DirectoryResource dir) {
        this.factory = factory;
        this.projectRoot = dir;
        this.resourceFactory = resourceFactory;
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

    public void setProjectRoot(DirectoryResource projectRoot) {
        this.projectRoot = projectRoot;
    }

    @Override
    public boolean exists() {
        return (projectRoot != null) && projectRoot.exists();
    }

    @Override
    public String toString() {
        return getProjectRoot().toString();
    }

    private FileResource<?> findActivator() {
        RandomAccessFile randomAccessFile = null;
        Resource<?> activator = null;
        try {
            File f = new File(getManifest().getFullyQualifiedName());
            randomAccessFile = new RandomAccessFile(f, "r");
            String line;
            while ((line = randomAccessFile.readLine()) != null) {
                if (line.contains(Constants.Manifest.ACTIVATOR)) {
                    break;
                }
            }
            if (line == null) {
                return null;//no activator
            }
            String actvatorPath = line.trim().substring(line.indexOf(Constants.Manifest.ACTIVATOR) + 18);
            actvatorPath = actvatorPath.trim().replaceAll("\\.", "/");
            if (!actvatorPath.startsWith("/")) {
                actvatorPath = "/" + actvatorPath;
            }
            activator = ProjectUtils.getProjectSourcePath(projectRoot) != null ? ProjectUtils.getProjectSourcePath(projectRoot).getChild(actvatorPath.concat(".java")) : null;
            if (activator == null || !activator.exists()) {
                //try to infer activator path from projectRoot path
                if (actvatorPath.contains(projectRoot.getName())) {
                    String[] activatoPathTokens = actvatorPath.split("/");
                    String projectRootPath = projectRoot.getFullyQualifiedName().substring(0, projectRoot.getFullyQualifiedName().indexOf(activatoPathTokens[1]));
                    activator = resourceFactory.getResourceFrom(new File((projectRootPath + actvatorPath).replaceAll("//", "/").trim().concat(".java")));
                }
                if (activator == null || !activator.exists()) {
                    return null;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return (FileResource<?>) activator;

    }

    /**
     * count lines of .java files under src folder
     *
     * @param projectRoot
     * @return
     */
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

    /**
     * count lines of .java files under test folder
     *
     * @param projectRoot
     * @return
     */
    private Long countModuleTestLines(DirectoryResource projectRoot) {
        for (Resource<?> resource : projectRoot.listResources()) {
            if (resource instanceof FileResource<?> && resource.getName().endsWith(".java")) {
                try {
                    this.totalTestLoc += countFileLines((FileResource<?>) resource);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (resource instanceof DirectoryResource) {
                this.totalTestLoc = countModuleTestLines((DirectoryResource) resource);
            }
        }
        return totalTestLoc;
    }

    private Long countFileLines(FileResource<?> resource) throws IOException {
        RandomAccessFile file = new RandomAccessFile(new File(resource.getFullyQualifiedName()), "r");
        Long total = new Long(0);
        String line;
        while ((line = file.readLine()) != null) {
            total++;
        }
        file.close();
        return total;
    }

    private boolean usesDeclarativeServices() {
        Resource<?> manifest = getManifest();
        if (manifest != null && manifest.exists()) {
            RandomAccessFile aFile = null;
            try {
                aFile = new RandomAccessFile(new File(manifest.getFullyQualifiedName()), "r");
                String line;
                while ((line = aFile.readLine()) != null) {
                    if (line.contains(Constants.Manifest.DECLARATIVE_SERVICES)) {
                        return true;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (aFile != null) {
                    try {
                        aFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    private boolean usesBlueprint() {
        Resource<?> manifest = getManifest();
        if (manifest != null && manifest.exists()) {
            RandomAccessFile aFile = null;
            try {
                aFile = new RandomAccessFile(new File(manifest.getFullyQualifiedName()), "r");
                String line;
                while ((line = aFile.readLine()) != null) {
                    if (line.contains(Constants.Manifest.BLUE_PRINT)) {
                        return true;
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (aFile != null) {
                    try {
                        aFile.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return false;
    }

    private Boolean declaresPermissions() {
        Resource<?> OSGiInf = ProjectUtils.getProjectResourcesPath(projectRoot).getChild("OSGI-INF");
        return OSGiInf.exists() && OSGiInf.getChild("permissions.perm").exists();
    }

    private FileResource<?> findManifest() {
        Resource<?> manifest = ProjectUtils.getBundleManifest(projectRoot);
        if (manifest == null || !manifest.exists()) {
            throw new RuntimeException("OSGi bundle(" + getProjectRoot().getFullyQualifiedName() + ") without META-INF directory cannot be analysed by intrabundle");
        }
        return (FileResource<?>) manifest;
    }

    private List<String> findExportedPackages() {
        exportedPackages = new ArrayList<String>();
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(new File(getManifest().getFullyQualifiedName()), "r");
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(Constants.Manifest.EXPORT_PACKAGE)) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                    if (!"".equals(line)) {
                        exportedPackages.addAll(Arrays.asList(line.split(",")));
                    }
                    //try to get packages from next lines
                    String nextLine;
                    while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")) {
                        exportedPackages.addAll(Arrays.asList(nextLine.trim().split(",")));
                    }
                    break;

                }
            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return exportedPackages;
        }
    }

    private List<String> findImportedPackages() {
        importedPackages = new ArrayList<String>();
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(new File(getManifest().getFullyQualifiedName()), "r");
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(Constants.Manifest.IMPORT_PACKAGE)) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                    if (!"".equals(line)) {
                        importedPackages.addAll(Arrays.asList(line.split(",")));
                    }
                    //try to get packages from next lines
                    String nextLine;
                    while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")) {
                        importedPackages.addAll(Arrays.asList(nextLine.trim().split(",")));
                    }
                    break;

                }
            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return importedPackages;
        }
    }

    private List<String> findRequiredBundles() {
        requiredBundles = new ArrayList<String>();
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(new File(getManifest().getFullyQualifiedName()), "r");
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(Constants.Manifest.REQUIRE_BUNDLE)) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                    if (!"".equals(line)) {
                        requiredBundles.addAll(Arrays.asList(line.split(",")));
                    }
                    //try to get required bundles from next lines
                    String nextLine;
                    while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")) {
                        requiredBundles.addAll(Arrays.asList(nextLine.trim().split(",")));
                    }
                    break;

                }
            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return requiredBundles;
        }
    }

    /**
     * <code>true</code> if exported packages by the bundle are only exporting interfaces
     * <code>false</code> if the bumdle is exporting one or more classes in exported packages
     */
    private Boolean publishedInterfaces() {
        if (getExportedPackages().isEmpty()) {
            return true;//no exported packages means the module is not publishing packages with implementation details
        }

        for (String exportedPackage : getExportedPackages()) {
            String exportedPackagePath = exportedPackage.trim().replaceAll("\\.", "/");
            if (!exportedPackagePath.startsWith("/")) {
                exportedPackagePath = "/" + exportedPackagePath;
            }
            try {
                //get .java files under exported packages
                List<Resource<?>> resources = ProjectUtils.getProjectSourcePath(projectRoot) != null ? ProjectUtils.getProjectSourcePath(projectRoot).getChild(exportedPackagePath).listResources(new ResourceFilter() {
                    @Override
                    public boolean accept(Resource<?> resource) {
                        return resource.getName().endsWith(".java");
                    }
                }) : null;

                if (resources != null && !resources.isEmpty()) {
                    for (Resource<?> resource : resources) {
                        if (!ProjectUtils.isInterface((FileResource<?>) resource)) {
                            return false;
                        }
                    }
                }
            } catch (ResourceException ex) {
                //log ex
            }

        }
        //all exported packages contains only interfaces
        return true;

    }

    public List<Resource<?>> findStaleReferences() {
        staleReferences = new ArrayList<Resource<?>>();

        searchStaleReferences(staleReferences, (DirectoryResource) ProjectUtils.getProjectSourcePath(projectRoot));

        return staleReferences;
    }

    private void searchStaleReferences(List<Resource<?>> staleReferences, DirectoryResource root) {
        for (Resource<?> child : root.listResources()) {
            if (child instanceof DirectoryResource) {
                searchStaleReferences(staleReferences, (DirectoryResource) child);
            } else if (child instanceof FileResource && child.getName().endsWith(".java")) {
                JavaSource source = null;
                try {
                    source = JavaParser.parse(child.getResourceInputStream());
                } catch (ParserException e) {
                    //intentional
                    continue;
                }
                if (source.hasImport("org.osgi.framework.ServiceReference") || source.hasImport("org.osgi.framework")) {
                    if (verifyStaleReference(source)) {
                        staleReferences.add(child);
                    }
                }
            }
        }
    }

    private boolean verifyStaleReference(JavaSource source) {
        CompilationUnit comp = (CompilationUnit) source.getInternal();
        StaleReferencesVisitor staleReferencesVisitor = ASTVisitors.getStaleReferencesVisitor();
        comp.accept(staleReferencesVisitor);
        staleReferencesVisitor.visit(comp);
        return staleReferencesVisitor.getNumGetServices() != staleReferencesVisitor.getNumUngetServices();
    }

    private String findBundleVersion() {
        version = new String();
        RandomAccessFile file = null;
        try {
            file = new RandomAccessFile(new File(getManifest().getFullyQualifiedName()), "r");
            String line;
            while ((line = file.readLine()) != null) {
                if (line.contains(Constants.Manifest.BUNDLE_VERSION)) {
                    line = line.substring(line.indexOf(":") + 1).trim();
                    if (!"".equals(line)) {
                        version = line.substring(line.indexOf(":") + 1).trim();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        } finally {
            if (file != null) {
                try {
                    file.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return version;
        }
    }



    private void calculateNumberOfPackagesClassesAndInterfaces() {
        numberOfAbstractClasses = new Integer(0);
        numberOfClasses = new Integer(0);
        numberOfInterfaces = new Integer(0);
        packages = new HashSet<String>();
        calculateRecursively(ProjectUtils.getProjectSourcePath(projectRoot));
    }

    private void calculateRecursively(Resource<?> root) {
        for (Resource<?> child : root.listResources()) {
            if (child instanceof DirectoryResource) {
                calculateRecursively((DirectoryResource) child);
            } else if (child instanceof FileResource && child.getName().endsWith(".java")) {
                JavaSource source = null;
                try {
                    source = JavaParser.parse(child.getResourceInputStream());
                } catch (ParserException e) {
                    //intentional
                    continue;
                }
                packages.add(source.getPackage());
                if (source.isInterface()) {
                    numberOfInterfaces++;
                } else if (source.isClass()) {
                    if (isAbstractClass(child)) {
                        numberOfAbstractClasses++;
                    } else {
                        numberOfClasses++;
                    }
                }

            }
        }
    }

    private boolean isAbstractClass(Resource<?> aClass) {
        RandomAccessFile aFile = null;
        try {
             aFile = new RandomAccessFile(new File(aClass.getFullyQualifiedName()),"r");
            String line;
            while((line = aFile.readLine())!=null){
                if(line.contains("abstract class")){
                    return true;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(aFile != null){
                try {
                    aFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
       return false;
    }

    //getters

    public Boolean getUsesDeclarativeServices() {
        if (usesDeclarativeServices == null) {
            usesDeclarativeServices = usesDeclarativeServices();
        }
        return usesDeclarativeServices;
    }

    public Boolean getUsesBlueprint() {
        if (usesBlueprint == null) {
            usesBlueprint = usesBlueprint();
        }
        return usesBlueprint;
    }

    @Override
    public FileResource<?> getActivator() {
        if (activator == null) {
            activator = findActivator();
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


    public Long getLinesOfCode() {
        if (totalLoc == null) {
            totalLoc = new Long(0L);
            Resource<?> srcPath = ProjectUtils.getProjectSourcePath(projectRoot);
            if (srcPath != null && srcPath.exists()) {
                totalLoc = countModuleLines((DirectoryResource) srcPath);
            }
        }
        return totalLoc;
    }

    public List<String> getExportedPackages() {
        if (exportedPackages == null) {
            exportedPackages = findExportedPackages();
        }
        return exportedPackages;
    }

    @Override
    public List<String> getRequiredBundles() {
        if (requiredBundles == null) {
            requiredBundles = findRequiredBundles();
        }
        return requiredBundles;
    }


    public List<String> getImportedPackages() {
        if (importedPackages == null) {
            importedPackages = findImportedPackages();
        }
        return importedPackages;
    }

    public Boolean getPublishesInterfaces() {
        if (publishesInterfaces == null) {
            publishesInterfaces = publishedInterfaces();
        }
        return publishesInterfaces;
    }

    @Override
    public Boolean getDeclaresPermissions() {
        if (declaresPermissions == null) {
            declaresPermissions = declaresPermissions();
        }
        return declaresPermissions;
    }

    @Override
    public List<Resource<?>> getStaleReferences() {
        if (staleReferences == null) {
            staleReferences = findStaleReferences();
        }
        return staleReferences;
    }

    @Override
    public String getVersion() {
        if (version == null) {
            version = findBundleVersion();
        }
        return version;
    }

    @Override
    public Set<String> getPackages() {
        if (packages == null) {
            calculateNumberOfPackagesClassesAndInterfaces();
        }
        return packages;
    }


    public Long getLinesOfTestCode() {
        if (totalTestLoc == null) {
            totalTestLoc = 0L;
            Resource<?> testPath = ProjectUtils.getProjectTestPath(projectRoot);
            if (testPath != null && testPath.exists()) {
                totalTestLoc = countModuleTestLines((DirectoryResource) testPath);
            }
        }
        return totalTestLoc;
    }

    public Integer getNumberOfClasses() {
        if (numberOfClasses == null) {
            calculateNumberOfPackagesClassesAndInterfaces();
        }
        return numberOfClasses;
    }

    public Integer getNumberOfAbstractClasses() {
        if (numberOfAbstractClasses == null) {
            calculateNumberOfPackagesClassesAndInterfaces();
        }
        return numberOfAbstractClasses;
    }

    public Integer getNumberOfInterfaces() {
        if (numberOfInterfaces == null) {
            calculateNumberOfPackagesClassesAndInterfaces();
        }
        return numberOfInterfaces;
    }
}
