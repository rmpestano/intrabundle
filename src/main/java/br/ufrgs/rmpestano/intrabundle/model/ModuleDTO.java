package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.metric.Metrics;

import java.io.Serializable;

/**
 * Created by rmpestano on 2/4/14.
 * used by jasper reports
 */
public class ModuleDTO implements Serializable {

    protected OSGiModule module;
    protected MetricPoints metricsPoints;

    public ModuleDTO() {
    }

    public ModuleDTO(OSGiModule module, Metrics metrics) {
        module.getNumberOfClasses();//forces visitAllClasses to be available in report as its called on demand
        module.getManifestMetadata();//forces createManifestMetadata to be available in report
        this.module = module;
        metricsPoints = metrics.calculateBundleMetric(module);
    }

    public OSGiModule getModule() {
        return module;
    }

    public void setModule(OSGiModule module) {
        this.module = module;
    }

    public MetricPoints getMetricsPoints() {
        return metricsPoints;
    }

    public void setMetricsPoints(MetricPoints metricsPoints) {
        this.metricsPoints = metricsPoints;
    }
}
