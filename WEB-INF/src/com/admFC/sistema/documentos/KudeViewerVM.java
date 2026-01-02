package com.admFC.sistema.documentos;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;

import com.admFC.modelo.Contribuyente;
import com.doxacore.util.Register;
import com.doxacore.util.SystemInfo;

import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.data.JRXmlDataSource;

public class KudeViewerVM {

    private String xmlKude;
    
    private String jasperPath;
    private  Map<String, Object> parametros;
    private JRXmlDataSource dataSource;

    @Init
    public void init() throws Exception {
        
    	// Leer los datos enviados por POST
        Map<String, String[]> params = Executions.getCurrent().getParameterMap();
        String[] xmlData = params.get("xml");
        xmlKude = (xmlData != null && xmlData.length > 0) ? xmlData[0] : "(Sin XML recibido)";
        
    	this.createKude(Long.parseLong(params.get("contribuyente")[0]));
    	
        
       
    }
    
    @AfterCompose(superclass = true)
	public void afterComposeKudeViewerVM() throws Exception {
    	
    
	}
    
    @Command
    public void createKude(long cid) throws Exception {
    	
    	
    	
        String xml = this.extraerRDE(this.xmlKude).trim();
        
        System.out.println(xml);
       
        Map<String, Object> dataFromXML = getTipoDocumentoFromXML(xml);
        int tipoDocumento = ((Integer)dataFromXML.get("tipoDocumento")).intValue();
        this.jasperPath = SystemInfo.SISTEMA_PATH_ABSOLUTO+"/reportTemplate/";
        
        System.out.println("jasper path del par 1 vale " + jasperPath);
       String pathDestino = SystemInfo.SISTEMA_PATH_ABSOLUTO+"/reportTemplate/";
        
       this.parametros = new HashMap<>();
       /* if (args.length > 3 && args[3] != null) {
          Gson gson = (new GsonBuilder()).setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
          try {
            parametros = (Map<String, Object>)gson.fromJson(args[3], parametros.getClass());
          } catch (Exception e) {
            System.err.println("Error ignorado... No se pudo convertir " + args[3] + " a Objeto!");
          } 
        } */
        if (tipoDocumento == 1)
          jasperPath = String.valueOf(jasperPath) + "Factura.jasper"; 
        if (tipoDocumento == 2)
          jasperPath = String.valueOf(jasperPath) + "FacturaImportacion.jasper"; 
        if (tipoDocumento == 3)
          jasperPath = String.valueOf(jasperPath) + "FacturaExportacion.jasper"; 
        if (tipoDocumento == 4)
          jasperPath = String.valueOf(jasperPath) + "AutoFactura.jasper"; 
        if (tipoDocumento == 5)
          jasperPath = String.valueOf(jasperPath) + "NotaCredito.jasper"; 
        if (tipoDocumento == 6)
          jasperPath = String.valueOf(jasperPath) + "NotaDebito.jasper"; 
        if (tipoDocumento == 7)
          jasperPath = String.valueOf(jasperPath) + "NotaRemision.jasper"; 
        System.out.println("----" + jasperPath);
        if (!(new File(jasperPath)).exists())
          throw new Exception("Archivo " + jasperPath + " no encontrado.!"); 
        try {
          InputStream inputStream = null;
          if (xml.startsWith("<?xml")) {
            inputStream = new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8));
          } else {
            inputStream = new FileInputStream(xml);
          } 
          this.dataSource = new JRXmlDataSource(inputStream, "/rDE/DE/gDtipDE/gCamItem");
          Locale locale = new Locale("es", "PY");
          parametros.put("REPORT_LOCALE", locale);
          //JasperPrint jprint = JasperFillManager.fillReport(jasperPath, parametros, (JRDataSource)dataSource);
         /* JasperPrint jprint = JasperFillManager.fillReport(jasperPath, parametros, dataSource);
         
          String tipoDocumentoDescripcion = (String)dataFromXML.get("tipoDocumentoDescripcion");
          String timbrado = (String)dataFromXML.get("timbrado");
          String establecimiento = (String)dataFromXML.get("establecimiento");
          String punto = (String)dataFromXML.get("punto");
          String numero = (String)dataFromXML.get("numero");
          String serie = (String)dataFromXML.get("serie");
          pathDestino = String.valueOf(pathDestino) + tipoDocumentoDescripcion + "_" + timbrado + "-" + establecimiento + "-" + punto + "-" + numero + ((serie != null) ? ("-" + serie) : "") + ".pdf";
          JasperExportManager.exportReportToPdfFile(jprint, pathDestino);*/
          
          Register reg = new Register();
          
          Contribuyente c = reg.getObjectById(Contribuyente.class.getName(),cid) ;
     
