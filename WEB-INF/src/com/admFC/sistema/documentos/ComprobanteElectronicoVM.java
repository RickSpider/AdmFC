package com.admFC.sistema.documentos;

import java.io.IOException;
import java.io.StringWriter;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.MatchMedia;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.Notification;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.admFC.modelo.ComprobanteElectronico;
import com.admFC.modelo.Contribuyente;
import com.admFC.modelo.Evento;
import com.admFC.util.ParamsLocal;
import com.admFC.util.TemplateViewModelLocal;
import com.admFC.util.conexionRest.HttpConexion;
import com.admFC.util.conexionRest.ResultRest;
import com.doxacore.components.finder.FinderInterface;
import com.doxacore.components.finder.FinderModel;
import com.doxacore.modelo.Auditoria;
import com.doxacore.report.ReportBigExcel;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.StringReader;

public class ComprobanteElectronicoVM extends TemplateViewModelLocal implements FinderInterface {

	private List<Object[]> lComprobantesElectronicos;
	private List<Object[]> lComprobantesElectronicosOri;
	private ComprobanteElectronico comprobanteElectronicoSelected;
	private Auditoria auditoria;
	private List<Object[]> lContribuyentes;
	private Contribuyente contribuyenteSelected;

	private boolean opCrearComprobanteElectronico;
	private boolean opEditarComprobanteElectronico;
	private boolean opBorrarComprobanteElectronico;

	private boolean camposBloqueados = false;

	private Date desde;
	private Date hasta;

	private boolean editar = false;

	@Init(superclass = true)
	public void initComprobanteElectronicoVM() {
		
		desde = this.um.modificarHorasMinutosSegundos(new Date(), 0,0,0,0);
		
		
		hasta = this.um.modificarHorasMinutosSegundos(this.desde, 23, 59, 59, 999);
		
		System.out.println(new SimpleDateFormat("dd/MM/YYYY hh:mm:ss").format(new Date()));
		System.out.println(new SimpleDateFormat("dd/MM/YYYY hh:mm:ss").format(desde));

	/*	lContribuyentes = this.getContribuyentesPorUsuario();

		if (lContribuyentes.size() > 0) {

			this.contribuyenteSelected = lContribuyentes.get(0);

		}*/

		
		inicializarFiltros();
		inicializarFinders();
		cargarComprobanteElectronicos();

	}
	
	
	

	@AfterCompose(superclass = true)
	public void afterComposeComprobanteElectronicoVM() {

	}

	@Override
	protected void inicializarOperaciones() {

		this.opCrearComprobanteElectronico = this.operacionHabilitada(ParamsLocal.OP_CREAR_COMPROBANTEELECTRONICO);
		this.opEditarComprobanteElectronico = this.operacionHabilitada(ParamsLocal.OP_EDITAR_COMPROBANTEELECTRONICO);
		this.opBorrarComprobanteElectronico = this.operacionHabilitada(ParamsLocal.OP_BORRAR_COMPROBANTEELECTRONICO);

	}
	
	@Command
	@NotifyChange("lComprobantesElectronicos")
	public void refrescarCompronbantesElectronicos() {
		
		Notification.show("Refrescando Datos.");
		this.cargarComprobanteElectronicos();
		
	}

	@Command
	@NotifyChange("lComprobantesElectronicos")
	public void cargarComprobanteElectronicos() {
		
		if (this.contribuyenteSelected == null) {
			
			return;
			
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (lContribuyentes.size() > 0) {

			String sql = this.um.getSql("comprobanteElectronico/listaComprobantesElectronicos.sql")
					.replace("?1", sdf.format(desde)).replace("?2", sdf.format(hasta))
					.replace("?3", this.contribuyenteSelected.getContribuyenteid() + "");

			this.lComprobantesElectronicos = this.reg.sqlNativo(sql);
			this.lComprobantesElectronicosOri = this.lComprobantesElectronicos;
			
			this.filtrarComprobanteElectronico();

		}
		
		

	}

	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[15];

		for (int i = 0; i < this.filtroColumns.length; i++) {

			this.filtroColumns[i] = "";

		}

	}

	@Command
	@NotifyChange("lComprobantesElectronicos")
	public void filtrarComprobanteElectronico() {

		this.lComprobantesElectronicos = filtrarListaObject(this.filtroColumns, this.lComprobantesElectronicosOri);

	}

