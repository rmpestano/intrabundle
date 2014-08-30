package br.ufrgs.rmpestano.intrabundle.util;

import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModuleImpl;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProjectImpl;
import org.jboss.forge.project.services.ResourceFactory;
import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 3/17/14.
 */
@Singleton
public class BundleFinder implements Serializable {

    @Inject
    private ResourceFactory resourceFactory;

    @Inject
    private ProjectUtils projectUtils;


    public OSGiProject scan(DirectoryResource root, Integer levels) {

        List<OSGiModule> modules = new ArrayList<OSGiModule>();
        findModulesRecursively(root, modules, levels);

        if (modules == null || modules.isEmpty()) {
            return null;
        } else {

            OSGiProjectImpl project = new OSGiProjectImpl(null, resourceFactory, root,projectUtils);
            project.setModules(modules);
            return project;
        }
    }


    private void findModulesRecursively(Resource<?> root, List<OSGiModule> modules, Integer level) {
        if (level-- == 0) {
            return;
        }

        List<Resource<?>> children = root.listResources(new ResourceFilter() {
            @Override
            public boolean accept(Resource<?> resource) {
                return resource instanceof DirectoryResource;
            }
        });

        if (children != null && !children.isEmpty()) {
            for (Resource<?> child : children) {
                DirectoryResource directoryResource = child.reify(DirectoryResource.class);
                if (directoryResource != null && projectUtils.isOsgiBundle(directoryResource)) {
                    modules.add(new OSGiModuleImpl(null, resourceFactory, directoryResource));
                } else {
                    findModulesRecursively(child, modules, level);
                }
            }
        }
    }
}
