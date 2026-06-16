package com.admFC.sistemaResp;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.image.AImage;
import org.zkoss.util.media.Media;
import org.zkoss.zhtml.Filedownload;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;

import com.admFC.modelo.ComprobanteElectronico;
import com.admFC.modelo.Contribuyente;
import com.doxacore.TemplateNoLoginViewModel;

public class buscarFacturaVM extends TemplateNoLoginViewModel {

	private Contribuyente contribuyente;

	private ComprobanteElectronico ce;
	
	private Media logoFile;

	private String docNum;

	@Init(superclass = true)
	public void initBusarFacturaVM() {

		String alias = Executions.getCurrent().getParameter("emi").replace("-", "");

		byte[] decoded = Base64.getUrlDecoder().decode(alias);
		
		String id = new String(decoded, StandardCharsets.UTF_8).split("-")[2];
		
		this.contribuyente = this.reg.getObjectById(Contribuyente.class.getName(), Long.parseLong(id));
		
	}

	@NotifyChange("*")
	@AfterCompose(superclass = true)
	public void afterComposeBusarFacturaVM() {

		

		System.out.println(
				"El ruc del contribuyente es = " + this.contribuyente.getRuc() + "-" + this.contribuyente.getDv());
		
		if (this.contribuyente.getLogo() != null) {
			
			try {
				this.logoFile = new AImage("logo.png",this.contribuyente.getLogo());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
	}

	@Command
	@NotifyChange("ce")
	public void buscarDocumento() {

		if (docNum == null || docNum.isEmpty()) {

			return;
		}

		ce = this.reg.getObjectByCondicion(ComprobanteElectronico.class.getName(),
				"contribuyenteid = " + this.contribuyente.getContribuyenteid() + "and numero like '" + docNum
						+ "' and Estado like 'Aprobado'");

		if (ce != null) {
			
			Clients.evalJavaScript(
				    "var result = document.getElementById('resultPanel');"+
				    "var notFound = document.getElementById('notFundPanel');" +
				    "if(result) {" +
				    "   result.style.display='block';" +
				    "   result.scrollIntoView({behavior:'smooth'});" +
				    "}"+
				    "if(notFound) notFound.style.display = 'none';"
				);

		} else {

			Clients.evalJavaScript(
				    "var result = document.getElementById('resultPanel');"+
				    "var notFound = document.getElementById('notFundPanel');" +
				    "if(result) {" +
				    "   result.style.display='none';" +
				    "}"+
				    "if(notFound) notFound.style.display = 'block';"
				);
		}

	}
	
	
	
	@Command
	public void descargarXML() {
		
		try {
			Filedownload.save(
				    this.ce.getXml().getBytes("UTF-8"),
				    "application/xml",
				    this.ce.getNumero()+".xml"
				);
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private void openInNewTabPost(String url, Map<String, String> params) {
		StringBuilder js = new StringBuilder();
		js.append("var f=document.createElement('form');");
		js.append("f.method='POST';");
		js.append("f.action='").append(url).append("';");
		js.append("f.target='_blank';");

		for (Map.Entry<String, String> entry : params.entrySet()) {
			js.append("var i=document.createElement('input');");
			js.append("i.type='hidden';");
			js.append("i.name='").append(entry.getKey()).append("';");
			js.append("i.value='").append(
					entry.getValue().replace("\\", "\\\\").replace("'", "\\'").replace("\r", "").replace("\n", "\\n"))
					.append("';");
			js.append("f.appendChild(i);");
		}

		js.append("document.body.appendChild(f); f.submit(); document.body.removeChild(f);");

		Clients.evalJavaScript(js.toString());
	}

	@Command
	public void verKude() {

		// ComprobanteElectronico ce =
		// this.reg.getObjectById(ComprobanteElectronico.class.getName(), id);

		// String prettyXml = prettyPrintXml(ce.getXml());

		Map<String, String> params = new HashMap<>();
		// params.put("xml", prettyXml);
		// params.put("contribuyente", ce.getContribuyente().getContribuyenteid()+"");
		params.put("id", ce.getId() + "");

		this.openInNewTabPost("../sistema/zul/documentos/kudeViewer.zul", params);
	}

	public Contribuyente getContribuyente() {
		return contribuyente;
	}

	public void setContribuyente(Contribuyente contribuyente) {
		this.contribuyente = contribuyente;
	}

	public ComprobanteElectronico getCe() {
		return ce;
	}

	public void setCe(ComprobanteElectronico ce) {
		this.ce = ce;
	}

	public String getDocNum() {
		return docNum;
	}

	public void setDocNum(String docNum) {
		this.docNum = docNum;
	}

	public Media getLogoFile() {
		return logoFile;
	}

	public void setLogoFile(Media logoFile) {
		this.logoFile = logoFile;
	}
	
	

}
