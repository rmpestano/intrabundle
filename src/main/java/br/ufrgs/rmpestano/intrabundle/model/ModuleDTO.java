package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.metric.MetricsCalculation;

import java.io.Serializable;

/**
 * Created by rmpestano on 2/4/14.
 * used by jasper reports
 */
public class ModuleDTO implements Serializable {

    protected OSGiModule module;
    protected MetricPoints metricPoints;

    public ModuleDTO() {
    }

    public ModuleDTO(OSGiModule module, MetricsCalculation metrics) {
        module.getNumberOfClasses();//forces visitAllClasses to be available in report as its called on demand
        module.getManifestMetadata();//forces createManifestMetadata to be available in report
        this.module = module;
        metricPoints = metrics.calculateBundleQuality(module);
    }

    public OSGiModule getModule() {
        return module;
    }

    public void setModule(OSGiModule module) {
        this.module = module;
    }

    public MetricPoints getMetricPoints() {
        return metricPoints;
    }

    public void setMetricPoints(MetricPoints metricsPoints) {
        this.metricPoints = metricsPoints;
    }
}
