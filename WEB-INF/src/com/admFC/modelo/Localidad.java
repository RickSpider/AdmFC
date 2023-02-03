/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.admFC.modelo;
import java.io.Serializable;
import javax.persistence.Entity;
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
@Table(name = "Localidades")
public class Localidad extends Modelo implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = 1416069709017578890L;
	@Id
    private Long localidadid;
    private String localidad;
    private Long codigoSifen;
    
    @ManyToOne
    @JoinColumn(name = "distritoid")
    private Distrito distrito;

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

    public Long getCodigoSifen() {
        return codigoSifen;
    }

    public void setCodigoSifen(Long codigoSifen) {
        this.codigoSifen = codigoSifen;
    }

    public Distrito getDistrito() {
        return distrito;
    }

    public void setDistrito(Distrito distrito) {
        this.distrito = distrito;
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
