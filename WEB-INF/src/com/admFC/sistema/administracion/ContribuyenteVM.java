package com.admFC.sistema.administracion;

import java.io.IOException;
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

import com.admFC.modelo.Contribuyente;
import com.admFC.modelo.ContribuyenteContacto;
import com.admFC.modelo.ContribuyenteUsuario;
import com.admFC.util.ParamsLocal;
import com.admFC.util.TemplateViewModelLocal;
import com.admFC.util.conexionRest.HttpConexion;
import com.admFC.util.conexionRest.ResultRest;
import com.doxacore.modelo.Auditoria;
import com.doxacore.modelo.Rol;
import com.doxacore.modelo.RolOperacion;
import com.doxacore.modelo.Usuario;
import com.doxacore.modelo.UsuarioRol;
import com.doxacore.util.UtilStaticMetodos;
import com.google.gson.Gson;



public class ContribuyenteVM extends TemplateViewModelLocal{

	private List<Object[]> lContribuyentes;
	private List<Object[]> lContribuyentesOri;
	private Contribuyente contribuyenteSelected;
	private Auditoria auditoria;
	
	private boolean opCrearContribuyente;
	private boolean opEditarContribuyente;
	private boolean opBorrarContribuyente;
	private boolean opAgregarContribuyenteUsuario;
	private boolean opInactivarContribuyenteUsuario;
	
	
	private boolean editar = false;

	@Init(superclass = true)
	public void initContribuyenteVM() {

		cargarContribuyentes();
		inicializarFiltros();

	}

	@AfterCompose(superclass = true)
	public void afterComposeContribuyenteVM() {

	}

	
	@Override
	protected void inicializarOperaciones() {
		
		this.opCrearContribuyente = this.operacionHabilitada(ParamsLocal.OP_CREAR_CONTRIBUYENTE);
		this.opEditarContribuyente = this.operacionHabilitada(ParamsLocal.OP_EDITAR_CONTRIBUYENTE);
		this.opBorrarContribuyente = this.operacionHabilitada(ParamsLocal.OP_BORRAR_CONTRIBUYENTE);
		
		this.opAgregarContribuyenteUsuario = this.operacionHabilitada(ParamsLocal.OP_AGREGAR_CONTRIBUYENTEUSUARIO);
		this.opInactivarContribuyenteUsuario = this.operacionHabilitada(ParamsLocal.OP_AGREGAR_CONTRIBUYENTEUSUARIO);
	
		
	}
	
	private void cargarContribuyentes() {

		String sql = this.um.getSql("contribuyente/listaContribuyentes.sql");
		
		if (!this.isUserRolMaster()) {
		
			sql = sql.replace("--", "").replace("?1", this.getCurrentUser().getUsuarioid()+"" );
						
		}
				
		
		this.lContribuyentes = this.reg.sqlNativo(sql);
		this.lContribuyentesOri = this.lContribuyentes;
		
	}
	
	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[4]; 

