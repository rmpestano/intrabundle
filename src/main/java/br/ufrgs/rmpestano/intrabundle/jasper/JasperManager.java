package br.ufrgs.rmpestano.intrabundle.jasper;

import br.ufrgs.rmpestano.intrabundle.i18n.MessageProvider;
import br.ufrgs.rmpestano.intrabundle.model.enums.FileType;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.export.*;
import org.jboss.forge.shell.Shell;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class JasperManager implements Serializable {

	private static final long serialVersionUID = 1L;

    private String fileName;
    private String reportName;
    private List<?> data;
    private Map params;
    private FileType type;

    @Inject
    Shell shell;

    @Inject
    MessageProvider provider;


    /**
     *
     * @param filename name of the generated PDF file, if not provided reportName.pdf will be used
     * @return
     */
    public JasperManager filename(String filename){
        this.fileName = filename;
        return this;
    }

    /**
     *
     * @param reportName name of the .jasper file
     * @return
     */
    public JasperManager reportName(String reportName){
        this.reportName = reportName;
        return this;
    }


    public JasperManager data(List<?> data){
        this.data = data;
        return this;
    }

    public JasperManager type(FileType type){
        this.type = type;
        return this;
    }

    public JasperManager params(Map params){
            this.params = params;
            return this;
    }

    /**
     * build PDF file from .jasper(located on /resources/report/filename) on current directory
     */
	public void build() {
        if(reportName == null || data == null){
            throw new RuntimeException("reportName and data is required to generate the report");
        }
        if(fileName == null){
            fileName = reportName;
        }

        if(type == null){
            type = FileType.PDF;
        }

        if(params == null){
            params = new HashMap();
        }
        String path = shell.getCurrentDirectory().getFullyQualifiedName()+"/"+fileName+"."+type.getName();
        JasperPrint jp = getJasperPrint();
        JRExporter exporter = null;
		try {
            switch (type){
                case PDF: {
                    JasperExportManager.exportReportToPdfFile(jp, path);
                    break;
                }
                case HTML:{
                    JasperExportManager.exportReportToHtmlFile(jp, path);
                    break;
                }
                case TXT:{
                    exporter = new JRTextExporter();
                    exporter.setParameter(JRTextExporterParameter.JASPER_PRINT, jp);
                    exporter.setParameter(JRTextExporterParameter.OUTPUT_FILE_NAME, path);
                    exporter.setParameter(JRTextExporterParameter.PAGE_WIDTH, 300);
                    exporter.setParameter(JRTextExporterParameter.PAGE_HEIGHT, 40);
                    exporter.exportReport();
                    break;
                }
                case EXCEL:{
                    exporter = new JRXlsExporter();
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT,
                            jp);
                    exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,
                            path);
                    exporter.exportReport();
                    break;
                }

                case CSV:{
                    exporter = new JRCsvExporter();
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT,
                            jp);
                    exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,
                            path);
                    exporter.exportReport();
                    break;
                }
                case RTF:{
                    exporter = new JRRtfExporter();
                    exporter.setParameter(JRExporterParameter.JASPER_PRINT,
                            jp);
                    exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME,
                            path);
                    exporter.exportReport();
                    break;
                }

            }

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
        shell.println(provider.getMessage("report.generated",path));
	}

	/**
	 * get JasperPrint from .jasper file
	 *
	 */
	public JasperPrint getJasperPrint() {
		try {
            //reports are placed under /resources/reports
            String path = "/reports/"+reportName;
            if(!path.endsWith(".jasper")){
                path = path.concat(".jasper");
            }
            InputStream is = getClass().getResourceAsStream(path);
            if (is == null) {
                throw new RuntimeException("File " + path + " not found");
            }
            params.put("INITIAL_TIME",new Date());
            params.put("SUBREPORT_DIR","/reports/");
            return JasperFillManager.fillReport(is, params, new JRBeanCollectionDataSource(data));
		} catch (JRException e) {
			throw new RuntimeException(e);
		}
	}



}
