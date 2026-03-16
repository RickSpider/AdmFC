package com.admFC.searchModel;

import com.admFC.modelo.Localidad;

public class LocalidadSM {
	
	private Long localidadid;
	private String localidad;
	private String distrito;
	private String departamento;
	
	public LocalidadSM(Long localidadid, String localidad, String distrito, String departamento) {
		super();
		this.localidadid = localidadid;
		this.localidad = localidad;
		this.distrito = distrito;
		this.departamento = departamento;
	}
	
	
	public LocalidadSM(Localidad l) {
		
		this.localidadid = l.getLocalidadid();
		this.localidad = l.getLocalidad();
		this.distrito = l.getDistrito().getDistrito();
		this.departamento = l.getDistrito().getDepartamento().getDepartamento();
		
	}
	
	public Long getLocalidadid() {
		return localidadid;
	}
	public void setLocalidadid(Long localidadid) {
		this.localidadid = localidadid;
	}
	public String getLocalidad() {
		return localidad;
	}
	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}	
	public String getDistrito() {
		return distrito;
	}
	public void setDistrito(String distrito) {
		this.distrito = distrito;
	}
	public String getDepartamento() {
		return departamento;
	}
	public void setDepartamento(String departamento) {
		this.departamento = departamento;
	}
	
	@Override
	public String toString() {
		return this.localidadid+" - "+this.localidad;
	}

}
