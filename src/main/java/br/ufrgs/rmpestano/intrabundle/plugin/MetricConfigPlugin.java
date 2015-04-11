package br.ufrgs.rmpestano.intrabundle.plugin;

import java.util.*;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;
import javax.inject.Singleton;

import br.ufrgs.rmpestano.intrabundle.event.MetricsConfigEvent;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricName;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import org.jboss.forge.shell.ShellColor;
import org.jboss.forge.shell.ShellPrompt;
import org.jboss.forge.shell.plugins.*;

import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;

/**
 *
 */
@Alias("metric-config")
@Singleton
public class MetricConfigPlugin implements Plugin {

    @Inject
    private ShellPrompt prompt;

    @Inject
    MessageProvider provider;

    List<MetricName> configurableMetrics;

    @Inject

    Event<MetricsConfigEvent> metricsConfigEvent;

    /**
     * each metric has its configurable limit, ex:
     * BUNDLE_DEPENDENCY -> STATE_OF_ART:0. VERY_GOOD: 3 and so on
     */
    Map<MetricName, Map<MetricScore, Double>> metricsLimit;

    @PostConstruct
    public void init() {
        metricsLimit = new HashMap<MetricName, Map<MetricScore, Double>>();
        metricsLimit.put(MetricName.BUNDLE_DEPENDENCIES, new HashMap<MetricScore, Double>());
        metricsLimit.put(MetricName.CYCLE, new HashMap<MetricScore, Double>());
        metricsLimit.put(MetricName.STALE_REFERENCES, new HashMap<MetricScore, Double>());

        //fixed vlues metrics
        metricsLimit.put(MetricName.PUBLISHES_INTERFACES, new HashMap<MetricScore, Double>(){
            {put(MetricScore.STATE_OF_ART,1D);};
            {put(MetricScore.REGULAR,0D);};
        });
        metricsLimit.put(MetricName.DECLARES_PERMISSION, new HashMap<MetricScore, Double>(){
            {put(MetricScore.STATE_OF_ART,1D);};
            {put(MetricScore.REGULAR,0D);};
        });
        metricsLimit.put(MetricName.USES_FRAMEWORK, new HashMap<MetricScore, Double>(){
            {put(MetricScore.STATE_OF_ART,1D);};
            {put(MetricScore.REGULAR,0D);};
        });


        configurableMetrics = new ArrayList<MetricName>();

        configurableMetrics.add(MetricName.BUNDLE_DEPENDENCIES);
        configurableMetrics.add(MetricName.STALE_REFERENCES);
        configurableMetrics.add(MetricName.CYCLE);

        loadDefaults(null, null);
    }

    @DefaultCommand
    public void config(PipeOut out) {
        MetricName metric = prompt.promptChoiceTyped(provider.getMessage("metrics.config"), configurableMetrics);
        Map<MetricScore, Double> limits = metricsLimit.get(metric);
        for (MetricScore metricScore : limits.keySet()) {
            Double currentValue  =metricsLimit.get(metric).get(metricScore);
           Double newValue =  prompt.prompt(provider.getMessage("metrics.config-value",metricScore.name(),currentValue), Double.class, currentValue);
           limits.put(metricScore, newValue);
        }

        out.println(ShellColor.YELLOW, provider.getMessage("metrics.config-new-value", metric.name()));
        for (MetricScore metricScore : limits.keySet()) {
            out.println(metricScore.name() + ": " + limits.get(metricScore));
        }

        metricsConfigEvent.fire(new MetricsConfigEvent(metricsLimit));
    }

    @Command(value = "loadDefaults")
    public void loadDefaults(@PipeIn String in, PipeOut out) {
        configDependenciesMetric();
        configCyclesMetric();
        configStaleReferencesMetric();
        if(out != null){
           print(in,out);
        }
        metricsConfigEvent.fire(new MetricsConfigEvent(metricsLimit));
    }


    @Command(value = "printLimits")
    public void print(@PipeIn String in, PipeOut out) {
        if(out != null){
            out.println(ShellColor.YELLOW, provider.getMessage("metrics.config-print"));
            for (MetricName metricName : metricsLimit.keySet()) {
                out.println(ShellColor.YELLOW, provider.getMessage(metricName.getValue()) + ":");
                for (Map.Entry<MetricScore, Double> metricScore : metricsLimit.get(metricName).entrySet()) {
                    out.println(metricScore.getKey().name()+":"+metricScore.getValue());
                }
            }
        }
    }


    private void configDependenciesMetric() {
        Map<MetricScore, Double> bundleDependenciesLimit =  metricsLimit.get(MetricName.BUNDLE_DEPENDENCIES);
        // not depending on others modules is state of art by default
        bundleDependenciesLimit.put(MetricScore.STATE_OF_ART,0D);
        bundleDependenciesLimit.put(MetricScore.VERY_GOOD,3D);
        bundleDependenciesLimit.put(MetricScore.GOOD,5D);
        bundleDependenciesLimit.put(MetricScore.REGULAR,9D);
        bundleDependenciesLimit.put(MetricScore.ANTI_PATTERN, 10D);
    }

    private void configStaleReferencesMetric() {
        Map<MetricScore, Double> bundleDependenciesLimit =  metricsLimit.get(MetricName.STALE_REFERENCES);
        // no  stale references is state of art by default
        bundleDependenciesLimit.put(MetricScore.STATE_OF_ART,0.0);
        bundleDependenciesLimit.put(MetricScore.VERY_GOOD,0.05);
        bundleDependenciesLimit.put(MetricScore.GOOD,0.10);
        bundleDependenciesLimit.put(MetricScore.REGULAR,0.15);
        bundleDependenciesLimit.put(MetricScore.ANTI_PATTERN,0.15);
    }

    private void configCyclesMetric() {
        Map<MetricScore, Double> bundleDependenciesLimit =  metricsLimit.get(MetricName.CYCLE);
        bundleDependenciesLimit.put(MetricScore.STATE_OF_ART,0D);
        bundleDependenciesLimit.put(MetricScore.VERY_GOOD,3D);
        bundleDependenciesLimit.put(MetricScore.GOOD,5D);
        bundleDependenciesLimit.put(MetricScore.REGULAR,9D);
        bundleDependenciesLimit.put(MetricScore.ANTI_PATTERN,10D);
    }


    @Produces
    @ApplicationScoped
    //@RequestScoped cannot be request because its used during report generation and there is no request context there
    public Map<MetricName, Map<MetricScore, Double>> getMetricsLimit(){
        return metricsLimit;
    }
}
