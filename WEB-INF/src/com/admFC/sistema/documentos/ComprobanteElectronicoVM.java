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

import com.admFC.modelo.ComprobanteElectronico;
import com.admFC.modelo.Contribuyente;
import com.admFC.util.ParamsLocal;
import com.admFC.util.TemplateViewModelLocal;
import com.doxacore.modelo.Auditoria;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ComprobanteElectronicoVM extends TemplateViewModelLocal {

	private List<Object[]> lComprobantesElectronicos;
	private ComprobanteElectronico comprobanteElectronicoSelected;
	private Auditoria auditoria;
	private List<Contribuyente> lContribuyentes;
	private Contribuyente contribuyenteSelected;

	private boolean opCrearComprobanteElectronico;
	private boolean opEditarComprobanteElectronico;
	private boolean opBorrarComprobanteElectronico;

	private Date desde = new Date();
	private Date hasta;

	private boolean editar = false;

	@Init(superclass = true)
	public void initComprobanteElectronicoVM() {

		hasta = this.um.calcularFecha(this.desde, Calendar.HOUR, 24);

		lContribuyentes = this.getContribuyentesPorUsuario();

		if (lContribuyentes.size()  > 0) {

			this.contribuyenteSelected = lContribuyentes.get(0);

		}

		cargarComprobanteElectronicos();
		inicializarFiltros();

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

	private void cargarComprobanteElectronicos() {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

		String sql = this.um.getSql("comprobanteElectronico/listaComprobantesElectronicos.sql")
				.replace("?1", sdf.format(desde)).replace("?2", sdf.format(hasta))
				.replace("?3", this.contribuyenteSelected.getContribuyenteid() + "");
		;

		this.lComprobantesElectronicos = this.reg.sqlNativo(sql);

	}
	
	

	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[9]; // se debe de iniciar el filtro deacuerdo a la cantidad declarada en el
		// modelo sin id

		for (int i = 0; i < this.filtroColumns.length; i++) {

			this.filtroColumns[i] = "";

		}

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
	@NotifyChange("lComprobantesElectronicos")
	public void onChangeFiltroFechas() {

		this.cargarComprobanteElectronicos();

	}
	
	@Command
	@NotifyChange("lComprobantesElectronicos")
	public void onChangeContribuyente() {
		
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

}
