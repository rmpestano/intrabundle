package br.ufrgs.rmpestano.intrabundle.model.enums;

/**
 * Created by rmpestano on 8/17/14.
 */
public enum MetricScore {
    ANTI_PATTERN(-5), REGULAR(0), GOOD(5), VERY_GOOD(10), STATE_OF_ART(20);

    private int value;

    private MetricScore(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
