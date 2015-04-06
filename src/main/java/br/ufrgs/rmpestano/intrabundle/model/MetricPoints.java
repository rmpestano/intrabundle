package br.ufrgs.rmpestano.intrabundle.model;

import br.ufrgs.rmpestano.intrabundle.model.enums.MetricName;
import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * Created by RAFAEL-PESTANO on 18/08/2014.
 */
public class MetricPoints {

    private MetricScore finalScore;
    private int bundlePoints;  //TODO rename 'Bundle' to 'Quality'
    private int maxPoints;
    private List<Metric> bundleMetrics;
    private Double compliance;

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

    public List<Metric> getBundleMetrics() {
        return bundleMetrics;
    }

    public Metric getMetric(MetricName name){
        for (Metric bundleMetric : bundleMetrics) {
            if(bundleMetric.getName().equals(name)){
                return bundleMetric;
            }
        }
        return null;
    }

    public Double getCompliance() {
        if(compliance == null){
            compliance = calculateCompliance();
        }
        return compliance;
    }

    private double calculateCompliance() {
        BigDecimal bd = new BigDecimal(bundlePoints / (double) maxPoints);
        bd = bd.setScale(3, RoundingMode.HALF_UP);
        return bd.doubleValue() * 100;
    }
}
