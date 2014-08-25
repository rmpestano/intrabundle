package br.ufrgs.rmpestano.intrabundle.model.enums;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by rmpestano on 1/26/14.
 */
public enum MetricName {
  LOC("metric.loc"),
  PUBLISHES_INTERFACES("metric.interfaces"),
  BUNDLE_DEPENDENCIES("metric.bundleDependencies"),
  USES_FRAMEWORK("metrics.usesFramework"),
  DECLARES_PERMISSION("metrics.declaresPermission"),
  STALE_REFERENCES("metric.staleReferences");

    private final String value;

    MetricName(String value) {
        this.value = value;
    }



    public static List<String> getAll() {
        List<String> retorno = new ArrayList<String>();
        for (MetricName m : MetricName.values()) {
            retorno.add(m.value);
        }
        return retorno;
    }

    @Override
    public String toString() {
        return value;
    }

}
