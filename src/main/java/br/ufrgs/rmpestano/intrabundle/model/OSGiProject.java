package br.ufrgs.rmpestano.intrabundle.model;

import java.io.Serializable;
import java.util.List;

/**
 * Created by rmpestano on 1/28/14.
 */
public interface OSGiProject extends Serializable{

    List<OSGiModule> getModules();
}