     	  try {
           	parametros.put("LOGO_URL", ImageIO.read(new ByteArrayInputStream(c.getLogo())));
   		} catch (IOException e) {
   			// TODO Auto-generated catch block
   			e.printStackTrace();
   		}

        } catch (Exception e) {
          e.printStackTrace();
        } 
    	
    }
    
    public String extraerRDE(String xml) throws Exception {
        // Limpiar espacios
        xml = xml.trim();

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false);
        DocumentBuilder builder = factory.newDocumentBuilder();

        // Parsear XML original
        Document doc = builder.parse(new InputSource(new StringReader(xml)));

        XPath xpath = XPathFactory.newInstance().newXPath();
        Node rdeNode = (Node) xpath.evaluate("//*[local-name()='rDE']", doc, XPathConstants.NODE);

        if (rdeNode == null) {
            throw new Exception("No se encontró rDE");
        }

        // Crear nuevo documento
        Document newDoc = builder.newDocument();
        Node imported = newDoc.importNode(rdeNode, true);
        newDoc.appendChild(imported);

        // Usar Transformer para controlar salida
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
        transformer.setOutputProperty(OutputKeys.STANDALONE, "no");
        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");

        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(newDoc), new StreamResult(writer));

        return writer.toString();
    }
    
    public Map<String, Object> getTipoDocumentoFromXML(String archivoDEXML) throws Exception {
        Map<String, Object> dataFromXML = new HashMap<>();
        File file = new File(archivoDEXML);
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
     
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = null;
       /* InputSource is = new InputSource(new StringReader(xml));
        Document document = documentBuilder.parse(is);*/
        
        if (archivoDEXML.startsWith("<?xml")) {
          InputStream inputStream = new ByteArrayInputStream(archivoDEXML.getBytes(StandardCharsets.UTF_8));
        	
        	/*byte[] raw = archivoDEXML.getBytes("ISO-8859-1");

            // Forzamos UTF-8 real
            String xmlUTF8 = new String(raw, StandardCharsets.UTF_8);

            InputStream inputStream = new ByteArrayInputStream(xmlUTF8.getBytes(StandardCharsets.UTF_8));*/
           
        	
          document = documentBuilder.parse(inputStream);
        } else {
          document = documentBuilder.parse(file);
        } 
        Node student = document.getElementsByTagName("rDE").item(0);
        Element dataFileElement = (Element)student;
        Node dataFile = dataFileElement.getElementsByTagName("DE").item(0);
        Element locationElement = (Element)dataFile;
        Node location = locationElement.getElementsByTagName("gTimb").item(0);
        Element iTipoDEElement = (Element)dataFile;
        Node iTiDE = iTipoDEElement.getElementsByTagName("iTiDE").item(0);
        Integer tipoDocumento = Integer.valueOf(iTiDE.getTextContent());
        Node dDesTiDE = iTipoDEElement.getElementsByTagName("dDesTiDE").item(0);
        String tipoDocumentoDescripcion = dDesTiDE.getTextContent();
        Node dNumTim = iTipoDEElement.getElementsByTagName("dNumTim").item(0);
        String timbrado = dNumTim.getTextContent();
        Node dEst = iTipoDEElement.getElementsByTagName("dEst").item(0);
        String establecimiento = dEst.getTextContent();
        Node dPunExp = iTipoDEElement.getElementsByTagName("dPunExp").item(0);
        String punto = dPunExp.getTextContent();
        Node dNumDoc = iTipoDEElement.getElementsByTagName("dNumDoc").item(0);
        String numero = dNumDoc.getTextContent();
        Node dSerieNum = iTipoDEElement.getElementsByTagName("dSerieNum").item(0);
        String serie = null;
        if (dSerieNum != null)
          dSerieNum.getTextContent(); 
        dataFromXML.put("tipoDocumento", tipoDocumento);
        dataFromXML.put("tipoDocumentoDescripcion", tipoDocumentoDescripcion);
        dataFromXML.put("timbrado", timbrado);
        dataFromXML.put("establecimiento", establecimiento);
        dataFromXML.put("punto", punto);
        dataFromXML.put("numero", numero);
        dataFromXML.put("serie", serie);
        return dataFromXML;
      }

	public String getXmlKude() {
		return xmlKude;
	}

	public void setXmlKude(String xmlKude) {
		this.xmlKude = xmlKude;
	}

	public String getJasperPath() {
		return jasperPath;
	}

	public void setJasperPath(String jasperPath) {
		this.jasperPath = jasperPath;
	}

	public Map<String, Object> getParametros() {
		return parametros;
	}

	public void setParametros(Map<String, Object> parametros) {
		this.parametros = parametros;
	}

	public JRXmlDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(JRXmlDataSource dataSource) {
		this.dataSource = dataSource;
	}
    
    
    
}