	// Seccion modal

	private Window modal;

	@Command
	public void modalComprobanteElectronicoAgregar() {

		if (!this.isOpCrearComprobanteElectronico())
			return;

		this.editar = false;
		this.modalComprobanteElectronico(-1);

	}

	@Command
	public void modalComprobanteElectronico(@BindingParam("comprobanteElectronicoid") long comprobanteElectronicoid) {

		this.auditoria = new Auditoria();
		this.auditoria.setModulo("Comprobante");

		if (comprobanteElectronicoid != -1) {

			if (!this.opEditarComprobanteElectronico)
				return;

			this.editar = true;
			this.camposBloqueados = true;
			this.comprobanteElectronicoSelected = this.reg.getObjectById(ComprobanteElectronico.class.getName(),
					comprobanteElectronicoid);
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
			this.auditoria.setJson(gson.toJson(this.comprobanteElectronicoSelected));

		} else {

			this.comprobanteElectronicoSelected = new ComprobanteElectronico();

		}

		modal = (Window) Executions.createComponents("/sistema/zul/documentos/comprobanteElectronicoModal.zul",
				this.mainComponent, null);
		Selectors.wireComponents(modal, this, false);
		modal.doModal();

	}

	@Command
	@NotifyChange("lComprobantesElectronicos")
	public void guardar() {

		this.save(this.comprobanteElectronicoSelected);

		this.comprobanteElectronicoSelected = null;

		

		this.auditoria.setUsuario(this.getCurrentUser().getAccount());

		if (editar) {

			Notification.show("ComprobanteElectronico Actualizado.");
			this.editar = false;
			this.auditoria.setSentencia("UPDATE");

		} else {

			this.auditoria.setSentencia("INSERT");
			Notification.show("El ComprobanteElectronico fue agregado.");
		}

		this.reg.saveObject(this.auditoria, "SYSTEM");
		
		this.modal.detach();
		this.cargarComprobanteElectronicos();
		

	}

	@Command
	public void openQrSifen(@BindingParam("linkQr") String linkQr) {

		Executions.getCurrent().sendRedirect(linkQr, "_blank");

	}

	@Command
	@NotifyChange("lComprobantesElectronicos")
	public void onChangeFiltroFechas() {

		this.cargarComprobanteElectronicos();

	}

	@Command
	@NotifyChange("lComprobantesElectronicos")
	public void onChangeContribuyente() {

		this.cargarComprobanteElectronicos();

	}

	@Command
	public void enviarLote() throws IOException {

		if (this.contribuyenteSelected == null) {
			this.mensajeError("Selecciona un contribuyente para iniciar el envio de comprobantes.");
			return;
		}

		Contribuyente c = new Contribuyente();
		c.setContribuyenteid(this.contribuyenteSelected.getContribuyenteid());
		c.setPass(this.contribuyenteSelected.getPass());

		HttpConexion conn = new HttpConexion();
		String link = this.getSistemaPropiedad("SERVIDOR_FC").getValor()
				+ this.getSistemaPropiedad("ENVIO_LOTE").getValor();
		ResultRest rr = conn.consumirREST(link, HttpConexion.POST, new Gson().toJson(c));

		Notification.show("Respuesta de servidor:\n Code:" + rr.getCode() + "\n Mensaje: " + rr.getMensaje());

	}

	@Command
	public void consultarEstados() throws IOException {

		if (this.contribuyenteSelected == null) {
			this.mensajeError("Selecciona un contribuyente para iniciar el envio de comprobantes.");
			return;
		}

		Contribuyente c = new Contribuyente();
		c.setContribuyenteid(this.contribuyenteSelected.getContribuyenteid());
		c.setPass(this.contribuyenteSelected.getPass());

		HttpConexion conn = new HttpConexion();
		String link = this.getSistemaPropiedad("SERVIDOR_FC").getValor()
				+ this.getSistemaPropiedad("CONSULTA_LOTE").getValor();
		ResultRest rr = conn.consumirREST(link, HttpConexion.POST, new Gson().toJson(c));

		Notification.show("Respuesta de servidor:\n Code:" + rr.getCode() + "\n Mensaje: " + rr.getMensaje());

	}
	
	private Evento eventoSelected;
	private int rdIndex = 0;
	private boolean visibleCancelacion = true;
	private boolean visibleInutilizacion = false;
	private long tiempoTranscurrido;
	
