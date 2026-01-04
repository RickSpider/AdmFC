package com.admFC.sistema.documentos;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;

import com.admFC.modelo.ComprobanteElectronico;
import com.doxacore.util.Register;

public class XmlViewerPageVM {

    private String prettyXml;
    

    public String getPrettyXml() {
        return prettyXml;
    }

    @Init
    public void init() {
        // Leer los datos enviados por POST
        Map<String, String[]> params = Executions.getCurrent().getParameterMap();
        /*String[] xmlData = params.get("xml");
        prettyXml = (xmlData != null && xmlData.length > 0) ? xmlData[0] : "(Sin XML recibido)";*/
        
        String[] id = params.get("id");
        
        if (id == null && id.length <= 0) {
        	
        	this.prettyXml = "ID no recibido";
        	return;
        }
        
        Register reg = new Register();
        ComprobanteElectronico ce = reg.getObjectById(ComprobanteElectronico.class.getName(), Long.valueOf(id[0].toString()));

        prettyXml = prettyPrintXml(ce.getXml());
       
    }
    
    
    private String prettyPrintXml(String xml) {
        try {
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
            StreamSource source = new StreamSource(new StringReader(xml));
            StringWriter writer = new StringWriter();
            transformer.transform(source, new StreamResult(writer));
            
            String out = writer.toString().replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;");
            
            return out;
        } catch (Exception e) {
            return xml;
        }
    }
}