package br.ufrgs.rmpestano.intrabundle.model.enums;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by rmpestano on 1/26/14.
 */
public enum MetricName {
  CYCLE("metrics.cycle"),
  PUBLISHES_INTERFACES("metrics.interfaces"),
  BUNDLE_DEPENDENCIES("metrics.bundleDependencies"),
  USES_FRAMEWORK("metrics.usesFramework"),
  DECLARES_PERMISSION("metrics.declaresPermission"),
  STALE_REFERENCES("metrics.staleReferences");

    private final String value;

    MetricName(String value) {
        this.value = value;
    }

    public String getValue(){
        return value;
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
