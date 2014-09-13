package br.ufrgs.rmpestano.intrabundle.metric;

import br.ufrgs.rmpestano.intrabundle.model.MetricPoints;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;
import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;
import br.ufrgs.rmpestano.intrabundle.model.Metric;
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
    Metric getLocMetric(OSGiModule bundle);


    /**
     * this metric is based on bundle dependencies
     * its based on the fact that the less bundle it
     * depends the less coupled it is
     *
     * @param bundle
     * @return
     */
    Metric getBundleDependencyMetric(OSGiModule bundle);


    Metric getPublishesInterfaceMetric(OSGiModule bundle);

    /**
     * verifies if bundle uses a framework to manage services lifecycle, frameworks being tracker are:
     * declarativeServices, bluePrint and ipojo
     *
     * @param bundle
     * @return Metric#STATE_OF_ART if use a framework,  Metric#REGULAR if no framework is used
     */
    Metric usesFrameworkToManageServicesMetric(OSGiModule bundle);

    Metric hasStaleReferencesMetric(OSGiModule bundle);

    Metric getDeclaresPermissionMetric(OSGiModule bundle);

    OSGiProject getCurrentOSGiProject();

    MetricPoints calculateBundleMetric(OSGiModule bundle);

    /**
     * get average project metric score
     * @param osGiProject
     * @return
     */
    MetricScore calculateProjectMetric(OSGiProject osGiProject);

    /**
     * get average project metric score on current OSGiProject
     * @return
     */
    MetricScore calculateProjectMetric();

}
