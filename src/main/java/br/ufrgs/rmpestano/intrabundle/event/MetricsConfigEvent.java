package br.ufrgs.rmpestano.intrabundle.event;

import br.ufrgs.rmpestano.intrabundle.model.enums.MetricName;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;

import java.util.Map;

/**
 * Created by rafael-pestano on 08/04/2015.
 */
public class MetricsConfigEvent {

  private Map<MetricName, Map<MetricScore, Double>> metricsLimit;

  public MetricsConfigEvent(Map<MetricName, Map<MetricScore, Double>> metricsLimit) {
    this.metricsLimit = metricsLimit;
  }

  public Map<MetricName, Map<MetricScore, Double>> getMetricsLimit() {
    return metricsLimit;
  }



}
