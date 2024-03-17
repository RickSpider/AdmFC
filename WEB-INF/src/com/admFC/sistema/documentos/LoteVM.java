package com.admFC.sistema.documentos;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

import com.admFC.modelo.Lote;
import com.admFC.modelo.Contribuyente;
import com.admFC.modelo.Evento;
import com.admFC.util.ParamsLocal;
import com.admFC.util.TemplateViewModelLocal;
import com.doxacore.components.finder.FinderModel;
import com.doxacore.modelo.Auditoria;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class LoteVM extends TemplateViewModelLocal {

	private List<Object[]> lLotes;
	private List<Object[]> lLotesOri;
	private Lote loteSelected;
	private Auditoria auditoria;
	private List<Object[]> lContribuyentes;
	private Contribuyente contribuyenteSelected;

	private boolean opCrearLote;
	private boolean opEditarLote;
	private boolean opBorrarLote;

	private boolean camposBloqueados = false;

	private Date desde = new Date();
	private Date hasta;

	private boolean editar = false;

	@Init(superclass = true)
	public void initLoteVM() {

		
		desde = this.um.modificarHorasMinutosSegundos(new Date(), 0,0,0,0);
		
		
		hasta = this.um.modificarHorasMinutosSegundos(this.desde, 23, 59, 59, 999);

		cargarLotes();
		inicializarFiltros();
		inicializarFinders();
	}

	@AfterCompose(superclass = true)
	public void afterComposeLoteVM() {

	}

	@Override
	protected void inicializarOperaciones() {

		this.opCrearLote = this.operacionHabilitada(ParamsLocal.OP_CREAR_LOTE);
		this.opEditarLote = this.operacionHabilitada(ParamsLocal.OP_EDITAR_LOTE);
		this.opBorrarLote = this.operacionHabilitada(ParamsLocal.OP_BORRAR_LOTE);

	}

	private void cargarLotes() {
		

		if (this.contribuyenteSelected == null) {
			
			return;
			
		}


		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		
		if (lContribuyentes.size() > 0) {

			String sql = this.um.getSql("lote/listaLotes.sql").replace("?1", sdf.format(desde))
					.replace("?2", sdf.format(hasta))
					.replace("?3", this.contribuyenteSelected.getContribuyenteid() + "");
			;

			// System.out.println("==================SQL LOTES=====================");
			// System.out.println(sql);
			// System.out.println("==================================================");

			this.lLotes = this.reg.sqlNativo(sql);
			this.lLotesOri = this.lLotes;

		}

	}

	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[6];

		for (int i = 0; i < this.filtroColumns.length; i++) {

			this.filtroColumns[i] = "";

		}

	}

	@Command
	@NotifyChange("lLotes")
	public void filtrarLote() {

		this.lLotes = filtrarListaObject(this.filtroColumns, this.lLotesOri);

	}

	// Seccion modal

	private Window modal;

	@Command
	public void modalLoteAgregar() {

		if (!this.isOpCrearLote())
			return;

		this.camposBloqueados = true;
		this.editar = false;
		this.modalLote(-1);

	}

	@Command
	public void modalLote(@BindingParam("loteid") long loteid) {

		this.auditoria = new Auditoria();
		this.auditoria.setModulo("Lote");

		if (loteid != -1) {

			if (!this.opEditarLote)
				return;

			this.editar = true;
			this.camposBloqueados = true;
			this.loteSelected = this.reg.getObjectById(Lote.class.getName(), loteid);
			Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
			this.auditoria.setJson(gson.toJson(this.loteSelected));

		} else {

			this.loteSelected = new Lote();

		}

		modal = (Window) Executions.createComponents("/sistema/zul/documentos/loteModal.zul", this.mainComponent, null);
		Selectors.wireComponents(modal, this, false);
		modal.doModal();

	}

	@Command
	@NotifyChange("lLotes")
	public void guardar() {

		this.save(this.loteSelected);

		this.loteSelected = null;

		this.cargarLotes();

		this.modal.detach();

		this.auditoria.setUsuario(this.getCurrentUser().getAccount());

		if (editar) {

			Notification.show("Lote Actualizado.");
			this.editar = false;
			this.auditoria.setSentencia("UPDATE");

		} else {

			this.auditoria.setSentencia("INSERT");
			Notification.show("El Lote fue agregado.");
		}

		this.reg.saveObject(this.auditoria, "SYSTEM");

	}

	@Command
	@NotifyChange("lLotes")
	public void onChangeFiltroFechas() {

		this.cargarLotes();

	}

	@Command
	@NotifyChange("lLotes")
	public void onChangeContribuyente() {

		this.cargarLotes();

	}

	@Command
	public void borrarConfirmacion(@BindingParam("dato") long id) {

		if (!this.opBorrarLote)
			return;

		Lote lote = this.reg.getObjectById(Lote.class.getName(), id);

		EventListener event = new EventListener() {

			@Override
			public void onEvent(Event evt) throws Exception {

				if (evt.getName().equals(Messagebox.ON_YES)) {

					borrar(lote);

				}

			}

		};

		this.mensajeEliminar("El Lote sera borrado permanentemente. \n Continuar?", event);

	}

	private void borrar(Lote l) {

		this.reg.deleteObject(l);
		this.cargarLotes();
		BindUtils.postNotifyChange(null, null, this, "lLotes");
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

		this.cargarLotes();

	}

	public List<Object[]> getlLotes() {
		return lLotes;
	}

	public void setlLotes(List<Object[]> lLotes) {
		this.lLotes = lLotes;
	}

	public Lote getLoteSelected() {
		return loteSelected;
	}

	public void setLoteSelected(Lote loteSelected) {
		this.loteSelected = loteSelected;
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

	public boolean isOpCrearLote() {
		return opCrearLote;
	}

	public void setOpCrearLote(boolean opCrearLote) {
		this.opCrearLote = opCrearLote;
	}

	public boolean isOpEditarLote() {
		return opEditarLote;
	}

	public void setOpEditarLote(boolean opEditarLote) {
		this.opEditarLote = opEditarLote;
	}

	public boolean isOpBorrarLote() {
		return opBorrarLote;
	}

	public void setOpBorrarLote(boolean opBorrarLote) {
		this.opBorrarLote = opBorrarLote;
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
