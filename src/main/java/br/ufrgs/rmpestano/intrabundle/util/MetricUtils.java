package br.ufrgs.rmpestano.intrabundle.util;

import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.model.MetricPoints;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Created by pestano on 05/04/15.
 */
@Singleton
public class MetricUtils {

    @Inject
    MessageProvider provider;

    public String metricQuality(MetricPoints metricPoints) {
        return provider.getMessage("metrics.quality", metricPoints.getBundlePoints(), metricPoints.getMaxPoints(), metricPoints.getCompliance());

    }



}
