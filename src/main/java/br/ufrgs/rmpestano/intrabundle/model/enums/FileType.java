package br.ufrgs.rmpestano.intrabundle.model.enums;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by rmpestano on 2/23/14.
 */
public enum FileType {
    PDF("pdf"),HTML("html"),TXT("txt"),EXCEL("xls"),CSV("csv"),RTF("rtf");

    private final String name;


    FileType(String name) {
        this.name = name;
    }

    public static List<FileType> getAll(){
        List<FileType> fileTypes = Arrays.asList(FileType.values());
        Collections.sort(fileTypes,new Comparator<FileType>() {
            @Override
            public int compare(FileType o1, FileType o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        return fileTypes;
    }

    public String getName() {
        return name;
    }
}
