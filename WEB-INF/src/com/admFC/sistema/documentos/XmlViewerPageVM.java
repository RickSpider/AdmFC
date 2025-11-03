package com.admFC.sistema.documentos;

import java.util.Map;

import org.zkoss.bind.annotation.Init;
import org.zkoss.zk.ui.Executions;

public class XmlViewerPageVM {

    private String prettyXml;

    public String getPrettyXml() {
        return prettyXml;
    }

    @Init
    public void init() {
        // Leer los datos enviados por POST
        Map<String, String[]> params = Executions.getCurrent().getParameterMap();
        String[] xmlData = params.get("xml");
        prettyXml = (xmlData != null && xmlData.length > 0) ? xmlData[0] : "(Sin XML recibido)";
    }
}