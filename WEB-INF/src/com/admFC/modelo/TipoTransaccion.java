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
 * @author blackspider
 */
@Entity
@Table(name="tipostransacciones")
public class TipoTransaccion extends Modelo implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -2949028643136553838L;
	
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)    
    private Long tipotransaccionid;
	
	@Column(name="codigo_sifen")
    private Long codigoSifen;
    private String descripcion;

    public Long getTipotransaccionid() {
        return tipotransaccionid;
    }

    public void setTipotransaccionid(Long tipotransaccionid) {
        this.tipotransaccionid = tipotransaccionid;
    }

    public Long getCodigoSifen() {
        return codigoSifen;
    }

    public void setCodigoSifen(Long codigoSifen) {
        this.codigoSifen = codigoSifen;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
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
