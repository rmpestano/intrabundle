package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;

/**
 * Created by rmpestano on 8/18/14.
 */
public interface Metrics {

    /**
     * this metric is based on bundle lines of code
     * its based on the fact that the less lines of code
     * the more cohesive the bundle is
     * @param bundle
     * @return
     */
    public MetricScore getLocMetric(OSGiModule bundle);


  /**
   * this metric is based on bundle dependencies
   * its based on the fact that the less bundle it
   * depends the less coupled it is
   * @param bundle
   * @return
   */
    public MetricScore getBundleDependencyMetric(OSGiModule bundle);


    public OSGiProject getCurrentOSGiProject();
}
