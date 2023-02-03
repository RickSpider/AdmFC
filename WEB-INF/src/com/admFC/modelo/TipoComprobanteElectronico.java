/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
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
@Table(name="tiposcomprobanteselectronicos")
public class TipoComprobanteElectronico extends Modelo implements Serializable{
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -5101587798194043350L;

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)   
    private Long tipocomprobanteelectronicoid;
    
    @Column(name="tipocomprobanteelectronico")
    private String tipoComprobanteElectronico;
    
    public TipoComprobanteElectronico(){}

    public TipoComprobanteElectronico(Long tipocomprobanteelectronicoid) {
        this.tipocomprobanteelectronicoid = tipocomprobanteelectronicoid;
    }

    public TipoComprobanteElectronico(Long tipocomprobanteelectronicoid, String tipoComprobanteElectronico) {
        this.tipocomprobanteelectronicoid = tipocomprobanteelectronicoid;
        this.tipoComprobanteElectronico = tipoComprobanteElectronico;
    }
    
    public Long getTipocomprobanteelectronicoid() {
        return tipocomprobanteelectronicoid;
    }

    public void setTipocomprobanteelectronicoid(Long tipocomprobanteelectronicoid) {
        this.tipocomprobanteelectronicoid = tipocomprobanteelectronicoid;
    }

    public String getTipoComprobanteElectronico() {
        return tipoComprobanteElectronico;
    }

    public void setTipoComprobanteElectronico(String tipoComprobanteElectronico) {
        this.tipoComprobanteElectronico = tipoComprobanteElectronico;
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
