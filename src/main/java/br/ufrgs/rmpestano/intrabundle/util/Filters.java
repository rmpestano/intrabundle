package br.ufrgs.rmpestano.intrabundle.util;

import org.jboss.forge.resources.DirectoryResource;
import org.jboss.forge.resources.Resource;
import org.jboss.forge.resources.ResourceFilter;

/**
 * Created by rmpestano on 8/16/14.
 */
public class Filters {

    public static final ResourceFilter BND_FILTER = new ResourceFilter() {
        @Override
        public boolean accept(Resource<?> resource) {
            return (resource instanceof DirectoryResource == false) && resource.getName().toLowerCase().endsWith(".bnd");
        }
    };

    public static final ResourceFilter MANIFEST_FILTER = new ResourceFilter() {
        @Override
        public boolean accept(Resource<?> resource) {
            return (resource instanceof DirectoryResource == false) && resource.getName().toLowerCase().endsWith(".mf");
        }
    };

}
