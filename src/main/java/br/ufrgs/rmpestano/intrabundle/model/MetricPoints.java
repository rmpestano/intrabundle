package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;

/**
 * Created by RAFAEL-PESTANO on 18/08/2014.
 */
public class MetricPoints {

  private MetricScore finalScore;
  private int bundlePoints;
  private int maxPoints;
  private int numberOfMetrics;

  public MetricPoints(int bundlePoints, int maxPoints, int numberOfMetrics) {
    this.bundlePoints = bundlePoints;
    this.maxPoints = maxPoints;
    this.numberOfMetrics = numberOfMetrics;
  }

  public MetricScore getFinalScore() {
    if(finalScore == null){
      calculateFinalScore();
    }
    return finalScore;
  }

  /**
   * Final score is based on the following rules:
   *  STATE_OF_ART: bundlePoints >= maxPoints * 0.9
   *  VERY_GOOD: bundlePoints >= maxPoints * 0.80 and < maxPoints * 0.9
   *  GOOD: bundlePoints >= maxPoints * 0.65 and < maxPoints * 0.8
   *  REGULAR: bundlePoints >= maxPoints * 0.50 and < maxPoints * 0.65
   *  ANTI_PATTERN: bundlePoints < maxPoints * 0.50
   */
  private void calculateFinalScore() {
    if(bundlePoints >= maxPoints *0.9){
      finalScore = MetricScore.STATE_OF_ART;
    }else if(bundlePoints >= maxPoints*0.8){
      finalScore = MetricScore.VERY_GOOD;
    } else if(bundlePoints >= maxPoints*0.65){
      finalScore = MetricScore.GOOD;
    }else if(bundlePoints >= maxPoints*0.5){
      finalScore = MetricScore.REGULAR;
    } else{
      finalScore = MetricScore.ANTI_PATTERN;
    }
  }

  public int getBundlePoints() {
    return bundlePoints;
  }

  public int getMaxPoints() {
    return maxPoints;
  }

  public int getNumberOfMetrics() {
    return numberOfMetrics;
  }
}
