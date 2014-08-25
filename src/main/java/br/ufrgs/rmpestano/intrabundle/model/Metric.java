package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.model.enums.MetricName;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;

import java.io.Serializable;

/**
 * Created by RAFAEL-PESTANO on 25/08/2014.
 */
public class Metric implements Serializable {

  private MetricName name;
  private MetricScore score;

  public Metric(MetricName name, MetricScore score) {
    this.name = name;
    this.score = score;
  }

  public MetricName getName() {
    return name;
  }

  public void setName(MetricName name) {
    this.name = name;
  }

  public MetricScore getScore() {
    return score;
  }

  public void setScore(MetricScore score) {
    this.score = score;
  }
}
