/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.admFC.modelo;


import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;


import com.doxacore.modelo.Modelo;

/**
 *
 * @author blackspider
 */
@Entity
@Table(name="Lotes")
public class Lote extends Modelo implements Serializable{
 
    /**
	 * 
	 */
	private static final long serialVersionUID = 4699374187232861148L;

	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long loteid;
    
    
    @Column(updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date fecha;
    
    @ManyToOne
    @JoinColumn(name = "contribuyenteid")
    private Contribuyente contribuyente;
    private String nro;
    
    @Column(name = "respuestaenvio", columnDefinition="text")
    private String respuestaEnvio;
    
    @Column(name = "respuestaconsulta", columnDefinition="text")
    private String respuestaconsulta;
    
    @Column(name = "estado")
    private String estado;

    public Contribuyente getContribuyente() {
        return contribuyente;
    }

    public void setContribuyente(Contribuyente contribuyente) {
        this.contribuyente = contribuyente;
    }

    public String getNro() {
        return nro;
    }

    public void setNro(String nro) {
        this.nro = nro;
    }

    public Long getLoteid() {
        return loteid;
    }

    public void setLoteid(Long loteid) {
        this.loteid = loteid;
    }

    public Date getFecha() {
        return fecha;
    }

    public void setFecha(Date fecha) {
        this.fecha = fecha;
    }

    public String getRespuestaEnvio() {
        return respuestaEnvio;
    }

    public void setRespuestaEnvio(String respuestaEnvio) {
        this.respuestaEnvio = respuestaEnvio;
    }

    public String getRespuestaconsulta() {
        return respuestaconsulta;
    }

    public void setRespuestaconsulta(String respuestaconsulta) {
        this.respuestaconsulta = respuestaconsulta;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
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
