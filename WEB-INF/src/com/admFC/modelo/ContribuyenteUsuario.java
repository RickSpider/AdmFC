package com.admFC.modelo;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

import com.doxacore.modelo.Modelo;
import com.doxacore.modelo.Usuario;

@Entity
@Table(name="ContribuyentesUsuarios")
public class ContribuyenteUsuario extends Modelo implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8099632243754962602L;
	
	@EmbeddedId
	private ContribuyenteUsuarioPK contribuyenteusuariopk = new ContribuyenteUsuarioPK();

	public Contribuyente getContribuyente() {
		return this.contribuyenteusuariopk.getContribuyente();
	}
	
	public void setContribuyente(Contribuyente contribuyente) {
		
		this.getContribuyenteusuariopk().setContribuyente(contribuyente);
		
	}
	
	public Usuario getUsuario() {
		return this.contribuyenteusuariopk.getUsuario();
	}
	
	public void setUsuario(Usuario usuario) {
		
		this.getContribuyenteusuariopk().setUsuario(usuario);
		
	}
	
	
	@Override
	public Object[] getArrayObjectDatos() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getStringDatos() {
		// TODO Auto-generated method stub
		return null;
	}

	public ContribuyenteUsuarioPK getContribuyenteusuariopk() {
		return contribuyenteusuariopk;
	}

	public void setContribuyenteusuariopk(ContribuyenteUsuarioPK contribuyenteusuariopk) {
		this.contribuyenteusuariopk = contribuyenteusuariopk;
	}


}
