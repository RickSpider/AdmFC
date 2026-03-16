/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.admFC.modelo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.doxacore.modelo.Modelo;

/**
 *
 * @author BlackSpider
 */

@Entity
@Table(name = "establecimientos")
public class Establecimiento extends Modelo implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 4414269133947974997L;

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long establecimientoid;
    
   /* @ManyToOne
    @JoinColumn(name="contribuyenteid", nullable=false)
    private Contribuyente contribuyente;*/
    
    private String establecimiento;
    
    @ManyToOne
    @JoinColumn(name = "localidadid")
    private Localidad localidad;
    private String direccion;
    private String nombreFantacia;
    private String numCasa;

    public Long getEstablecimientoid() {
        return establecimientoid;
    }

    public void setEstablecimientoid(Long establecimientoid) {
        this.establecimientoid = establecimientoid;
    }
    
    public String getEstablecimiento() {
        return establecimiento;
    }

    public void setEstablecimiento(String establecimiento) {
        this.establecimiento = establecimiento;
    }

    public Localidad getLocalidad() {
        return localidad;
    }

    public void setLocalidad(Localidad localidad) {
        this.localidad = localidad;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNombreFantacia() {
        return nombreFantacia;
    }

    public void setNombreFantacia(String nombreFantacia) {
        this.nombreFantacia = nombreFantacia;
    }

    public String getNumCasa() {
        return numCasa;
    }

    public void setNumCasa(String numCasa) {
        this.numCasa = numCasa;
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
    
}
