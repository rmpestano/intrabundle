package br.ufrgs.rmpestano.intrabundle.model.enums;

import java.util.Arrays;
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
        return Arrays.asList(FileType.values());
    }

    public String getName() {
        return name;
    }
}
