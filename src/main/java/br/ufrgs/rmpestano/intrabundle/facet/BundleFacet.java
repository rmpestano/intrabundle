package br.ufrgs.rmpestano.intrabundle.facet;

import org.jboss.forge.project.facets.BaseFacet;
import org.jboss.forge.resources.Resource;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by rmpestano on 1/22/14.
 */

public class BundleFacet extends BaseFacet {

    @Override
    public boolean install() {

        return isInstalled();
    }

    @Override
    public boolean isInstalled() {
        Resource<?> metaInf = project.getProjectRoot().getChild("META-INF");
        if(!metaInf.exists()){
            return false;
        }
        Resource<?> manifest = metaInf.getChild("MANIFEST.MF");
        RandomAccessFile randomAccessFile;
        try {
            File f = new File(manifest.getFullyQualifiedName());
            randomAccessFile = new RandomAccessFile(f,"r");
            return hasOsgiConfig(randomAccessFile);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }

    }

    private boolean hasOsgiConfig(RandomAccessFile aFile) throws IOException {
        for (int i = 0 ;i<aFile.getChannel().size();i++){
           String line = aFile.readLine();
           if(line.contains("Bundle")){
               return true;
           }
        }
        return false;
    }
}
