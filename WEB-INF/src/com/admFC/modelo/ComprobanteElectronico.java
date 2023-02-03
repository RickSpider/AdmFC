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
import javax.persistence.Index;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


import com.doxacore.modelo.Modelo;

/**
 *
 * @author BlackSpider
 */
@Entity
@Table(name="comprobanteselectronicos"
        ,indexes = {
            @Index(name="cdc_index", columnList="cdc", unique=true)
        }
)
public class ComprobanteElectronico extends Modelo implements Serializable {
    
    /**
	 * 
	 */
	private static final long serialVersionUID = -6967460436252402871L;

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "contribuyenteid")
    private Contribuyente contribuyente;
    
    @Column(columnDefinition="text")
    private String xml;
      
    private String cdc;

    private String numero;
    private double total;
    
    @Column(columnDefinition="text")
    private String respuesta;
    
    private String estado;
    
    @Column(columnDefinition = "boolean default false")
    private boolean enviado = false;
    
    //Seccion Lote
    @Column(name = "envioporlote", columnDefinition = "boolean default false")
    private boolean envioPorLote = false;
    
    @ManyToOne
    @JoinColumn(name = "loteid")
    private Lote lote;
    
   @Column(columnDefinition = "boolean default false", name="enviadolote")
    private boolean enviadoLote = false;
  
    @ManyToOne
    @JoinColumn(name = "tipocomprobanteelectronicoid")
   private TipoComprobanteElectronico tipoComprobanteElectronico;
    
    //Seccion evento
    
    
    @ManyToOne
    @JoinColumn(name = "eventoid")
    private Evento evento;
    

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
   
    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getRespuesta() {
        return respuesta;
    }

    public void setRespuesta(String respuesta) {
        this.respuesta = respuesta;
    }

    public String getCdc() {
        return cdc;
    }
    
      public void setCdc(String cdc) {
        this.cdc = cdc;
    }

    public boolean isEnviado() {
        return enviado;
    }

    public void setEnviado(boolean enviado) {
        this.enviado = enviado;
    }

    public TipoComprobanteElectronico getTipoComprobanteElectronico() {
        return tipoComprobanteElectronico;
    }

    public void setTipoComprobanteElectronico(TipoComprobanteElectronico tipoComprobanteElectronico) {
        this.tipoComprobanteElectronico = tipoComprobanteElectronico;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public boolean isEnvioPorLote() {
        return envioPorLote;
    }

    public void setEnvioPorLote(boolean envioPorLote) {
        this.envioPorLote = envioPorLote;
    }

    public boolean isEnviadoLote() {
        return enviadoLote;
    }

    public void setEnviadoLote(boolean enviadoLote) {
        this.enviadoLote = enviadoLote;
    }

    public Evento getEvento() {
        return evento;
    }

    public void setEvento(Evento evento) {
        this.evento = evento;
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
