/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.admFC.modelo;
import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.doxacore.modelo.Modelo;

/**
 *
 * @author BlackSpider
 */

@Entity
@Table(name="tiposcontribuyentes")
public class TipoContribuyente extends Modelo implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -4718006219686655140L;
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)    
    private Long tipocontribuyenteid;
	
	@Column(name="tipo_contribuyente")
    private String tipoContribuyente;
	
	@Column(name="codigo_sifen")
    private Long codigoSifen;

    public Long getCodigoSifen() {
        return codigoSifen;
    }

    public void setCodigoSifen(Long codigoSifen) {
        this.codigoSifen = codigoSifen;
    }
   
    public Long getTipocontribuyenteid() {
        return tipocontribuyenteid;
    }

    public void setTipocontribuyenteid(Long tipocontribuyenteid) {
        this.tipocontribuyenteid = tipocontribuyenteid;
    }

    public String getTipoContribuyente() {
        return tipoContribuyente;
    }

    public void setTipoContribuyente(String tipoContribuyente) {
        this.tipoContribuyente = tipoContribuyente;
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
