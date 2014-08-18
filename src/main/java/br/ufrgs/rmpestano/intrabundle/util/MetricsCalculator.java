package br.ufrgs.rmpestano.intrabundle.util;

import br.ufrgs.rmpestano.intrabundle.model.enums.MetricScore;
import br.ufrgs.rmpestano.intrabundle.model.Metrics;
import br.ufrgs.rmpestano.intrabundle.model.OSGiModule;

import javax.inject.Singleton;

/**
 * Created by rmpestano on 8/17/14.
 */
@Singleton
public class MetricsCalculator implements Metrics {



    public MetricScore getLocMetric(OSGiModule bundle){
        long linesOfCode = bundle.getLinesOfCode();

        if(linesOfCode <= 300){
            return MetricScore.STATE_OF_ART;
        }else if(linesOfCode <= 500){
            return MetricScore.VERY_GOOD;
        }else if (linesOfCode <= 750){
            return MetricScore.GOOD;
        } else if (linesOfCode <= 1000){
            return MetricScore.REGULAR;
        }
        //lines of code greater than 1000
        return MetricScore.ANTI_PATTERN;
    }
}