	@Command
	public void modalGenerarEvento(@BindingParam("comprobanteElectronicoid") long comprobanteElectronicoid) {
		
		this.comprobanteElectronicoSelected = this.reg.getObjectById(ComprobanteElectronico.class.getName(),
				comprobanteElectronicoid);
		
		if (this.comprobanteElectronicoSelected.getEvento() != null) {
			
			this.mensajeInfo("El Comprobante Eletronico ya tiene un evento asociado.");
			
			return;
			
		}
		
		this.eventoSelected = new Evento();
		this.eventoSelected.setFecha(new Date());
		this.eventoSelected.setCdc(this.comprobanteElectronicoSelected.getCdc());
		this.eventoSelected.setAmbiente(this.comprobanteElectronicoSelected.getAmbiente());
		this.eventoSelected.setTipoComprobanteElectronico(this.comprobanteElectronicoSelected.getTipoComprobanteElectronico());
		this.eventoSelected.setContribuyente(this.comprobanteElectronicoSelected.getContribuyente());
		this.eventoSelected.setMotivo("Cancelacion de CDC");
		this.eventoSelected.setEnviado(false);
		
		this.tiempoTranscurrido = (new Date().getTime() - this.comprobanteElectronicoSelected.getCreado().getTime())/(1000*60*60);
		
		
		modal = (Window) Executions.createComponents("/sistema/zul/documentos/generarEventoModal.zul",
				this.mainComponent, null);
		Selectors.wireComponents(modal, this, false);
		modal.doModal();

		
	}
	
	@Command
	@NotifyChange("*")
	public void onChangeRg(@BindingParam("index") int index) {
		
		this.rdIndex = index;
		
		this.eventoSelected = new Evento();
		
		this.visibleCancelacion = false;
		this.visibleInutilizacion = false;
		
		if (this.rdIndex == 0) {
			
			
			this.eventoSelected.setCdc(this.comprobanteElectronicoSelected.getCdc());
			this.eventoSelected.setMotivo("Cancelacion de CDC");			
			this.visibleCancelacion = true;
			
			
			
		}else if (this.rdIndex == 1){
			
					
			this.eventoSelected.setTimbrado(this.comprobanteElectronicoSelected.getTimbrado());			
			String [] arrayNum = this.comprobanteElectronicoSelected.getNumero().split("-");
			this.eventoSelected.setEstablecimiento(arrayNum[0]);
			this.eventoSelected.setPuntoExpedicion(arrayNum[1]);
			this.eventoSelected.setNumeroIni(arrayNum[2]);
			this.eventoSelected.setNumeroFin(arrayNum[2]);
			this.eventoSelected.setMotivo("Inutilizacion de Numeracion");
			this.visibleInutilizacion = true;
			
		}
		
		
		this.eventoSelected.setContribuyente(this.comprobanteElectronicoSelected.getContribuyente());
		this.eventoSelected.setEnviado(false);
		this.eventoSelected.setTipoComprobanteElectronico(this.comprobanteElectronicoSelected.getTipoComprobanteElectronico());	
		this.eventoSelected.setFecha(new Date());
		this.eventoSelected.setAmbiente(this.comprobanteElectronicoSelected.getAmbiente());
	
		
	}
	
	
	
	@Command
	public void aceptarEventoConfirmacion() {
		
		
		if (this.comprobanteElectronicoSelected.getEvento() != null) {
			
			this.mensajeError("El comprobante ya posee un evento");
			
			return;
			
		}
		
		if (this.rdIndex == 1){
			
			if (this.rdIndex == 1) {
				
				if (this.eventoSelected.getTimbrado() == null) {
					
					this.mensajeError("El evento no posee timbrado");
					
					return;
					
				}			
				
			}
			
		}

	
		
		EventListener event = new EventListener() {

			@Override
			public void onEvent(Event evt) throws Exception {

				if (evt.getName().equals(Messagebox.ON_YES)) {

					generarEvento(eventoSelected);

				}

			}

		};
		
		this.mensajeEliminar("Se generara el envento al Comprobante Electronico, continuar?", event);
		
		
	}
	
	private void generarEvento(Evento e) {
		
		this.save(e);
		
		BindUtils.postNotifyChange(null, null, this, "lComprobantesElectronicos");
		
		modal.detach();
		
	}

