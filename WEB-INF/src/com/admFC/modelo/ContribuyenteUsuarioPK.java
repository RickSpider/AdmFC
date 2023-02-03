package com.admFC.modelo;

import java.io.Serializable;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import com.doxacore.modelo.Usuario;


@Embeddable
public class ContribuyenteUsuarioPK implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4483501887169731521L;

	@ManyToOne
	@JoinColumn(name="Contribuyenteid")
	private Contribuyente contribuyente = new Contribuyente();
	
	@ManyToOne
	@JoinColumn(name="usuarioid")
	private Usuario usuario = new Usuario();

	public Contribuyente getContribuyente() {
		return contribuyente;
	}

	public void setContribuyente(Contribuyente contribuyente) {
		this.contribuyente = contribuyente;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}
	
	
	
}
