package br.ufrgs.rmpestano.intrabundle.model.enums;

/**
 * Created by rmpestano on 8/17/14.
 */
public enum MetricScore implements Comparable<MetricScore>{
    ANTI_PATTERN(1), REGULAR(2), GOOD(3), VERY_GOOD(4), STATE_OF_ART(5);

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