	@Command
	public void borrarConfirmacion(@BindingParam("dato") long id) {

		if (!this.opBorrarComprobanteElectronico)
			return;

		ComprobanteElectronico ce = this.reg.getObjectById(ComprobanteElectronico.class.getName(), id);

		EventListener event = new EventListener() {

			@Override
			public void onEvent(Event evt) throws Exception {

				if (evt.getName().equals(Messagebox.ON_YES)) {

					borrar(ce);

				}

			}

		};

		this.mensajeEliminar("El Comprobante Electronico sera borrado permanentemente. \n Continuar?", event);

	}
	
	public void consultarEstadoSifen(@BindingParam("dato") long id) throws IOException {
		
		ComprobanteElectronico ce = this.reg.getObjectById(ComprobanteElectronico.class.getName(), id);
		
		HttpConexion conn = new HttpConexion();
		String link = this.getSistemaPropiedad("SERVIDOR_FC").getValor()
				+ this.getSistemaPropiedad("CONSULTA_SIFEN_CDC").getValor()+"/"+ce.getCdc();
		ResultRest rr = conn.consumirREST(link, HttpConexion.GET, null);

		Notification.show("Respuesta de servidor:\n Code:" + rr.getCode() + "\n Mensaje: " + rr.getMensaje());
		
	}

	private void borrar(ComprobanteElectronico ce) {

		this.reg.deleteObject(ce);
		this.cargarComprobanteElectronicos();
		BindUtils.postNotifyChange(null, null, this, "lComprobantesElectronicos");
	}

	//seccion responsive
	private boolean visibleResponsive = true;
	private int colResponsive = 7;
	
	@MatchMedia("all and (min-width: 958px)")
	@NotifyChange({ "collapsed", "includeSclass", "visibleResponsive", "colResponsive" })
	public void beWide() {

		this.visibleResponsive = true;
		this.colResponsive = 7;
	}

	@MatchMedia("all and (max-width: 957px)")
	@NotifyChange({ "collapsed", "includeSclass", "visibleResponsive", "colResponsive" })
	public void beNarrow() {

		this.visibleResponsive = false;
		this.colResponsive = 2;

	}

	// -------------Finder Seccion --------------

	private FinderModel contribuyenteFinder;

	@NotifyChange("*")
	public void inicializarFinders() {

		String sqlContribuyente = "Select c.contribuyenteid as id, c.nombre as contribuyente, (c.ruc||'-'||c.dv) as ruc, ambiente as ambiente from contribuyentes c \n"
				+ "--left join contribuyentesusuarios cu on cu.contribuyenteid = c.contribuyenteid \n"
				+ "where c.habilitado = true \n "
				+ "-- and cu.usuarioid = ?1 \n" 
				+ "order by c.contribuyenteid asc;";

		// String sqlContribuyente =
		// this.um.getSql("contribuyente/listaContribuyentes.sql");

		if (!this.isUserRolMaster() && !this.isUserRolAdmin()) {

			sqlContribuyente = sqlContribuyente.replace("--", "").replace("?1",
					this.getCurrentUser().getUsuarioid() + "");

		}

		contribuyenteFinder = new FinderModel("Contribuyente", sqlContribuyente);

		this.lContribuyentes = this.reg.sqlNativo(sqlContribuyente);

		// System.out.println("El tamaño de lContribuyetes es:
		// "+this.lContribuyentes.size());
	}

	public void generarFinders(@BindingParam("finder") String finder) {

		if (finder.compareTo(this.contribuyenteFinder.getNameFinder()) == 0) {

			this.contribuyenteFinder.generarListFinder();
			BindUtils.postNotifyChange(null, null, this.contribuyenteFinder, "listFinder");

		}

	}

	@Command
	public void finderFilter(@BindingParam("filter") String filter, @BindingParam("finder") String finder) {

		if (finder.compareTo(this.contribuyenteFinder.getNameFinder()) == 0) {

			this.contribuyenteFinder
					.setListFinder(this.filtrarListaObject(filter, this.contribuyenteFinder.getListFinderOri()));
			BindUtils.postNotifyChange(null, null, this.contribuyenteFinder, "listFinder");

		}

	}

