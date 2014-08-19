package br.ufrgs.rmpestano.intrabundle.metric;

import br.ufrgs.rmpestano.intrabundle.model.MetricPoints;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;

/**
 * Created by rmpestano on 8/18/14.
 */
public interface Metrics {

    /**
     * this metric is based on bundle lines of code
     * its based on the fact that the less lines of code
     * the more cohesive the bundle is
     *
     * @param bundle
     * @return
     */
    MetricScore getLocMetric(OSGiModule bundle);


    /**
     * this metric is based on bundle dependencies
     * its based on the fact that the less bundle it
     * depends the less coupled it is
     *
     * @param bundle
     * @return
     */
    MetricScore getBundleDependencyMetric(OSGiModule bundle);


    MetricScore getPublishesInterfaceMetric(OSGiModule bundle);

    /**
     * verifies if bundle uses a framework to manage services lifecycle, frameworks being tracker are:
     * declarativeServices, bluePrint and ipojo
     *
     * @param bundle
     * @return MetricScore#STATE_OF_ART if use a framework,  MetricScore#REGULAR if no framework is used
     */
    MetricScore usesFrameworkToManageServicesMetric(OSGiModule bundle);

    MetricScore hasStaleReferencesMetric(OSGiModule bundle);

    MetricScore getDeclaresPermissionMetric(OSGiModule bundle);

    OSGiProject getCurrentOSGiProject();

    MetricPoints calculateBundleMetric(OSGiModule bundle);
}
