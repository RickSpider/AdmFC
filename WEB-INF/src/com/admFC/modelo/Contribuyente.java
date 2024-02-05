/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.admFC.modelo;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import com.doxacore.modelo.Modelo;


/**
 *
 * @author BlackSpider
 */
@Entity
@Table(name = "Contribuyentes")
public class Contribuyente extends Modelo implements Serializable {

    
    /**
	 * 
	 */
	private static final long serialVersionUID = -2225354319701466777L;
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long contribuyenteid;
    private String ruc;
    private String dv;

    /*
        FISICA
        JURIDICA
     */
    
    @ManyToOne
    @JoinColumn(name = "tipocontribuyenteid")
    private TipoContribuyente tipoContribuyente;
    private String nombre; //razon social
    private String nombreFantacia;
    private String direccion;
    @Column(name="num_casa")
    private String numCasa;

    @ManyToOne
    @JoinColumn(name = "distritoid")
    private Distrito distrito;

    private String telefono;

    private String email;
    private String sucursal;

    @ManyToOne
    @JoinColumn(name = "tipotransaccionid")
    private TipoTransaccion tipoTransaccion;

    @ManyToOne
    @JoinColumn(name = "tipoimpuestoid")
    private TipoImpuesto tipoImpuesto;

    @ManyToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinTable(
            name = "contribuyentesactividades",
            joinColumns = @JoinColumn(name = "contribuyenteid"),
            inverseJoinColumns = @JoinColumn(name = "actividadeconomicaid"))
    private Set<ActividadEconomica> actividades = new HashSet<ActividadEconomica>();

    private String pass;

    /*
    PROD = produccion
    DEV = desarrollo
     */
    private String ambiente;
    @Column(columnDefinition = "boolean default true", name="sololote")
    private boolean soloLote;
    
    private String pathkey;
    private String passkey;
    
    @Column(name="monedad")
    private String monedaD;
    
    @ManyToOne
    @JoinColumn(name = "unidadmedidaid")
    private UnidadMedida unidadMedida;
    
    private String cscid;
    private String csc;
       
    //OneToMany Unidireccional
    @OneToMany(fetch = FetchType.LAZY,cascade=CascadeType.ALL, orphanRemoval = true )
    @JoinColumn(name="contribuyenteid", nullable = false)
    private List<ContribuyenteContacto> contactos = new ArrayList<ContribuyenteContacto>();
    
    @Column(columnDefinition = "boolean default true", name="habilitado")
    private boolean habilitado;
    
  
    /**
     * 
     * PROD = produccion
     * DEV = desarrollo
     * 
     * @param ambiente 
     */
    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public String getAmbiente() {
        return ambiente;
    }
    
 //Seccion mail server
    
    @Column(name="enviaralerta", columnDefinition = "boolean default false")
    private boolean enviarAlerta;
    
    @Column(name="mailserverhost")
    private String mailServerHost;
    
    @Column(name="mailserverport")
    private Integer mailServerPort;
    
    @Column(name="mailserveruser")
    private String mailServerUser;
    
    @Column(name="mailserverpass")
    private String mailServerpass;
    
    @Column(name="smtpauth", columnDefinition = "boolean default false")
    private boolean smtpauth;
    
    @Column(name="smtpssl", columnDefinition = "boolean default false")
    private boolean smtpssl;
    
    @Column(name="smtpstarttls", columnDefinition = "boolean default false" )
    private boolean smtpstarttls;
    


