package br.ufrgs.rmpestano.intrabundle.event;

import br.ufrgs.rmpestano.intrabundle.model.OSGiProject;

import java.io.Serializable;

/**
 * Created by rmpestano on 8/30/14.
 */
public class ProjectChangeEvent implements Serializable{

    private OSGiProject osGiProject;

    public ProjectChangeEvent(OSGiProject osGiProject) {
        this.osGiProject = osGiProject;
    }

    public OSGiProject getOsGiProject() {
        return osGiProject;
    }
}
