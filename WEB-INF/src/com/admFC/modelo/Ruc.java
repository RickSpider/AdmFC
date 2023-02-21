/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.admFC.modelo;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.Table;

import com.doxacore.modelo.Modelo;

/**
 *
 * @author BlackSpider
 */
@Entity
@Table(name="rucs"
        ,indexes = {
            @Index(name="ruc_index", columnList="ruc", unique=true)
        }
)
public class Ruc extends Modelo implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -2676998714250661393L;
	
	@Id
    private Long rucid;
    @Column(nullable=false)
    private String ruc;
    private String dv;
    @Column(name = "razon_social", columnDefinition="varchar(500)")
    private String razonSocial;

    public Long getRucid() {
        return rucid;
    }

    public void setRucid(Long rucid) {
        this.rucid = rucid;
    }

    public String getRuc() {
        return ruc;
    }

    public void setRuc(String ruc) {
        this.ruc = ruc;
    }

    public String getDv() {
        return dv;
    }

    public void setDv(String dv) {
        this.dv = dv;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
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
