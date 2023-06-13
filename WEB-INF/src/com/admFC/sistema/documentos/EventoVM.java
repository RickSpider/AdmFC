package com.admFC.sistema.documentos;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.util.Notification;
import org.zkoss.zul.Window;

import com.admFC.modelo.Evento;
import com.admFC.modelo.Contribuyente;
import com.admFC.util.ParamsLocal;
import com.admFC.util.TemplateViewModelLocal;
import com.doxacore.modelo.Auditoria;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class EventoVM extends TemplateViewModelLocal {

	private List<Object[]> lEventos;
	private List<Object[]> lEventosOri;
	private Evento eventoSelected;
	private Auditoria auditoria;
	private List<Contribuyente> lContribuyentes;
	private Contribuyente contribuyenteSelected;

	private boolean opCrearEvento;
	private boolean opEditarEvento;
	private boolean opBorrarEvento;

	private Date desde = new Date();
	private Date hasta;

	private boolean editar = false;

	@Init(superclass = true)
	public void initEventoVM() {

		hasta = this.um.calcularFecha(this.desde, Calendar.HOUR, 24);

		lContribuyentes = this.getContribuyentesPorUsuario();

		if (lContribuyentes.size()  > 0) {

			this.contribuyenteSelected = lContribuyentes.get(0);

		}

		cargarEventos();
		inicializarFiltros();

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

	private void cargarEventos() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		
		if (lContribuyentes.size()  > 0) {
			
			String sql = this.um.getSql("evento/listaEventos.sql")
					.replace("?1", sdf.format(desde)).replace("?2", sdf.format(hasta))
					.replace("?3", this.contribuyenteSelected.getContribuyenteid() + "");
			;
	
			System.out.println("==================SQL EVENTOS=====================");
			System.out.println(sql);
			System.out.println("==================================================");
			
			this.lEventos = this.reg.sqlNativo(sql);
			this.lEventosOri = this.lEventos;
		
		}

	}
	
	

	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[10]; 
		

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
			this.eventoSelected = this.reg.getObjectById(Evento.class.getName(),
					eventoid);
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
			this.auditoria.setJson(gson.toJson(this.eventoSelected));

		} else {

			this.eventoSelected = new Evento();

		}

		modal = (Window) Executions.createComponents("/sistema/zul/documentos/eventoModal.zul",
				this.mainComponent, null);
		Selectors.wireComponents(modal, this, false);
		modal.doModal();

	}

	@Command
	@NotifyChange("lEventos")
	public void guardar() {

		this.save(this.eventoSelected);

		this.eventoSelected = null;

		this.cargarEventos();

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

	public List<Contribuyente> getlContribuyentes() {
		return lContribuyentes;
	}

	public void setlContribuyentes(List<Contribuyente> lContribuyentes) {
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

	
}
