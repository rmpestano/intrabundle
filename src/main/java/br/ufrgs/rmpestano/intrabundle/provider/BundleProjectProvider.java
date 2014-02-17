package br.ufrgs.rmpestano.intrabundle.provider;

import br.ufrgs.rmpestano.intrabundle.facet.BundleFacet;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModuleImpl;
import br.ufrgs.rmpestano.intrabundle.util.ProjectUtils;
import org.jboss.forge.addon.facets.FacetFactory;
import org.jboss.forge.addon.projects.Project;
import org.jboss.forge.addon.projects.ProjectProvider;
import org.jboss.forge.addon.projects.ProvidedProjectFacet;
import org.jboss.forge.addon.resource.DirectoryResource;
import org.jboss.forge.addon.resource.Resource;
import org.jboss.forge.addon.resource.ResourceFilter;

import javax.inject.Inject;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by rmpestano on 2/16/14.
 */
public class BundleProjectProvider implements ProjectProvider {

    @Inject
    private FacetFactory factory;


    @Override
    public String getType() {
        return "Bundle";
    }

    @Override
    public Iterable<Class<? extends ProvidedProjectFacet>> getProvidedFacetTypes() {
        Set<Class<? extends ProvidedProjectFacet>> result = new HashSet<Class<? extends ProvidedProjectFacet>>();
        result.add(BundleFacet.class);
        return Collections.unmodifiableSet(result);
    }

    @Override
    public Project createProject(final DirectoryResource dir)
    {
        Project project = new OSGiModuleImpl(dir);

        try
        {
            factory.install(project, BundleFacet.class);
        }
        catch (RuntimeException e)
        {
            throw new IllegalStateException("Could not install Bundle into Project located at ["
                    + dir.getFullyQualifiedName() + "]", e);
        }

        return project;
    }

    @Override
    public boolean containsProject(DirectoryResource directoryResource) {
        List<Resource<?>> manifestList = directoryResource.listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource.getName().toLowerCase().endsWith(".mf");
            }
        });
        for (Resource<?> resource : manifestList) {
            if(ProjectUtils.isOSGiManifest(resource)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public int priority() {
        return 0;
    }


}