		for (int i = 0; i < this.filtroColumns.length; i++) {

			this.filtroColumns[i] = "";

		}

	}
	
	@Command
	@NotifyChange("lContribuyentes")
	public void filtrarContribuyente() {
		
		this.lContribuyentes = this.filtrarListaObject(this.filtroColumns, this.lContribuyentesOri);
		
	}
	
	//Seccion modal
	
	private Window modal;
	
	@Command
	public void modalContribuyenteAgregar() {

		if(!this.isOpCrearContribuyente())
			return;

		this.editar = false;
		this.modalContribuyente(-1);

	}

	
	@Command
	public void modalContribuyente(@BindingParam("contribuyenteid") long contribuyenteid) {
		
		this.auditoria = new Auditoria();
		
		this.nombre = "";
		this.email ="";
		
		if (contribuyenteid != -1) {
			
			if (!this.opEditarContribuyente)
				return;
			
			this.editar= true;
			this.contribuyenteSelected = this.reg.getObjectById(Contribuyente.class.getName(), contribuyenteid);
			
		//	this.auditoria.setJson(new Gson().toJson(this.contribuyenteSelected));
			
		}else {
			
			this.contribuyenteSelected = new Contribuyente();
			
		}
		

		modal = (Window) Executions.createComponents("/sistema/zul/administracion/contribuyenteModal.zul",
				this.mainComponent, null);
		Selectors.wireComponents(modal, this, false);
		modal.doModal();

	}
	
	@Command
	@NotifyChange("lContribuyentes")
	public void guardar() {
		
		this.save(this.contribuyenteSelected);
		
		this.contribuyenteSelected = null;

		this.cargarContribuyentes();

		this.modal.detach();
		
		this.auditoria.setUsuario(this.getCurrentUser().getAccount());
		
		if (editar) {
			
			Notification.show("Contribuyente Actualizado.");
			this.auditoria.setSentencia("UPDATE");
			this.editar = false;
			
		}else {
			
			this.auditoria.setSentencia("INSERT");
			Notification.show("El Contribuyente fue agregado.");
		}
		
		this.reg.saveObject(this.auditoria, "SYSTEM");
		
	}
	
	//Seccion Agregar Usuario
	
	
	
	@Command
	public void modalContribuyenteUsuario(@BindingParam("contribuyenteid") long contribuyenteid) {
		
		if (!this.opAgregarContribuyenteUsuario)
			return;
		
		this.contribuyenteSelected = this.reg.getObjectById(Contribuyente.class.getName(), contribuyenteid);
		
		this.usuarioSelected = new Usuario();
		
		this.lContribuyentesUsuarios = this.reg.getAllObjectsByCondicionOrder(ContribuyenteUsuario.class.getName(),
				"contribuyenteid = " + this.contribuyenteSelected.getContribuyenteid(), "usuarioid asc");
		

		modal = (Window) Executions.createComponents("/sistema/zul/administracion/contribuyenteUsuarioModal.zul",
				this.mainComponent, null);
		Selectors.wireComponents(modal, this, false);
		modal.doModal();

	}
	
	private List<ContribuyenteUsuario> lContribuyentesUsuarios;
	private Usuario usuarioSelected;
	
	@Command
	@NotifyChange({"lContribuyentesUsuarios", "usuarioSelected"})
	public void agregarUsuario() {

		
		ContribuyenteUsuario cu = new ContribuyenteUsuario();
		this.usuarioSelected.setActivo(true);
		this.usuarioSelected.setPassword(UtilStaticMetodos.getSHA256(this.usuarioSelected.getAccount()));
	
		
		cu.setContribuyente(this.contribuyenteSelected);
		cu.setUsuario(this.usuarioSelected);
		
		lContribuyentesUsuarios.add(cu);
		
		this.usuarioSelected = new Usuario();
		

	}
	
	public void guardarUsuarios() {
		
		for (ContribuyenteUsuario x : lContribuyentesUsuarios ) {
			
			if (x.getUsuario().getUsuarioid() == null) {

				x.setUsuario(this.save(x.getUsuario()));
				
				UsuarioRol ur = new UsuarioRol();
				ur.setUsuario(x.getUsuario());
				
				Rol rol = this.reg.getObjectByColumnString(Rol.class.getName(), "rol", ParamsLocal.ROL_OPERADOR);
				ur.setRol(rol);
				this.save(ur);
				
			}else {
				

				x.setUsuario(this.save(x.getUsuario()));
				
			}
			
			
			this.save(x);
			
		}
		this.modal.detach();
		Notification.show("Lista de Usuarios Actualizada.");
	}

	
	//Seccion Contacto
	
	private String nombre;
	private String email;
	
	@Command
	@NotifyChange({"nombre", "email","contribuyenteSelected"})
	public void agregarContacto() {
		
		if (this.nombre == null || this.nombre.length()==0) {
			
			return;
			
		}
		
		if (this.email == null || this.email.length()==0) {
			
			return;
			
		}
		
		ContribuyenteContacto cc = new ContribuyenteContacto();
		
		cc.setNombre(this.nombre);
		cc.setMail(this.email);
		
		this.contribuyenteSelected.getContactos().add(cc);
		
		
		
	}
	
	@Command
	@NotifyChange({"contribuyenteSelected"})
	public void removerContacto(@BindingParam("contacto") ContribuyenteContacto contacto){
		
		this.contribuyenteSelected.getContactos().remove(contacto);
		
	}
	
	public Contribuyente getContribuyenteSelected() {
		return contribuyenteSelected;
	}

	public void setContribuyenteSelected(Contribuyente contribuyenteSelected) {
		this.contribuyenteSelected = contribuyenteSelected;
	}

	public boolean isOpCrearContribuyente() {
		return opCrearContribuyente;
	}

	public void setOpCrearContribuyente(boolean opCrearContribuyente) {
		this.opCrearContribuyente = opCrearContribuyente;
	}

	public boolean isOpEditarContribuyente() {
		return opEditarContribuyente;
	}

	public void setOpEditarContribuyente(boolean opEditarContribuyente) {
		this.opEditarContribuyente = opEditarContribuyente;
	}

	public boolean isOpBorrarContribuyente() {
		return opBorrarContribuyente;
	}

	public void setOpBorrarContribuyente(boolean opBorrarContribuyente) {
		this.opBorrarContribuyente = opBorrarContribuyente;
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

	public List<Object[]> getlContribuyentes() {
		return lContribuyentes;
	}

	public void setlContribuyentes(List<Object[]> lContribuyentes) {
		this.lContribuyentes = lContribuyentes;
	}

	public boolean isOpAgregarContribuyenteUsuario() {
		return opAgregarContribuyenteUsuario;
	}

	public void setOpAgregarContribuyenteUsuario(boolean opAgregarContribuyenteUsuario) {
		this.opAgregarContribuyenteUsuario = opAgregarContribuyenteUsuario;
	}

	public boolean isOpInactivarContribuyenteUsuario() {
		return opInactivarContribuyenteUsuario;
	}

	public void setOpInactivarContribuyenteUsuario(boolean opInactivarContribuyenteUsuario) {
		this.opInactivarContribuyenteUsuario = opInactivarContribuyenteUsuario;
	}

	public Usuario getUsuarioSelected() {
		return usuarioSelected;
	}

	public void setUsuarioSelected(Usuario usuarioSelected) {
		this.usuarioSelected = usuarioSelected;
	}

	public List<ContribuyenteUsuario> getlContribuyentesUsuarios() {
		return lContribuyentesUsuarios;
	}

	public void setlContribuyentesUsuarios(List<ContribuyenteUsuario> lContribuyentesUsuarios) {
		this.lContribuyentesUsuarios = lContribuyentesUsuarios;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	

}
