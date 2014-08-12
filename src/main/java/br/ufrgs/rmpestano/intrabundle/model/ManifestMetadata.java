package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.util.Constants;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.FileResource;
import org.jboss.forge.resources.Resource;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by rmpestano on 7/13/14.
 * <p/>
 * gather information from Manifest or BND file
 */
public class ManifestMetadata implements Serializable {

    private List<String> importedPackages;
    private List<String> exportedPackages;
    private List<String> requiredBundles;
    private FileResource<?> activator;
    private String version;
    private DirectoryResource projectRoot;
    private ResourceFactory resourceFactory;
    private Boolean usesBlueprint;
    private Boolean usesDeclarativeServices;


    public ManifestMetadata(FileResource<?> manifestSource, DirectoryResource projectRoot, ResourceFactory resourceFactory) {
        if (manifestSource == null) {
            throw new RuntimeException("provide source of manifest metadata such as manisfest or bnd file");
        }
        this.resourceFactory = resourceFactory;
        this.projectRoot = projectRoot;

        this.readManifest(manifestSource);


    }

    public List<String> getImportedPackages() {
        return importedPackages;
    }

    public List<String> getExportedPackages() {
        return exportedPackages;
    }

    public List<String> getRequiredBundles() {
        return requiredBundles;
    }

    public FileResource<?> getActivator() {
        return activator;
    }

    public String getVersion() {
        return version;
    }

    public Boolean getUsesBlueprint() {
        return usesBlueprint;
    }

    public Boolean getUsesDeclarativeServices() {
        return usesDeclarativeServices;
    }

