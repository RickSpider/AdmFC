package com.admFC.sistema.documentos;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.util.Notification;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.admFC.modelo.Evento;
import com.admFC.modelo.Contribuyente;
import com.admFC.util.ParamsLocal;
import com.admFC.util.TemplateViewModelLocal;
import com.admFC.util.conexionRest.HttpConexion;
import com.admFC.util.conexionRest.ResultRest;
import com.doxacore.components.finder.FinderInterface;
import com.doxacore.components.finder.FinderModel;
import com.doxacore.modelo.Auditoria;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EventoVM extends TemplateViewModelLocal implements FinderInterface {

	private List<Object[]> lEventos;
	private List<Object[]> lEventosOri;
	private Evento eventoSelected;
	private Auditoria auditoria;
	private List<Object[]> lContribuyentes;
	private Contribuyente contribuyenteSelected;

	private boolean opCrearEvento;
	private boolean opEditarEvento;
	private boolean opBorrarEvento;

	private Date desde = new Date();
	private Date hasta;

	private boolean editar = false;

	private boolean camposBloqueados = true;

	@Init(superclass = true)
	public void initEventoVM() {

		
		desde = this.um.modificarHorasMinutosSegundos(new Date(), 0,0,0,0);
		
		
		hasta = this.um.modificarHorasMinutosSegundos(this.desde, 23, 59, 59, 999);

		
		inicializarFiltros();
		inicializarFinders();
		cargarEventos();

	}

	@AfterCompose(superclass = true)
	public void afterComposeEventoVM() {

	}

	@Override
	protected void inicializarOperaciones() {

		this.opCrearEvento = this.operacionHabilitada(ParamsLocal.OP_CREAR_EVENTO);
		this.opEditarEvento = this.operacionHabilitada(ParamsLocal.OP_EDITAR_EVENTO);
		this.opBorrarEvento = this.operacionHabilitada(ParamsLocal.OP_BORRAR_EVENTO);

	}
	
	@Command
	@NotifyChange("*")
	public void refrescarEventos() {
		
		Notification.show("Refrescando Datos.");
		this.cargarEventos();
		
	}

	@Command
	@NotifyChange("*")
	public void cargarEventos() {
		

		if (this.contribuyenteSelected == null) {
			
			return;
			
		}


		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		if (lContribuyentes.size() > 0) {

			String sql = this.um.getSql("evento/listaEventos.sql").replace("?1", sdf.format(desde))
					.replace("?2", sdf.format(hasta))
					.replace("?3", this.contribuyenteSelected.getContribuyenteid() + "");
			;

			System.out.println("==================SQL EVENTOS=====================");
			System.out.println(sql);
			System.out.println("==================================================");

			this.lEventos = this.reg.sqlNativo(sql);
			this.lEventosOri = this.lEventos;
			
			this.filtrarEvento();

		}
		
		

	}

	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[11];

		for (int i = 0; i < this.filtroColumns.length; i++) {

			this.filtroColumns[i] = "";

		}

	}

	@Command
	@NotifyChange("lEventos")
	public void filtrarEvento() {

		this.lEventos = filtrarListaObject(this.filtroColumns, this.lEventosOri);

	}

	// Seccion modal

	private Window modal;

	@Command
	public void modalEventoAgregar() {

		if (!this.isOpCrearEvento())
			return;

		this.camposBloqueados = true;
		this.editar = false;
		this.modalEvento(-1);

	}

	@Command
	public void modalEvento(@BindingParam("eventoid") long eventoid) {

		this.auditoria = new Auditoria();
		this.auditoria.setModulo("Comprobante");

		if (eventoid != -1) {

			if (!this.opEditarEvento)
				return;

			this.editar = true;
			this.eventoSelected = this.reg.getObjectById(Evento.class.getName(), eventoid);
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
			this.auditoria.setJson(gson.toJson(this.eventoSelected));

		} else {

			this.eventoSelected = new Evento();

		}

		modal = (Window) Executions.createComponents("/sistema/zul/documentos/eventoModal.zul", this.mainComponent,
				null);
		Selectors.wireComponents(modal, this, false);
		modal.doModal();

	}

	@Command
	@NotifyChange("lEventos")
	public void guardar() {

		this.save(this.eventoSelected);

		this.eventoSelected = null;

		

		this.modal.detach();

		this.auditoria.setUsuario(this.getCurrentUser().getAccount());

		if (editar) {

			Notification.show("Evento Actualizado.");
			this.editar = false;
			this.auditoria.setSentencia("UPDATE");

		} else {

			this.auditoria.setSentencia("INSERT");
			Notification.show("El Evento fue agregado.");
		}

		this.reg.saveObject(this.auditoria, "SYSTEM");
		
		this.cargarEventos();
		this.filtrarEvento();

	}

	@Command
	@NotifyChange("lEventos")
	public void onChangeFiltroFechas() {

		this.cargarEventos();

	}

	@Command
	@NotifyChange("lEventos")
	public void onChangeContribuyente() {

		this.cargarEventos();

	}

	@Command
	@NotifyChange("lEventos")
	public void enviarEventos() throws IOException {
		
		if (this.contribuyenteSelected == null) {
			this.mensajeError("Selecciona un contribuyente para iniciar el envio de comprobantes.");
			return;
		}
		
		Contribuyente c = new Contribuyente();
		c.setContribuyenteid(this.contribuyenteSelected.getContribuyenteid());
		c.setPass(this.contribuyenteSelected.getPass());

		HttpConexion conn = new HttpConexion();
		String link = this.getSistemaPropiedad("SERVIDOR_FC").getValor()
				+ this.getSistemaPropiedad("ENVIO_EVENTO").getValor();
		ResultRest rr = conn.consumirREST(link, HttpConexion.POST, new Gson().toJson(c));

		Notification.show("Respuesta de servidor:\n Code:" + rr.getCode() + "\n Mensaje: " + rr.getMensaje());

		this.cargarEventos();
	}

	@Command
	public void borrarConfirmacion(@BindingParam("dato") long id) {

		if (!this.opBorrarEvento)
			return;

		Evento evento = this.reg.getObjectById(Evento.class.getName(), id);

		EventListener event = new EventListener() {

			@Override
			public void onEvent(Event evt) throws Exception {

				if (evt.getName().equals(Messagebox.ON_YES)) {

					borrar(evento);

				}

			}

		};

		this.mensajeEliminar("El Evento sera borrado permanentemente. \n Continuar?", event);

	}

	private void borrar(Evento e) {

		this.reg.deleteObject(e);
		this.cargarEventos();
		BindUtils.postNotifyChange(null, null, this, "lEventos");
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

		this.cargarEventos();

	}

	public List<Object[]> getlEventos() {
		return lEventos;
	}

	public void setlEventos(List<Object[]> lEventos) {
		this.lEventos = lEventos;
	}

	public Evento getEventoSelected() {
		return eventoSelected;
	}

	public void setEventoSelected(Evento eventoSelected) {
		this.eventoSelected = eventoSelected;
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

	public boolean isOpCrearEvento() {
		return opCrearEvento;
	}

	public void setOpCrearEvento(boolean opCrearEvento) {
		this.opCrearEvento = opCrearEvento;
	}

	public boolean isOpEditarEvento() {
		return opEditarEvento;
	}

	public void setOpEditarEvento(boolean opEditarEvento) {
		this.opEditarEvento = opEditarEvento;
	}

	public boolean isOpBorrarEvento() {
		return opBorrarEvento;
	}

	public void setOpBorrarEvento(boolean opBorrarEvento) {
		this.opBorrarEvento = opBorrarEvento;
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

	public boolean isEditar() {
		return editar;
	}

	public void setEditar(boolean editar) {
		this.editar = editar;
	}

	public String[] getFiltroColumns() {
		return filtroColumns;
	}

	public void setFiltroColumns(String[] filtroColumns) {
		this.filtroColumns = filtroColumns;
	}

	public boolean isCamposBloqueados() {
		return camposBloqueados;
	}

	public void setCamposBloqueados(boolean camposBloqueados) {
		this.camposBloqueados = camposBloqueados;
	}

	public FinderModel getContribuyenteFinder() {
		return contribuyenteFinder;
	}

	public void setContribuyenteFinder(FinderModel contribuyenteFinder) {
		this.contribuyenteFinder = contribuyenteFinder;
	}

}
