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
@Table(name = "contribuyentescontactos")
public class ContribuyenteContacto  extends Modelo implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1525645509179817927L;
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long contribuyentecontactoid;
    private String nombre;
    private String mail;
    
    @ManyToOne
    @JoinColumn(name="contribuyenteid", nullable=false)
    private Contribuyente contribuyente;

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public Long getContribuyentecontactoid() {
        return contribuyentecontactoid;
    }

    public void setContribuyentecontactoid(Long contribuyentecontactoid) {
        this.contribuyentecontactoid = contribuyentecontactoid;
    }

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
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
