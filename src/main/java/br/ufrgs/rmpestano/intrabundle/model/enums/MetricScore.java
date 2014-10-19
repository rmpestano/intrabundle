package br.ufrgs.rmpestano.intrabundle.model.enums;

/**
 * Created by rmpestano on 8/17/14.
 */
public enum MetricScore {
    ANTI_PATTERN(0), REGULAR(5), GOOD(10), VERY_GOOD(15), STATE_OF_ART(20);

    private int value;

    private MetricScore(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    @Override
    public String toString() {
        return name() +"("+getValue()+")";
    }
}
