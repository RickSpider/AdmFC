/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.admFC.modelo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.doxacore.modelo.Modelo;

/**
 *
 * @author blackspider
 */
@Entity
@Table(name="unidadesmedidas")
public class UnidadMedida extends Modelo implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -5093015915783202805L;
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)   
    private Long unidadmedidaid;
    private Long codigoSifen;
    private String unidadMediad;
    private String abreviatura;

    public Long getUnidadmedidaid() {
        return unidadmedidaid;
    }

    public void setUnidadmedidaid(Long unidadmedidaid) {
        this.unidadmedidaid = unidadmedidaid;
    }

    

    public Long getCodigoSifen() {
        return codigoSifen;
    }

    public void setCodigoSifen(Long codigoSifen) {
        this.codigoSifen = codigoSifen;
    }

    public String getUnidadMediad() {
        return unidadMediad;
    }

    public void setUnidadMediad(String unidadMediad) {
        this.unidadMediad = unidadMediad;
    }

    public String getAbreviatura() {
        return abreviatura;
    }

    public void setAbreviatura(String abreviatura) {
        this.abreviatura = abreviatura;
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
