package br.ufrgs.rmpestano.intrabundle.util;

/**
 * Created by rmpestano on 2/9/14.
 */

public interface Constants {

    public static interface Manifest{
        public static final String DECLARATIVE_SERVICES = "Service-Component";
        public static final String BLUE_PRINT = "Bundle-Blueprint";
        public static final String EXPORT_PACKAGE = "Export-Package";
        public static final String IMPORT_PACKAGE = "Import-Package";
        public static final String PRIVATE_PACKAGE = "Private-Package";
        public static final String REQUIRE_BUNDLE = "Require-Bundle";
        public static final String ACTIVATOR = "Bundle-Activator";
        public static final String SYMBOLIC_NAME = "Bundle-SymbolicName";
        public static final String BUNDLE_VERSION = "Bundle-Version";
    }

    public static interface BND {
        public static final String MAVEN_BUNDLE_PLUGIN = "maven-bundle-plugin";
    }
    public static interface IPOJO {
        public static final String COMPONENT_IMPORT = "org.apache.felix.ipojo.annotations.Component";
        public static final String COMPONENT_IMPORT_WITH_WILDCARD = "org.apache.felix.ipojo.annotations.*";
    }
}