    private void readManifest(FileResource<?> source) {
        RandomAccessFile randomAccessFile = null;
        try {
            File f = new File(source.getFullyQualifiedName());
            randomAccessFile = new RandomAccessFile(f, "r");
            this.initActivator(randomAccessFile);
            randomAccessFile.seek(0);//rewind
            this.initImportedPackages(randomAccessFile);
            randomAccessFile.seek(0);//rewind
            this.initExportedPackages(randomAccessFile);
            randomAccessFile.seek(0);
            this.initRequiredBundles(randomAccessFile);
            randomAccessFile.seek(0);
            this.initVersion(randomAccessFile);
            randomAccessFile.seek(0);
            this.initBluePrint(randomAccessFile);
            randomAccessFile.seek(0);
            initDeclarativeServices(randomAccessFile);
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
    }

    private void initDeclarativeServices(RandomAccessFile manifest) {
        try {
            //look into stardard location
            Resource<?> resourcesDir = ProjectUtils.getProjectResourcesPath(projectRoot);
            if (resourcesDir != null && resourcesDir.getChild("OSGI-INF").exists() && resourcesDir.getChild("OSGI-INF").getChild("services.xml").exists()) {
                usesDeclarativeServices = Boolean.TRUE;
                return;
            }
            String line;
            while ((line = manifest.readLine()) != null) {
                if (line.contains(Constants.Manifest.DECLARATIVE_SERVICES)) {
                    usesDeclarativeServices = Boolean.TRUE;
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        usesDeclarativeServices = Boolean.FALSE;
    }

    private void initBluePrint(RandomAccessFile manifest) {
        try {
            //look into stardard location
            Resource<?> resourcesDir = ProjectUtils.getProjectResourcesPath(projectRoot);
            if (resourcesDir != null && resourcesDir.getChild("OSGI-INF").exists() && resourcesDir.getChild("OSGI-INF").getChild("blueprint").exists()) {
                usesBlueprint = Boolean.TRUE;
                return;
            }
            String line;
            while ((line = manifest.readLine()) != null) {
                if (line.contains(Constants.Manifest.BLUE_PRINT)) {
                    usesBlueprint = Boolean.TRUE;
                    return;
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        usesBlueprint = Boolean.FALSE;
    }

    private void initVersion(RandomAccessFile file) {
        version = new String();
        try {
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
        }
    }

    private void initRequiredBundles(RandomAccessFile file) {
        requiredBundles = new ArrayList<String>();
        try {
            String line;
            while ((line = file.readLine()) != null) {

                if (ProjectUtils.isMavenBndProject(projectRoot)) {
                    //read OSGi metadata from pom.xml in maven bundle plugin
                    if (line.trim().startsWith(Constants.BND.REQUIRE_BUNDLE)) {
                        line = line.substring(line.indexOf(">") + 1);
                        if (line.contains("</")) {
                            line = line.substring(0, line.indexOf("</"));
                        }
                        if (!"".equals(line)) {
                            requiredBundles.addAll(Arrays.asList(line.split(",")));
                        }
                        String nextLine;
                        //try to get packages from next lines
                        while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains("</Required-Bundle>")) {
                            requiredBundles.addAll(Arrays.asList(nextLine.trim().split(",")));
                        }
                        if (nextLine != null && nextLine.contains("</") && !nextLine.startsWith("</")) {
                            requiredBundles.addAll(Arrays.asList(nextLine.trim().substring(0, nextLine.trim().indexOf("</")).split(",")));
                        }
                        break;
                    }
                } else { //read OSGi metadata from MANIFEST.MF

                    if (line.contains(Constants.Manifest.REQUIRE_BUNDLE)) {
                        if (ProjectUtils.isMavenBndProject(projectRoot) && line.contains(">")) {

                        } else if (line.contains(":")) {
                            line = line.substring(line.indexOf(":") + 1).trim();
                        }
                        if (!"".equals(line)) {
                            requiredBundles.addAll(Arrays.asList(line.split(",")));
                        }
                        //try to get packages from next lines
                        String nextLine;
                        while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains(":")) {
                            requiredBundles.addAll(Arrays.asList(nextLine.trim().split(",")));
                        }
                        break;

                    }
                }

            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        }
    }

    private void initImportedPackages(RandomAccessFile file) {
        importedPackages = new ArrayList<String>();
        try {
            String line;
            while ((line = file.readLine()) != null) {

                if (ProjectUtils.isMavenBndProject(projectRoot)) {
                    //read OSGi metadata from pom.xml in maven bundle plugin
                    if (line.trim().startsWith(Constants.BND.IMPORT_PACKAGE)) {
                        line = line.substring(line.indexOf(">") + 1);
                        if (line.contains("</")) {
                            line = line.substring(0, line.indexOf("</"));
                        }
                        if (!"".equals(line)) {
                            importedPackages.addAll(Arrays.asList(line.split(",")));
                        }
                        String nextLine;
                        //try to get packages from next lines
                        while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains("</Import-Package>")) {
                            importedPackages.addAll(Arrays.asList(nextLine.trim().split(",")));
                        }
                        if (nextLine != null && nextLine.contains("</") && !nextLine.startsWith("</")) {
                            importedPackages.addAll(Arrays.asList(nextLine.trim().substring(0, nextLine.trim().indexOf("</")).split(",")));
                        }
                        break;
                    }
                } else { //read OSGi metadata from MANIFEST.MF

                    if (line.contains(Constants.Manifest.IMPORT_PACKAGE)) {
                        if (ProjectUtils.isMavenBndProject(projectRoot) && line.contains(">")) {

                        } else if (line.contains(":")) {
                            line = line.substring(line.indexOf(":") + 1).trim();
                        }
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

            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        }

    }

    private void initExportedPackages(RandomAccessFile file) {
        exportedPackages = new ArrayList<String>();
        try {
            String line;
            while ((line = file.readLine()) != null) {

                if (ProjectUtils.isMavenBndProject(projectRoot)) {
                    //read OSGi metadata from pom.xml in maven bundle plugin
                    if (line.trim().startsWith(Constants.BND.EXPORT_PACKAGE)) {
                        line = line.substring(line.indexOf(">") + 1);
                        if (line.contains("</")) {
                            line = line.substring(0, line.indexOf("</"));
                        }
                        if (!"".equals(line)) {
                            exportedPackages.addAll(Arrays.asList(line.split(",")));
                        }
                        String nextLine;
                        //try to get packages from next lines
                        while ((nextLine = file.readLine()) != null && !"".equals(nextLine.trim()) && !nextLine.contains("</Export-Package>")) {
                            exportedPackages.addAll(Arrays.asList(nextLine.trim().split(",")));
                        }
                        if (nextLine != null && nextLine.contains("</") && !nextLine.startsWith("</")) {
                            exportedPackages.addAll(Arrays.asList(nextLine.trim().substring(0, nextLine.trim().indexOf("</")).split(",")));
                        }
                        break;
                    }
                } else { //read OSGi metadata from MANIFEST.MF

                    if (line.contains(Constants.Manifest.EXPORT_PACKAGE)) {
                        if (ProjectUtils.isMavenBndProject(projectRoot) && line.contains(">")) {

                        } else if (line.contains(":")) {
                            line = line.substring(line.indexOf(":") + 1).trim();
                        }
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

            }
        } catch (Exception e) {
            //TODO log ex
            e.printStackTrace();
        }
    }

    private void initActivator(RandomAccessFile randomAccessFile) throws IOException {
        try {
            String line;
            while ((line = randomAccessFile.readLine()) != null) {
                if (ProjectUtils.isMavenBndProject(projectRoot)) {
                    if (line.contains("<" + Constants.Manifest.ACTIVATOR + ">") && !line.contains("${")) {
                        while ((line += randomAccessFile.readLine()) != null && !line.contains("</" + Constants.Manifest.ACTIVATOR + ">")) {

                        }
                        line = line.substring(line.indexOf(">") + 1, line.indexOf("</"));
                        break;
                    }
                } else {
                    if (line.contains(Constants.Manifest.ACTIVATOR)) {
                        break;
                    }
                }
            }
            if (line == null) {
                activator = null;//no activator
                return;
            }
            String activatorPath = null;
            if (ProjectUtils.isMavenBndProject(projectRoot)) {
                activatorPath = line.trim();
            } else {
                activatorPath = line.trim().substring(line.indexOf(Constants.Manifest.ACTIVATOR) + 18);
            }
            activatorPath = activatorPath.trim().replaceAll("\\.", "/");
            if (!activatorPath.startsWith("/")) {
                activatorPath = "/" + activatorPath;
            }
            activator = ProjectUtils.getProjectSourcePath(projectRoot) != null ? (FileResource<?>) ProjectUtils.getProjectSourcePath(projectRoot).getChild(activatorPath.concat(".java")) : null;
            if (activator == null || !activator.exists()) {
                //try to infer activator path from projectRoot path
                if (activatorPath.contains(projectRoot.getName())) {
                    String[] activatoPathTokens = activatorPath.split("/");
                    String projectRootPath = projectRoot.getFullyQualifiedName().substring(0, projectRoot.getFullyQualifiedName().indexOf(activatoPathTokens[1]));
                    activator = (FileResource<?>) resourceFactory.getResourceFrom(new File((projectRootPath + activatorPath).replaceAll("//", "/").trim().concat(".java")));

                }
            }
        } catch (Exception e) {
            activator = null;
            System.out.println("could not find activator for project:" + projectRoot.getFullyQualifiedName());
        }
    }


}
