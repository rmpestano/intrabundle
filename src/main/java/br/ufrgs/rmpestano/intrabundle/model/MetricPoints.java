package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;

import java.util.List;

/**
 * Created by RAFAEL-PESTANO on 18/08/2014.
 */
public class MetricPoints {

    private MetricScore finalScore;
    private int bundlePoints;
    private int maxPoints;
    private List<Metric> bundleMetrics;

    public MetricPoints(List<Metric> bundleMetrics) {
        this.bundleMetrics = bundleMetrics;
        int numMetrics = bundleMetrics.size();
        maxPoints = numMetrics * MetricScore.STATE_OF_ART.getValue();
        bundlePoints = calculateBundlePoints();
    }

    private int calculateBundlePoints() {
        int totalPoints = 0;
        for (Metric bundleMetric : bundleMetrics) {
            totalPoints += bundleMetric.getScore().getValue();
        }

        return totalPoints;

    }

    public MetricScore getFinalScore() {
        if (finalScore == null) {
            calculateFinalScore();
        }
        return finalScore;
    }

    /**
     * Final score is based on the following rules:
     * STATE_OF_ART: bundlePoints >= maxPoints * 0.9
     * VERY_GOOD: bundlePoints >= maxPoints * 0.75 and < maxPoints * 0.9
     * GOOD: bundlePoints >= maxPoints * 0.60 and < maxPoints * 0.75
     * REGULAR: bundlePoints >= maxPoints * 0.40 and < maxPoints * 0.60
     * ANTI_PATTERN: bundlePoints < maxPoints * 0.40
     */
    private void calculateFinalScore() {
        if (bundlePoints >= maxPoints * 0.9) {
            finalScore = MetricScore.STATE_OF_ART;
        } else if (bundlePoints >= maxPoints * 0.75) {
            finalScore = MetricScore.VERY_GOOD;
        } else if (bundlePoints >= maxPoints * 0.60) {
            finalScore = MetricScore.GOOD;
        } else if (bundlePoints >= maxPoints * 0.4) {
            finalScore = MetricScore.REGULAR;
        } else {
            finalScore = MetricScore.ANTI_PATTERN;
        }
    }

    public int getBundlePoints() {
        return bundlePoints;
    }

    public int getMaxPoints() {
        return maxPoints;
    }

}