    public String getPass() {
        return pass;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public Distrito getDistrito() {
        return distrito;
    }

    public void setDistrito(Distrito distrito) {
        this.distrito = distrito;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Long getContribuyenteid() {
        return contribuyenteid;
    }

    public void setContribuyenteid(Long contribuyenteid) {
        this.contribuyenteid = contribuyenteid;
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

    public TipoContribuyente getTipoContribuyente() {
        return tipoContribuyente;
    }

    public void setTipoContribuyente(TipoContribuyente tipoContribuyente) {
        this.tipoContribuyente = tipoContribuyente;
    }


    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNombreFantacia() {
        return nombreFantacia;
    }

    public void setNombreFantacia(String nombreFantacia) {
        this.nombreFantacia = nombreFantacia;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getNumCasa() {
        return numCasa;
    }

    public void setNumCasa(String numCasa) {
        this.numCasa = numCasa;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public TipoTransaccion getTipoTransaccion() {
        return tipoTransaccion;
    }

    public void setTipoTransaccion(TipoTransaccion tipoTransaccion) {
        this.tipoTransaccion = tipoTransaccion;
    }

    public TipoImpuesto getTipoImpuesto() {
        return tipoImpuesto;
    }

    public void setTipoImpuesto(TipoImpuesto tipoImpuesto) {
        this.tipoImpuesto = tipoImpuesto;
    }

    public Set<ActividadEconomica> getActividades() {
        return actividades;
    }

    public void setActividades(Set<ActividadEconomica> actividades) {
        this.actividades = actividades;
    }

    public String getPathkey() {
        return pathkey;
    }

    public void setPathkey(String pathkey) {
        this.pathkey = pathkey;
    }

    public String getMonedaD() {
        return monedaD;
    }

    public void setMonedaD(String monedaD) {
        this.monedaD = monedaD;
    }

    public String getPasskey() {
        return passkey;
    }

    public void setPasskey(String passkey) {
        this.passkey = passkey;
    }

    public UnidadMedida getUnidadMedida() {
        return unidadMedida;
    }

    public void setUnidadMedida(UnidadMedida unidadMedida) {
        this.unidadMedida = unidadMedida;
    }

    public String getCscid() {
        return cscid;
    }

    public void setCscid(String cscid) {
        this.cscid = cscid;
    }

    public String getCsc() {
        return csc;
    }

    public void setCsc(String csc) {
        this.csc = csc;
    }

    public boolean isSoloLote() {
        return soloLote;
    }

    public void setSoloLote(boolean soloLote) {
        this.soloLote = soloLote;
    }

  

    public boolean isHabilitado() {
        return habilitado;
    }

    public void setHabilitado(boolean habilitado) {
        this.habilitado = habilitado;
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

	public List<ContribuyenteContacto> getContactos() {
		return contactos;
	}

	public void setContactos(List<ContribuyenteContacto> contactos) {
		this.contactos = contactos;
	}

	public boolean isEnviarAlerta() {
		return enviarAlerta;
	}

	public void setEnviarAlerta(boolean enviarAlerta) {
		this.enviarAlerta = enviarAlerta;
	}

	public String getMailServerHost() {
		return mailServerHost;
	}

	public void setMailServerHost(String mailServerHost) {
		this.mailServerHost = mailServerHost;
	}

	public Integer getMailServerPort() {
		return mailServerPort;
	}

	public void setMailServerPort(Integer mailServerPort) {
		this.mailServerPort = mailServerPort;
	}

	public String getMailServerUser() {
		return mailServerUser;
	}

	public void setMailServerUser(String mailServerUser) {
		this.mailServerUser = mailServerUser;
	}

	public String getMailServerpass() {
		return mailServerpass;
	}

	public void setMailServerpass(String mailServerpass) {
		this.mailServerpass = mailServerpass;
	}

	public boolean isSmtpauth() {
		return smtpauth;
	}

	public void setSmtpauth(boolean smtpauth) {
		this.smtpauth = smtpauth;
	}

	public boolean isSmtpssl() {
		return smtpssl;
	}

	public void setSmtpssl(boolean smtpssl) {
		this.smtpssl = smtpssl;
	}

	public boolean isSmtpstarttls() {
		return smtpstarttls;
	}

	public void setSmtpstarttls(boolean smtpstarttls) {
		this.smtpstarttls = smtpstarttls;
	}

	
    
    
}