	@Command
	@NotifyChange("*")
	public void onSelectetItemFinder(@BindingParam("id") Long id, @BindingParam("finder") String finder) {

		if (finder.compareTo(this.contribuyenteFinder.getNameFinder()) == 0) {

			this.contribuyenteSelected = this.reg.getObjectById(Contribuyente.class.getName(), id);
			// BindUtils.postNotifyChange(null, null, this, "contribuyenteFinder");
		}

		this.cargarComprobanteElectronicos();

	}
	
	@Command
	public void uploadFile(@BindingParam("file") Media file) {
		
		if (file.getName().contains(".png")) {
			
			
		}else {
			
			this.mensajeInfo("Archivo no valido.");
			
		}
		
	}
	
	@Command
	public void exportarExcel() {
		
		if(this.contribuyenteSelected == null) {
			
			this.mensajeInfo("Debes seleccionar un contribuyente.");
			
			return;
			
		}
		
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("es", "ES"));
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#,##0.##",dfs);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
		List<String[]> titulos = new ArrayList<String[]>();
		
		
		String[] t1 = {"Comprobantes Electronicos"};
		String[] t2 = {this.contribuyenteSelected.getNombre()};
		String[] t3 = {"Fecha Desde:", sdf.format(this.desde)};
		String[] t4 = {"Fecha Hasta:", sdf.format(this.hasta)};
		String[] espacioBlanco = {""};

		titulos.add(t1);
		titulos.add(t2);
		titulos.add(espacioBlanco);
		titulos.add(t3);
		titulos.add(t4);
		titulos.add(espacioBlanco);
		
		List<String[]> headersDatos = new ArrayList<String[]>();
		String [] hd1 =  {"ID","CREADO","CDC", "NUMERO", "ENVIO POR LOTE", "ENVIADO LOTE", "ENVIADO", "LOTE Nº", "ESTADO", "RESPUESTA" ,"TIPO COMPROBANTE","AMBIENTE","TOTAL"};
		headersDatos.add(hd1);
		
		/*String sql = this.um.getSql("comprobanteElectronico/listaComprobantesElectronicos.sql")
				.replace("?1", sdf.format(desde)).replace("?2", sdf.format(hasta))
				.replace("?3", this.contribuyenteSelected.getContribuyenteid() + "");
		
		List<Object[]> datos = this.reg.sqlNativo(sql);*/
		
		List<Object[]> detalles = new ArrayList<>();
		
		for (Object[] ox : this.lComprobantesElectronicos) {
			
			Object[] o = new Object[13];
			
				
			o[0] = ox[0].toString();
			o[1] = ox[1].toString();
			o[2] = ox[3].toString();
			o[3] = ox[4].toString();
			o[4] = ox[5].toString();
			o[5] = ox[6].toString();
			o[6] = ox[7].toString();
			
			o[7] = "";
			if (ox[8]!=null) {
				
				o[7] = ox[8].toString();
			}
			
			
			o[8] = ox[9].toString();
			o[9] = "";
			if (ox[13] != null) {
				o[9] = ox[13].toString();
			}
			o[10] = ox[10].toString();
			o[11] = ox[12].toString();
			
			
			
			o[12] = df.format((Double)ox[14]);
			
			
			detalles.add(o);
		}
		
