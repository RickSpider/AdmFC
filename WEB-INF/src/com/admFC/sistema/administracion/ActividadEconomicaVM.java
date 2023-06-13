package com.admFC.sistema.administracion;

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

import com.admFC.modelo.ActividadEconomica;
import com.admFC.util.ParamsLocal;
import com.admFC.util.TemplateViewModelLocal;
import com.doxacore.modelo.Auditoria;
import com.google.gson.Gson;




public class ActividadEconomicaVM extends TemplateViewModelLocal{

	private List<Object[]> lActividadesEconomicas;
	private List<Object[]> lActividadesEconomicasOri;
	private ActividadEconomica actividadEconomicaSelected;
	private Auditoria auditoria;
	
	private boolean opCrearActividadEconomica;
	private boolean opEditarActividadEconomica;
	private boolean opBorrarActividadEconomica;
	
	
	
	private boolean editar = false;

	@Init(superclass = true)
	public void initActividadEconomicaVM() {

		cargarActividadesEconomicas();
		inicializarFiltros();

	}

	@AfterCompose(superclass = true)
	public void afterComposeActividadEconomicaVM() {

	}

	
	@Override
	protected void inicializarOperaciones() {
		
		this.opCrearActividadEconomica = this.operacionHabilitada(ParamsLocal.OP_CREAR_ACTIVIDADECONOMICA);
		this.opEditarActividadEconomica = this.operacionHabilitada(ParamsLocal.OP_EDITAR_ACTIVIDADECONOMICA);
		this.opBorrarActividadEconomica = this.operacionHabilitada(ParamsLocal.OP_BORRAR_ACTIVIDADECONOMICA);
	
		
	}
	
	private void cargarActividadesEconomicas() {

		String sql = this.um.getSql("actividadEconomica/listaActividadesEconomicas.sql");
		
		this.lActividadesEconomicas = this.reg.sqlNativo(sql);
		this.lActividadesEconomicasOri = this.lActividadesEconomicas;
		
	}
	
	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[4]; 

		for (int i = 0; i < this.filtroColumns.length; i++) {

			this.filtroColumns[i] = "";

		}

	}
	
	@Command
	@NotifyChange("lActividadesEconomicas")
	public void filtrarActividadEconomica() {
		
		this.lActividadesEconomicas = this.filtrarListaObject(this.filtroColumns, this.lActividadesEconomicasOri);
		
	}
	
	//Seccion modal
	
	private Window modal;
	
	@Command
	public void modalActividadEconomicaAgregar() {

		if(!this.isOpCrearActividadEconomica())
			return;

		this.editar = false;
		this.modalActividadEconomica(-1);

	}

	
	@Command
	public void modalActividadEconomica(@BindingParam("actividadEconomicaid") long actividadEconomicaid) {
		
		this.auditoria = new Auditoria();
		
		if (actividadEconomicaid != -1) {
			
			if (!this.opEditarActividadEconomica)
				return;
			
			this.editar= true;
			this.actividadEconomicaSelected = this.reg.getObjectById(ActividadEconomica.class.getName(), actividadEconomicaid);
			
			this.auditoria.setJson(new Gson().toJson(this.actividadEconomicaSelected));
			
		}else {
			
			this.actividadEconomicaSelected = new ActividadEconomica();
		}
		

		modal = (Window) Executions.createComponents("/sistema/zul/administracion/actividadEconomicaModal.zul",
				this.mainComponent, null);
		Selectors.wireComponents(modal, this, false);
		modal.doModal();

	}
	
	@Command
	@NotifyChange("lActividadesEconomicas")
	public void guardar() {
		
		this.save(this.actividadEconomicaSelected);
		
		this.actividadEconomicaSelected = null;

		this.cargarActividadesEconomicas();

		this.modal.detach();
		
		this.auditoria.setUsuario(this.getCurrentUser().getAccount());
		
		if (editar) {
			
			Notification.show("ActividadEconomica Actualizado.");
			this.auditoria.setSentencia("UPDATE");
			this.editar = false;
			
		}else {
			
			this.auditoria.setSentencia("INSERT");
			Notification.show("El ActividadEconomica fue agregado.");
		}
		
		this.reg.saveObject(this.auditoria, "SYSTEM");
		
	}
	
	
	public ActividadEconomica getActividadEconomicaSelected() {
		return actividadEconomicaSelected;
	}

	public void setActividadEconomicaSelected(ActividadEconomica actividadEconomicaSelected) {
		this.actividadEconomicaSelected = actividadEconomicaSelected;
	}

	public boolean isOpCrearActividadEconomica() {
		return opCrearActividadEconomica;
	}

	public void setOpCrearActividadEconomica(boolean opCrearActividadEconomica) {
		this.opCrearActividadEconomica = opCrearActividadEconomica;
	}

	public boolean isOpEditarActividadEconomica() {
		return opEditarActividadEconomica;
	}

	public void setOpEditarActividadEconomica(boolean opEditarActividadEconomica) {
		this.opEditarActividadEconomica = opEditarActividadEconomica;
	}

	public boolean isOpBorrarActividadEconomica() {
		return opBorrarActividadEconomica;
	}

	public void setOpBorrarActividadEconomica(boolean opBorrarActividadEconomica) {
		this.opBorrarActividadEconomica = opBorrarActividadEconomica;
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

	public List<Object[]> getlActividadesEconomicas() {
		return lActividadesEconomicas;
	}

	public void setlActividadesEconomicas(List<Object[]> lActividadesEconomicas) {
		this.lActividadesEconomicas = lActividadesEconomicas;
	}	

}
