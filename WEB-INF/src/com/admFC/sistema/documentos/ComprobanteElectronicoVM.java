package com.admFC.sistema.documentos;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.MatchMedia;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
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
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

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

	private Date desde = new Date();
	private Date hasta;

	private boolean editar = false;

	@Init(superclass = true)
	public void initComprobanteElectronicoVM() {

		hasta = this.um.calcularFecha(this.desde, Calendar.HOUR, 24);

	/*	lContribuyentes = this.getContribuyentesPorUsuario();

		if (lContribuyentes.size() > 0) {

			this.contribuyenteSelected = lContribuyentes.get(0);

		}*/

		cargarComprobanteElectronicos();
		inicializarFiltros();
		inicializarFinders();

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
	public void cargarComprobanteElectronicos() {
		
		if (this.contribuyenteSelected == null) {
			
			return;
			
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		if (lContribuyentes.size() > 0) {

			String sql = this.um.getSql("comprobanteElectronico/listaComprobantesElectronicos.sql")
					.replace("?1", sdf.format(desde)).replace("?2", sdf.format(hasta))
					.replace("?3", this.contribuyenteSelected.getContribuyenteid() + "");

			this.lComprobantesElectronicos = this.reg.sqlNativo(sql);
			this.lComprobantesElectronicosOri = this.lComprobantesElectronicos;

		}

	}

	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[13];

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

		this.cargarComprobanteElectronicos();

		this.modal.detach();

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
	private int colResponsive = 6;
	
	@MatchMedia("all and (min-width: 958px)")
	@NotifyChange({ "collapsed", "includeSclass", "visibleResponsive", "colResponsive" })
	public void beWide() {

		this.visibleResponsive = true;
		this.colResponsive = 6;
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
				+ "--where cu.usuarioid = ?1 \n" + "order by c.contribuyenteid asc;";

		// String sqlContribuyente =
		// this.um.getSql("contribuyente/listaContribuyentes.sql");

		if (!this.isUserRolMaster()) {

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

}