		ReportBigExcel re = new ReportBigExcel("CE_"+this.contribuyenteSelected.getNombre().trim().replaceAll("\\s+", "").replace(".", "").replace(",", ""));
		re.descargar(titulos, headersDatos, detalles);
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
	            js.append("i.value='").append(entry.getValue()
	                    .replace("\\", "\\\\")
	                    .replace("'", "\\'")
	                    .replace("\r", "")
	                    .replace("\n", "\\n"))
	                    .append("';");
	            js.append("f.appendChild(i);");
	        }

	        js.append("document.body.appendChild(f); f.submit(); document.body.removeChild(f);");

	        Clients.evalJavaScript(js.toString());
	    }
	 
	 
	 @Command
	 public void verXml() {
		 	
		 
		 
	        String prettyXml = prettyPrintXml(this.comprobanteElectronicoSelected.getXml());
	        
	        prettyXml = prettyXml.replace("&", "&amp;")
	                  .replace("<", "&lt;")
	                  .replace(">", "&gt;")
	                  .replace("\"", "&quot;")
	                  .replace("'", "&apos;");
	        
	        Map<String, String> params = new HashMap<>();
	        params.put("xml", prettyXml);

	        this.openInNewTabPost("sistema/zul/documentos/xmlviewer.zul", params);
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
	            return writer.toString();
	        } catch (Exception e) {
	            return xml;
	        }
	    }

	public ComprobanteElectronico getComprobanteElectronicoSelected() {
		return comprobanteElectronicoSelected;
	}

	public void setComprobanteElectronicoSelected(ComprobanteElectronico comprobanteElectronicoSelected) {
		this.comprobanteElectronicoSelected = comprobanteElectronicoSelected;
	}

	public boolean isOpCrearComprobanteElectronico() {
		return opCrearComprobanteElectronico;
	}

	public void setOpCrearComprobanteElectronico(boolean opCrearComprobanteElectronico) {
		this.opCrearComprobanteElectronico = opCrearComprobanteElectronico;
	}

	public boolean isOpEditarComprobanteElectronico() {
		return opEditarComprobanteElectronico;
	}

	public void setOpEditarComprobanteElectronico(boolean opEditarComprobanteElectronico) {
		this.opEditarComprobanteElectronico = opEditarComprobanteElectronico;
	}

	public boolean isOpBorrarComprobanteElectronico() {
		return opBorrarComprobanteElectronico;
	}

	public void setOpBorrarComprobanteElectronico(boolean opBorrarComprobanteElectronico) {
		this.opBorrarComprobanteElectronico = opBorrarComprobanteElectronico;
	}

	public String[] getFiltroColumns() {
		return filtroColumns;
	}

	public void setFiltroColumns(String[] filtroColumns) {
		this.filtroColumns = filtroColumns;
	}

	public boolean isEditar() {
		return editar;
	}

	public void setEditar(boolean editar) {
		this.editar = editar;
	}

	public List<Object[]> getlComprobantesElectronicos() {
		return lComprobantesElectronicos;
	}

	public void setlComprobantesElectronicos(List<Object[]> lComprobantesElectronicos) {
		this.lComprobantesElectronicos = lComprobantesElectronicos;
	}

	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public List<Object[]> getlContribuyentes() {
		return lContribuyentes;
	}

	public void setlContribuyentes(List<Object[]> lContribuyentes) {
		this.lContribuyentes = lContribuyentes;
	}

	public Contribuyente getContribuyenteSelected() {
		return contribuyenteSelected;
	}

	public void setContribuyenteSelected(Contribuyente contribuyenteSelected) {
		this.contribuyenteSelected = contribuyenteSelected;
	}

	public boolean isCamposBloqueados() {
		return camposBloqueados;
	}

	public void setCamposBloqueados(boolean camposBloqueados) {
		this.camposBloqueados = camposBloqueados;
	}

	public boolean isVisibleResponsive() {
		return visibleResponsive;
	}

	public void setVisibleResponsive(boolean visibleResponsive) {
		this.visibleResponsive = visibleResponsive;
	}

	public FinderModel getContribuyenteFinder() {
		return contribuyenteFinder;
	}

	public void setContribuyenteFinder(FinderModel contribuyenteFinder) {
		this.contribuyenteFinder = contribuyenteFinder;
	}

	public int getColResponsive() {
		return colResponsive;
	}

	public void setColResponsive(int colResponsive) {
		this.colResponsive = colResponsive;
	}




	public Evento getEventoSelected() {
		return eventoSelected;
	}




	public void setEventoSelected(Evento eventoSelected) {
		this.eventoSelected = eventoSelected;
	}




	public int getRdIndex() {
		return rdIndex;
	}




	public void setRdIndex(int rdIndex) {
		this.rdIndex = rdIndex;
	}




	public boolean isVisibleCancelacion() {
		return visibleCancelacion;
	}




	public void setVisibleCancelacion(boolean visibleCancelacion) {
		this.visibleCancelacion = visibleCancelacion;
	}




	public boolean isVisibleInutilizacion() {
		return visibleInutilizacion;
	}




	public void setVisibleInutilizacion(boolean visibleInutilizacion) {
		this.visibleInutilizacion = visibleInutilizacion;
	}




	public long getTiempoTranscurrido() {
		return tiempoTranscurrido;
	}




	public void setTiempoTranscurrido(long tiempoTranscurrido) {
		this.tiempoTranscurrido = tiempoTranscurrido;
	}
	
	

}
