package com.admFC.sistema.administracion;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.image.AImage;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.util.Notification;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.admFC.modelo.ActividadEconomica;
import com.admFC.modelo.Contribuyente;
import com.admFC.modelo.ContribuyenteContacto;
import com.admFC.modelo.ContribuyenteUsuario;
import com.admFC.modelo.Localidad;
import com.admFC.modelo.TipoContribuyente;
import com.admFC.modelo.TipoImpuesto;
import com.admFC.modelo.TipoTransaccion;
import com.admFC.util.ParamsLocal;
import com.admFC.util.TemplateViewModelLocal;
import com.doxacore.modelo.Auditoria;
import com.doxacore.modelo.Rol;
import com.doxacore.modelo.Tipo;
import com.doxacore.modelo.Tipotipo;
import com.doxacore.modelo.Usuario;
import com.doxacore.modelo.UsuarioRol;
import com.doxacore.util.UtilStaticMetodos;
import com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContribuyenteVM extends TemplateViewModelLocal {

	private List<Object[]> lContribuyentes;
	private List<Object[]> lContribuyentesOri;
	private Contribuyente contribuyenteSelected;
	private Auditoria auditoria;

	private boolean opCrearContribuyente;
	private boolean opEditarContribuyente;
	private boolean opBorrarContribuyente;
	private boolean opAgregarContribuyenteUsuario;
	private boolean opInactivarContribuyenteUsuario;

	private boolean editar = false;
	
	private ListModelList<Tipo> etiquetas;

	@Init(superclass = true)
	public void initContribuyenteVM() {

		
		inicializarFiltros();
		cargarContribuyentes();

	}

	@AfterCompose(superclass = true)
	public void afterComposeContribuyenteVM() {

	}

	@Override
	protected void inicializarOperaciones() {

		this.opCrearContribuyente = this.operacionHabilitada(ParamsLocal.OP_CREAR_CONTRIBUYENTE);
		this.opEditarContribuyente = this.operacionHabilitada(ParamsLocal.OP_EDITAR_CONTRIBUYENTE);
		this.opBorrarContribuyente = this.operacionHabilitada(ParamsLocal.OP_BORRAR_CONTRIBUYENTE);

		this.opAgregarContribuyenteUsuario = this.operacionHabilitada(ParamsLocal.OP_AGREGAR_CONTRIBUYENTEUSUARIO);
		this.opInactivarContribuyenteUsuario = this.operacionHabilitada(ParamsLocal.OP_AGREGAR_CONTRIBUYENTEUSUARIO);

	}

	private void cargarContribuyentes() {

		String sql = this.um.getSql("contribuyente/listaContribuyentes.sql");

		if (!this.isUserRolMaster() && !this.isUserRolAdmin()) {

			sql = sql.replace("--", "").replace("?1", this.getCurrentUser().getUsuarioid() + "");

		}

		this.lContribuyentes = this.reg.sqlNativo(sql);
		this.lContribuyentesOri = this.lContribuyentes;
		
		this.filtrarContribuyente();

	}

	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[7];

		for (int i = 0; i < this.filtroColumns.length; i++) {

			this.filtroColumns[i] = "";

		}

	}

	@Command
	@NotifyChange("lContribuyentes")
	public void filtrarContribuyente() {

		this.lContribuyentes = this.filtrarListaObject(this.filtroColumns, this.lContribuyentesOri);

	}

	// Seccion modal

	private Window modal;

	@Command
	public void modalContribuyenteAgregar() {

		if (!this.isOpCrearContribuyente())
			return;

		this.editar = false;
		this.modalContribuyente(-1);

	}

	@Command
	public void modalContribuyente(@BindingParam("contribuyenteid") long contribuyenteid) {

		this.auditoria = new Auditoria();
		this.auditoria.setModulo("Contribuyente");
		
		this.logoFile = null;

		this.nombre = "";
		this.email = "";
		
		this.buscarTipoContribuyente = "";
		this.buscarTipoTransaccion = "";
		this.buscarTipoImpuesto = "";
		this.buscarDistrito = "";
		
		Tipotipo tt = this.reg.getObjectBySigla(Tipotipo.class.getName(), "ETIQUETA");
		
		
		List<Tipo> lEtiquetas = this.reg.getAllObjectsByCondicionOrder(Tipo.class.getName(),
				"tipotipoid = " + tt.getTipotipoid(), "tipoid asc");
		
		this.etiquetas = new ListModelList<Tipo>(lEtiquetas);
		
		//this.generarSearchModels();
		this.generarBuscadores();

		if (contribuyenteid != -1) {

			if (!this.opEditarContribuyente)
				return;

			this.editar = true;
			this.contribuyenteSelected = this.reg.getObjectById(Contribuyente.class.getName(), contribuyenteid);

			

			if (this.contribuyenteSelected.getLocalidad() != null) {

				this.buscarDistrito = this.contribuyenteSelected.getLocalidad().getLocalidad();
				//this.localidadSearchModelSelected = new LocalidadSearchModel(this.contribuyenteSelected.getLocalidad());

			}

			if (this.contribuyenteSelected.getTipoContribuyente() != null) {

				this.buscarTipoContribuyente = this.contribuyenteSelected.getTipoContribuyente().getTipoContribuyente();

			}

			if (this.contribuyenteSelected.getTipoTransaccion() != null) {

				this.buscarTipoTransaccion = this.contribuyenteSelected.getTipoTransaccion().getDescripcion();

			}

			if (this.contribuyenteSelected.getTipoImpuesto() != null) {

				this.buscarTipoImpuesto = this.contribuyenteSelected.getTipoImpuesto().getDescripcion();

			}

			for (Tipo t : this.contribuyenteSelected.getEtiquetas()) {
				
				this.etiquetas.addToSelection(t);
			}
			
			if (this.contribuyenteSelected.getLogo() != null) {
				
				try {
					this.logoFile = new AImage("logo.png",this.contribuyenteSelected.getLogo());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
			this.auditoria.setJson(new Gson().toJson(this.contribuyenteSelected));
			
			

		} else {

			this.contribuyenteSelected = new Contribuyente();
			this.contribuyenteSelected.setPass(UtilStaticMetodos.getSHA256(this.GenerarStringAleatorio()));
		}

		modal = (Window) Executions.createComponents("/sistema/zul/administracion/contribuyenteModal.zul",
				this.mainComponent, null);
		Selectors.wireComponents(modal, this, false);
		modal.doModal();

	}

	@Command
	@NotifyChange("contribuyenteSelected")
	public void regenerarPass() {

		this.contribuyenteSelected.setPass(UtilStaticMetodos.getSHA256(this.GenerarStringAleatorio()));

	}

	@Command
	@NotifyChange("lContribuyentes")
	public void guardar() {

		if (this.contribuyenteSelected.getActividades().size() <= 0) {

			this.mensajeInfo("Debes de Cargar Almenos una actividad");
			return;
		}

		this.contribuyenteSelected = this.save(this.contribuyenteSelected);
		if (this.contribuyenteSelected.getPathkey() != null || !this.contribuyenteSelected.getPasskey().isEmpty()) {
		
			this.guardarCert();
			
		}
		
		/*this.contribuyenteSelected.setLocalidad(Optional.ofNullable(this.localidadSearchModelSelected)
	            .map(LocalidadSearchModel::getLocalidadObject)
	            .orElse(null));*/


		if (editar) {

			Notification.show("Contribuyente Actualizado.");
			this.auditoria.setSentencia("UPDATE");
			this.editar = false;

		} else {
			
			if (!this.isUserRolMaster() && !this.isUserRolAdmin()) {
				
				ContribuyenteUsuario cu = new ContribuyenteUsuario();
				
				cu.setContribuyente(contribuyenteSelected);
				cu.setUsuario(this.getCurrentUser());
				
			}

			this.auditoria.setSentencia("INSERT");
			Notification.show("El Contribuyente fue agregado.");
		}
		
		this.contribuyenteSelected = null;

		this.modal.detach();

		this.auditoria.setUsuario(this.getCurrentUser().getAccount());

		this.reg.saveObject(this.auditoria, "SYSTEM");
		
		this.cargarContribuyentes();

	}

	// Seccion Agregar Usuario

	@Command
	public void modalContribuyenteUsuario(@BindingParam("contribuyenteid") long contribuyenteid) {

		if (!this.opAgregarContribuyenteUsuario)
			return;

		this.contribuyenteSelected = this.reg.getObjectById(Contribuyente.class.getName(), contribuyenteid);

		this.usuarioSelected = new Usuario();

		this.lContribuyentesUsuarios = this.reg.getAllObjectsByCondicionOrder(ContribuyenteUsuario.class.getName(),
				"contribuyenteid = " + this.contribuyenteSelected.getContribuyenteid(), "usuarioid asc");

		modal = (Window) Executions.createComponents("/sistema/zul/administracion/contribuyenteUsuarioModal.zul",
				this.mainComponent, null);
		Selectors.wireComponents(modal, this, false);
		modal.doModal();

	}

	private List<ContribuyenteUsuario> lContribuyentesUsuarios;
	private Usuario usuarioSelected;

	@Command
	@NotifyChange("usuarioSelected")
	public void onBlurUsuario() {

		Usuario us = this.reg.getObjectByColumnString(Usuario.class.getName(), "account",
				this.usuarioSelected.getAccount());

		if (us != null) {

			this.usuarioSelected = us;

		}

	}

	@Command
	@NotifyChange({ "lContribuyentesUsuarios", "usuarioSelected" })
	public void agregarUsuario() {

		ContribuyenteUsuario cu = new ContribuyenteUsuario();

		if (this.usuarioSelected.getUsuarioid() == null) {

			this.usuarioSelected.setActivo(true);
			this.usuarioSelected.setPassword(UtilStaticMetodos.getSHA256(this.usuarioSelected.getAccount()));

		}

		cu.setContribuyente(this.contribuyenteSelected);
		cu.setUsuario(this.usuarioSelected);

		lContribuyentesUsuarios.add(cu);

		this.usuarioSelected = new Usuario();

	}

	public void guardarUsuarios() {

		for (ContribuyenteUsuario x : lContribuyentesUsuarios) {

			if (x.getUsuario().getUsuarioid() == null) {

				x.setUsuario(this.save(x.getUsuario()));

				UsuarioRol ur = new UsuarioRol();
				ur.setUsuario(x.getUsuario());

				Rol rol = this.reg.getObjectByColumnString(Rol.class.getName(), "rol", ParamsLocal.ROL_OPERADOR);
				ur.setRol(rol);
				this.save(ur);

			} else {

				x.setUsuario(this.save(x.getUsuario()));

			}

			this.save(x);

		}
		this.modal.detach();
		Notification.show("Lista de Usuarios Actualizada.");
	}

	// Seccion Contacto

	private String nombre;
	private String email;

	@Command
	@NotifyChange({ "nombre", "email", "contribuyenteSelected" })
	public void agregarContacto() {

		if (this.nombre == null || this.nombre.length() == 0) {

			return;

		}

		if (this.email == null || this.email.length() == 0) {

			return;

		}

		ContribuyenteContacto cc = new ContribuyenteContacto();

		cc.setNombre(this.nombre);
		cc.setMail(this.email);

		this.contribuyenteSelected.getContactos().add(cc);

		this.nombre = "";
		this.email = "";
		
		

	}

	@Command
	@NotifyChange({ "contribuyenteSelected" })
	public void removerContacto(@BindingParam("contacto") ContribuyenteContacto contacto) {

		this.contribuyenteSelected.getContactos().remove(contacto);

	}

	public String GenerarStringAleatorio() {
		int length = 8; // Longitud del string aleatorio
		String caracteres = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890!$%&";
		StringBuilder sb = new StringBuilder();

		Random random = new Random();
		for (int i = 0; i < length; i++) {
			int index = random.nextInt(caracteres.length());
			char randomChar = caracteres.charAt(index);
			sb.append(randomChar);
		}

		String stringAleatorio = sb.toString();
		// System.out.println("String aleatorio: " + stringAleatorio);
		return stringAleatorio;
	}

	// Seccion Buscadores
	
	/*private ListModelArray<LocalidadSearchModel> lLocalidadSearchModel;
	private LocalidadSearchModel localidadSearchModelSelected;
	
	private void generarSearchModels() {
		
		 this.lLocalidadSearchModel = crearSearchModel(
			        this.um.getSql("buscadores/buscarLocalidad.sql"),
			        o -> new LocalidadSearchModel(
			        	Long.parseLong(o[0].toString()),
			        	o[2] == null ? "" : o[2].toString(),
			            o[3] == null ? "" : o[2].toString(),
			            o[4] == null ? "" : o[4].toString()
			        )
			    );
		
	}
	
	
	
	private <T> ListModelArray<T> crearSearchModel(String sql, java.util.function.Function<Object[], T> mapper) {
	    List<Object[]> resultados = this.reg.sqlNativo(sql);
	    List<T> lista = new ArrayList<>(resultados.size());

	    for (Object[] fila : resultados) {
	        lista.add(mapper.apply(fila));
	    }

	    ListModelArray<T> modelo = new ListModelArray<>(lista);
	    return modelo;
	}*/
	
	
	public void generarBuscadores() {
		
		String sql = this.um.getSql("buscadores/buscarLocalidad.sql");
		this.lDistritosBuscar = this.reg.sqlNativo(sql);
		this.lDistritosBuscarOri = this.lDistritosBuscar;
		
		String sql2 = this.um.getSql("buscadores/buscarTipoImpuesto.sql");
		this.lTiposImpuestosBuscar = this.reg.sqlNativo(sql2);
		this.lTiposImpuestosBuscarOri = this.lTiposImpuestosBuscar;
		
		String sql3 = this.um.getSql("buscadores/buscarTipoTransaccion.sql");
		this.lTiposTransaccionesBuscar = this.reg.sqlNativo(sql3);
		this.lTiposTransaccionesBuscarOri = this.lTiposTransaccionesBuscar;
		
		String sql4 = this.um.getSql("buscadores/buscarTipoContribuyente.sql");
		this.lTiposContribuyentesBuscar = this.reg.sqlNativo(sql4);
		this.lTiposContribuyentesBuscarOri = this.lTiposContribuyentesBuscar;
		
		String sql5 = this.um.getSql("buscadores/buscarActividadEconomica.sql");
		this.lActividadesEconomicasBuscar = this.reg.sqlNativo(sql5);
		this.lActividadesEconomicasBuscarOri = this.lActividadesEconomicasBuscar;

		
	}

	//LOCALIDAD
	private List<Object[]> lDistritosBuscarOri = null;
	private List<Object[]> lDistritosBuscar = null;
	private String buscarDistrito = "";

	/*@Command
	@NotifyChange("lDistritosBuscar")
	public void generarListaDistritos() {

		String sql = this.um.getSql("buscadores/buscarLocalidad.sql");

		this.lDistritosBuscar = this.reg.sqlNativo(sql);

		this.lDistritosBuscarOri = this.lDistritosBuscar;

	}*/

	@Command
	@NotifyChange("lDistritosBuscar")
	public void filtrarDistritoBuscar() {

		this.lDistritosBuscar = this.filtrarListaObject(buscarDistrito, this.lDistritosBuscarOri);

	}

	@Command
	@NotifyChange("buscarDistrito")
	public void onSelectDistrito(@BindingParam("id") long id) {

		this.contribuyenteSelected.setLocalidad(this.reg.getObjectById(Localidad.class.getName(), id));
		this.buscarDistrito = this.contribuyenteSelected.getLocalidad().getLocalidad();

	}

	// tipoImpuesto
	private List<Object[]> lTiposImpuestosBuscarOri = null;
	private List<Object[]> lTiposImpuestosBuscar = null;
	private String buscarTipoImpuesto = "";

	/*@Command
	@NotifyChange("lTiposImpuestosBuscar")
	public void generarListaTiposImpuestos() {

		String sql = this.um.getSql("buscadores/buscarTipoImpuesto.sql");

		this.lTiposImpuestosBuscar = this.reg.sqlNativo(sql);

		this.lTiposImpuestosBuscarOri = this.lTiposImpuestosBuscar;

	}*/

	@Command
	@NotifyChange("lTiposImpuestosBuscar")
	public void filtrarTipoImpuestoBuscar() {

		this.lTiposImpuestosBuscar = this.filtrarListaObject(buscarTipoImpuesto, this.lTiposImpuestosBuscarOri);

	}

	@Command
	@NotifyChange("buscarTipoImpuesto")
	public void onSelectTipoImpuesto(@BindingParam("id") long id) {

		this.contribuyenteSelected.setTipoImpuesto(this.reg.getObjectById(TipoImpuesto.class.getName(), id));
		this.buscarTipoImpuesto = this.contribuyenteSelected.getTipoImpuesto().getDescripcion();

	}
	
	
	// tipotransaccion
	private List<Object[]> lTiposTransaccionesBuscarOri = null;
	private List<Object[]> lTiposTransaccionesBuscar = null;
	private String buscarTipoTransaccion = "";

/*	@Command
	@NotifyChange("lTiposTransaccionesBuscar")
	public void generarListaTiposTransacciones() {

		String sql = this.um.getSql("buscadores/buscarTipoTransaccion.sql");

		this.lTiposTransaccionesBuscar = this.reg.sqlNativo(sql);

		this.lTiposTransaccionesBuscarOri = this.lTiposTransaccionesBuscar;

	}*/

	@Command
	@NotifyChange("lTiposTransaccionesBuscar")
	public void filtrarTipoTransaccionBuscar() {

		this.lTiposTransaccionesBuscar = this.filtrarListaObject(buscarTipoTransaccion,
				this.lTiposTransaccionesBuscarOri);

	}

	@Command
	@NotifyChange("buscarTipoTransaccion")
	public void onSelectTipoTransaccion(@BindingParam("id") long id) {

		this.contribuyenteSelected.setTipoTransaccion(this.reg.getObjectById(TipoTransaccion.class.getName(), id));
		this.buscarTipoTransaccion = this.contribuyenteSelected.getTipoTransaccion().getDescripcion();

	}

	// TipoContribuyente
	private List<Object[]> lTiposContribuyentesBuscarOri = null;
	private List<Object[]> lTiposContribuyentesBuscar = null;
	private String buscarTipoContribuyente = "";

	/*@Command
	@NotifyChange("lTiposContribuyentesBuscar")
	public void generarListaTiposContribuyentes() {

		String sql = this.um.getSql("buscadores/buscarTipoContribuyente.sql");

		this.lTiposContribuyentesBuscar = this.reg.sqlNativo(sql);

		this.lTiposContribuyentesBuscarOri = this.lTiposContribuyentesBuscar;

	}*/

	@Command
	@NotifyChange("lTiposContribuyentesBuscar")
	public void filtrarTipoContribuyenteBuscar() {

		this.lTiposContribuyentesBuscar = this.filtrarListaObject(buscarTipoContribuyente,
				this.lTiposContribuyentesBuscarOri);

	}

	@Command
	@NotifyChange("buscarTipoContribuyente")
	public void onSelectTipoContribuyente(@BindingParam("id") long id) {

		this.contribuyenteSelected.setTipoContribuyente(this.reg.getObjectById(TipoContribuyente.class.getName(), id));
		this.buscarTipoContribuyente = this.contribuyenteSelected.getTipoContribuyente().getTipoContribuyente();

	}

	// ActividadEconomica
	private List<Object[]> lActividadesEconomicasBuscarOri = null;
	private List<Object[]> lActividadesEconomicasBuscar = null;
	private ActividadEconomica actividadEconomicaSelected = null;
	private String buscarActividadEconomica = "";

	/*@Command
	@NotifyChange("lActividadesEconomicasBuscar")
	public void generarListaActividadesEconomicas() {

		String sql = this.um.getSql("buscadores/buscarActividadEconomica.sql");

		this.lActividadesEconomicasBuscar = this.reg.sqlNativo(sql);

		this.lActividadesEconomicasBuscarOri = this.lActividadesEconomicasBuscar;

	}*/

	@Command
	@NotifyChange("lActividadesEconomicasBuscar")
	public void filtrarActividadEconomicaBuscar() {

		this.lActividadesEconomicasBuscar = this.filtrarListaObject(buscarActividadEconomica,
				this.lActividadesEconomicasBuscarOri);

	}

	@Command
	@NotifyChange("buscarActividadEconomica")
	public void onSelectActividadEconomica(@BindingParam("id") long id) {

		this.actividadEconomicaSelected = this.reg.getObjectById(ActividadEconomica.class.getName(), id);

		this.buscarActividadEconomica = this.actividadEconomicaSelected.getDescripcion();
	}

	@Command
	@NotifyChange({ "buscarActividadEconomica" })
	public void agregarActividadEconomica() {

		if (this.actividadEconomicaSelected == null) {

			return;

		}

		for (ActividadEconomica x : this.contribuyenteSelected.getActividades()) {

			if (x.getActividadeconomicaid().longValue() == this.actividadEconomicaSelected.getActividadeconomicaid()
					.longValue()) {
				this.actividadEconomicaSelected = null;
				this.buscarActividadEconomica = "";
				return;

			}

		}

		this.contribuyenteSelected.getActividades().add(this.actividadEconomicaSelected);
		this.actividadEconomicaSelected = null;
		this.buscarActividadEconomica = "";
		BindUtils.postNotifyChange(null, null, this.contribuyenteSelected, "actividades");
	}

	@Command
	@NotifyChange({ "contribuyenteSelected" })
	public void removerActividadEconomica(@BindingParam("actividadEconomica") ActividadEconomica actividadEconomica) {

		this.contribuyenteSelected.getActividades().remove(actividadEconomica);

	}

	// fin buscadores

	private Media certFile;

	@Command
	public void uploadFile(@BindingParam("file") Media file) {

	//	System.out.println("formato:" + file.getName());

		if (file.getName().contains(".pfx")) {

			//System.out.println("es el archivo");

			Date vencimiento = this.getVencimientoKey(file.getByteData());

			if (vencimiento != null) {

				certFile = file;

				this.contribuyenteSelected.setVencimientokey(vencimiento);

				String directorio = this.getSistemaPropiedad("FE_CERTDIR").getValor()+"/"+this.contribuyenteSelected.getAmbiente();

				this.contribuyenteSelected
						.setPathkey(directorio + "/" + this.contribuyenteSelected.getRuc() + "/" + file.getName());
			}

			// this.guardarArchivo();

		} else {

			this.mensajeInfo("Archivo no valido.");
			this.certFile = null;

		}
		
		BindUtils.postNotifyChange(null, null, this.contribuyenteSelected, "vencimientokey");
		BindUtils.postNotifyChange(null, null, this.contribuyenteSelected, "pathkey");

	}
	
	@Command
	public void refreshVencimiento() {
	
		try {
					
			Path path = Paths.get(this.contribuyenteSelected.getPathkey());
			
			Date vencimiento = this.getVencimientoKey(Files.readAllBytes(path)); 
			
			if (vencimiento != null) {
				
				System.out.println(vencimiento);
				
				this.contribuyenteSelected.setVencimientokey(vencimiento);
				
			}else {
				
				this.mensajeError("No se pudo actualizar la fecha de vencimiento.");
				
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}
		
		BindUtils.postNotifyChange(null, null, this.contribuyenteSelected, "vencimientokey");
		
	}

	public Date getVencimientoKey(byte[] fis) {

		try {

			KeyStore ks = KeyStore.getInstance("PKCS12");
			// ks.load(fis, password.toCharArray());
			ks.load(new ByteArrayInputStream(fis), this.contribuyenteSelected.getPasskey().toCharArray());

			// Obtener el certificado del almacén de claves
			String alias = ks.aliases().nextElement();
			Certificate cert = ks.getCertificate(alias);

			// Verificar que el certificado sea de tipo X509
			if (cert instanceof X509Certificate) {
				X509Certificate x509Cert = (X509Certificate) cert;
				// Verificar la validez del certificado
				x509Cert.checkValidity();
				// Si no hay excepciones, el certificado es válido

				System.out.println("El certificado es válido hasta: " + x509Cert.getNotAfter());

				return x509Cert.getNotAfter();

			} else {
				System.err.println("El certificado no es de tipo X509.");

				return null;
			}

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		} catch (CertificateException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		} catch (KeyStoreException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}

		return null;

	}

	public void guardarCert() {

		if (this.certFile == null) {

			return;

		}
		
		File cert = new File(this.contribuyenteSelected.getPathkey());

		Path directorioPath = Paths.get(cert.getParent());

		try {

			if (!Files.exists(directorioPath)) {

				Files.createDirectories(directorioPath);
			}

			try (InputStream inputStream = this.certFile.getStreamData();

					OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(this.contribuyenteSelected.getPathkey()))) {

				byte[] buffer = new byte[65536];
				int bytesRead;
				while ((bytesRead = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

			} catch (FileNotFoundException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.out.println(e);
		}

	}
	
	@Command
	public void descargarPFX () throws FileNotFoundException {
		
		
		File archivo = new File(this.contribuyenteSelected.getPathkey());
		
		if (archivo.exists() && archivo.isFile()) {
            InputStream is = new FileInputStream(archivo);
            // El navegador sabrá que es gzip y descargará
            Filedownload.save(is, "application/x-pkcs12", archivo.getName());
        } else {
            // Podrías mostrar un mensaje con Clients.showNotification
        	
        	Notification.show("Error al descargar la firma");
        	
            throw new RuntimeException("Archivo no encontrado: " + archivo.getAbsolutePath());
            
        }
		
		
	}
	
	@Command
	@NotifyChange("lContribuyentes")
	public void ordenar(@BindingParam("indice") int indice,
	                    @BindingParam("asc") boolean asc) {
	    this.lContribuyentes.sort((a, b) -> {
	        Date d1 = (Date) ((Object[]) a)[indice];
	        Date d2 = (Date) ((Object[]) b)[indice];
	        int cmp = d1.compareTo(d2);
	        return asc ? cmp : -cmp;
	    });
	}
	
	
	@Command
	public void borrarContribuyenteConfirmacion(@BindingParam("contribuyenteid") long id) {

		if (!this.opBorrarContribuyente)
			return;
		
		List<Object[]> comprobantes = this.reg.sqlNativo("select contribuyenteid, count(*) from comprobanteselectronicos\r\n"
				+ "where contribuyenteid = "+id+"\r\n"
				+ "group by contribuyenteid;");
		
		//System.out.println("tamaño de comprobantes ----> "+comprobantes.size());
		
		if (comprobantes != null && comprobantes.size() > 0) {
			
			
			this.mensajeError("El contribuyente ya posee comprobantes electronicos, no se puede borrar.");
			return;
			
		}

		Contribuyente c = this.reg.getObjectById(Contribuyente.class.getName(), id);
		
		

		EventListener event = new EventListener() {

			@Override
			public void onEvent(Event evt) throws Exception {

				if (evt.getName().equals(Messagebox.ON_YES)) {

					borrar(c);

				}

			}

		};

		this.mensajeEliminar("El Contribuyente sera borrado permanentemente. \n Continuar?", event);

	}
	
	
	private void borrar(Contribuyente c) {

		this.reg.sqlNativoVoid("Delete from contribuyentesactividades where contribuyenteid = "+c.getContribuyenteid()+";");
		this.reg.sqlNativoVoid("Delete from contribuyentescontactos where contribuyenteid = "+c.getContribuyenteid()+";");
		this.reg.sqlNativoVoid("Delete from ContribuyentesUsuarios where contribuyenteid = "+c.getContribuyenteid()+";");
		this.reg.sqlNativoVoid("Delete from contribuyentesetiquetas where contribuyenteid = "+c.getContribuyenteid()+";");
		
		
		this.reg.deleteObject(c);

		this.cargarContribuyentes();
		BindUtils.postNotifyChange(null, null, this, "lContribuyentes");
		
		Notification.show("Contribuyente Borrado.");
	}
	
	
	//upload pdf
	
	//private Media pdfFile;
	
	@Command
	public void uploadFilePDF(@BindingParam("file") Media file) throws IOException {

	//	System.out.println("formato:" + file.getName());
		if (!file.getName().contains(".pdf")) {

			this.mensajeInfo("Archivo no valido.");
			//file = null;

		} 
		
		//pdfFile = file;     
		
		//InputStream is = file.getStreamData();
		
		//ByteArrayOutputStream out = new ByteArrayOutputStream();
        //byte[] buf = new byte[8192];
        //int r;
        //while ((r = is.read(buf)) != -1) out.write(buf, 0, r);
        
		
		PDDocument document = Loader.loadPDF(file.getByteData());
		
		PDFTextStripper stripper = new PDFTextStripper();
		
		stripper.setSortByPosition(true);
		
	    String pdf = stripper.getText(document);
	
	    System.out.println(pdf);
	        
	    procesarPdf(pdf);

		BindUtils.postNotifyChange(null, null, this, "*");

	}
	
	
	private void procesarPdf(String text) {
		
		 // RUC + DV
		text = text.replace("\u00A0", " ");  
		
		this.contribuyenteSelected.setTipoTransaccion(this.reg.getObjectByCondicion(TipoTransaccion.class.getName(), "codigo_sifen = 3"));
		this.buscarTipoTransaccion = this.contribuyenteSelected.getTipoTransaccion().getDescripcion();
		this.contribuyenteSelected.setTipoImpuesto(this.reg.getObjectByCondicion(TipoImpuesto.class.getName(), "codigo_sifen = 1"));
		this.buscarTipoImpuesto = this.contribuyenteSelected.getTipoImpuesto().getDescripcion();
		
		if (text.contains("PERSONA FÍSICA")) {
			
			this.mensajeInfo("No soportado Persona Fisica");
			
			return;
			/*
			this.contribuyenteSelected.setTipoContribuyente(this.reg.getObjectByCondicion(TipoContribuyente.class.getName(),"codigo_sifen = 1"));
			this.buscarTipoContribuyente = this.contribuyenteSelected.getTipoContribuyente().getTipoContribuyente();
			
			Pattern pRuc = Pattern.compile(
				    "RUC Actual\\s+DV\\s+RUC Anterior\\s*\\n\\s*(\\d+)\\s+(\\d+)"
				);
				Matcher m = pRuc.matcher(text);
				if (m.find()) {
				    this.contribuyenteSelected.setRuc(m.group(1));
				    this.contribuyenteSelected.setDv(m.group(2));
				}
				
				Pattern pRazon = Pattern.compile(
						"Primer Apellido.*?\\n\\s*([A-ZÁÉÍÓÚÑ ]+)\\s+([A-ZÁÉÍÓÚÑ ]+)\\s+\\*+\\s+([A-ZÁÉÍÓÚÑ ]+)",
						    Pattern.DOTALL
						);
					 m = pRazon.matcher(text);
					if (m.find()) {
						
						  String primerApellido = m.group(1);
						  String segundoApellido = m.group(2);
						  String nombres = m.group(3);
						
						  this.contribuyenteSelected.setNombre(primerApellido+" "+segundoApellido+", "+nombres);
						
					
					}
					
				Pattern pMail = Pattern.compile(
						    "Correo Electrónico\\s+([\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,})"
						);
						m = pMail.matcher(text);
						if (m.find()) {
							this.contribuyenteSelected.setEmail(m.group(1));
						}
						
						//Dirección Teléfono Teléfono Teléfono
						this.contribuyenteSelected.setNumCasa("0");
						
						Pattern pActiPri = Pattern.compile(
								 "6-ACTIVIDAD ECONÓMICA PRINCIPAL.*?\\nCódigo\\s+Descripción\\s*\\n(\\d{5})\\s+(.+)",
								 Pattern.DOTALL | Pattern.CASE_INSENSITIVE
									);
						
						m = pActiPri.matcher(text);
						if(m.find()) {
							
							String bloque = m.group(1).trim();
							//System.out.println("Bloque pActiPri: "+bloque);
				
							    String[] partes = new String[3];
							    partes[0] = m.group(1); // código
							    partes[1] = m.group(2); // descripción

							 //   System.out.println("Partes ActiPri:" +Arrays.toString(partes));
							    
							    for(Object[] x : this.lActividadesEconomicasBuscar) {
							    	
							    	if (x[1].toString().compareTo(partes[0].toString().trim()) == 0){

							    		this.contribuyenteSelected.getActividades().add(this.reg.getObjectById(ActividadEconomica.class.getName(), Long.parseLong(x[0].toString())));
							    		break;
							    	}
							    	
							    }
							}*/
						
			
			
		}else if (text.contains("PERSONA JURÍDICA")) {
			
			System.out.println("es Juridico");
			
			this.contribuyenteSelected.setTipoContribuyente(this.reg.getObjectByCondicion(TipoContribuyente.class.getName(),"codigo_sifen=2"));
			this.buscarTipoContribuyente = this.contribuyenteSelected.getTipoContribuyente().getTipoContribuyente();
			
			Pattern pRuc = Pattern.compile(
				    "Número\\s+DV\\s+(\\d+)\\s+(\\d+)"
				);
				Matcher m = pRuc.matcher(text);
				if (m.find()) {
				    this.contribuyenteSelected.setRuc(m.group(1));
				    this.contribuyenteSelected.setDv(m.group(2));
				}
				
			Pattern pRazon = Pattern.compile(
					"Razón o Denominación Social Nombre Fantasía\\s*\\R(.+?)\\s*Correo",
					    Pattern.DOTALL
					);
				 m = pRazon.matcher(text);
				if (m.find()) {
					
					String bloque = m.group(1).trim();
					String[] partes = bloque.split("\\s{2,}");
					
					this.contribuyenteSelected.setNombre(partes[0]);
					if (!partes[1].contains("*")) {
						this.contribuyenteSelected.setNombreFantacia(partes[1]);
					}
				
				}
				
			Pattern pMail = Pattern.compile(
					    "Correo Electrónico\\s+([\\w._%+-]+@[\\w.-]+\\.[A-Za-z]{2,})"
					);
					m = pMail.matcher(text);
					if (m.find()) {
						this.contribuyenteSelected.setEmail(m.group(1));
					}
					
					//Dirección Teléfono Teléfono Teléfono
					
			Pattern pDir = Pattern.compile(
					"(?:Dirección Teléfono línea baja Otro telef\\. línea baja Teléfono Celular Otro teléfono celular"
							  + "|Dirección Teléfono Teléfono Teléfono)"
							  + "\\s*\\R(.+?)\\s+Cuenta Corriente",
						    Pattern.DOTALL
						);
				m = pDir.matcher(text);
						if (m.find()) {
							String bloque = m.group(1).trim();
							
							//System.out.println("Bloque: "+bloque);
							
							 String[] partes = bloque.split("\\s{3,}");
							
							this.contribuyenteSelected.setDireccion(partes[0].trim());
							for (int i = 1 ; i<=4 ; i++) {
								
								if (!partes[i].contains("*")) {
									this.contribuyenteSelected.setTelefono(partes[i]);
									break;
								}
								
							}							
						    
						}
			this.contribuyenteSelected.setNumCasa("0");
			
			Pattern pLocali = Pattern.compile(
					 "Departamento Distrito/Ciudad Localidad/Compañía Barrio\\s*(.*?)\\s*Dirección",
					 Pattern.DOTALL | Pattern.CASE_INSENSITIVE
						);
			m = pLocali.matcher(text);
			if(m.find()) {
				
				String bloque = m.group(1).trim();
				//System.out.println("Bloque locali: "+bloque);
				String[] partes = bloque.split("\\s{3,}");

				for (Object[] x : this.getlDistritosBuscar()) {
					
					/*l.localidadid,
					l.codigo_sifen,
					l.localidad,
					di.distrito,
					de.departamento*/
					
					//DEP DISTR LOCA
					
					if (x[2].toString().contains(partes[2].toString().trim())
							&& x[3].toString().contains(partes[1].toString().trim())
							&& x[4].toString().contains(partes[0].toString().trim())) {
						
					
						 Localidad l = this.reg.getObjectById(Localidad.class.getName(),  Long.parseLong(x[1].toString()));
						 
						 this.contribuyenteSelected.setLocalidad(l);
						 this.buscarDistrito = this.contribuyenteSelected.getLocalidad().getLocalidad();
						 
						 break;
					}
					
					
				} 

			}
			
			Pattern pActiPri = Pattern.compile(
					 "6-ACTIVIDAD ECONÓMICA PRINCIPAL.*?\\n\\s*(\\d{5}.*?\\d{2}/\\d{2}/\\d{4})\\s*7-",
					 Pattern.DOTALL | Pattern.CASE_INSENSITIVE
						);
			
			m = pActiPri.matcher(text);
			if(m.find()) {
				
				String bloque = m.group(1).trim();
				//System.out.println("Bloque pActiPri: "+bloque);
				
				Pattern p = Pattern.compile("(\\d{5})\\s+(.+?)\\s+(\\d{2}/\\d{2}/\\d{4})");
				Matcher match = p.matcher(bloque);
				if (match.find()) {
				    String[] partes = new String[3];
				    partes[0] = match.group(1); // código
				    partes[1] = match.group(2); // descripción
				    partes[2] = match.group(3); // fecha

				 //   System.out.println("Partes ActiPri:" +Arrays.toString(partes));
				    
				    for(Object[] x : this.lActividadesEconomicasBuscar) {
				    	
				    	if (x[1].toString().compareTo(partes[0].toString().trim()) == 0){

				    		this.contribuyenteSelected.getActividades().add(this.reg.getObjectById(ActividadEconomica.class.getName(), Long.parseLong(x[0].toString())));
				    		break;
				    	}
				    	
				    }
				}
			}
			
			Pattern pActiSec = Pattern.compile(
					 "7-ACTIVIDADES ECONÓMICAS SECUNDARIAS.*?\\n(?:Código.*?\\n)?([\\s\\S]*?)8-\\s*SUSPENSIÓN TEMPORAL REGISTRO",
					 Pattern.DOTALL | Pattern.CASE_INSENSITIVE
						);
			
			m = pActiSec.matcher(text);
			if(m.find()) {
				
				String bloque = m.group(1).trim();
				//System.out.println("Bloque pActiSec: "+bloque);
				
				String [] lineas = bloque.split("\n");
				
				System.out.println("Lineas: \n"+Arrays.toString(lineas));
				
				for (String s: lineas) {
					
					Pattern p = Pattern.compile("(\\d{5})\\s+(.+?)\\s+(\\d{2}/\\d{2}/\\d{4})");
					Matcher match = p.matcher(s);
					if (match.find()) {
					    String[] partes = new String[3];
					    partes[0] = match.group(1); // código
					    partes[1] = match.group(2); // descripción
					    partes[2] = match.group(3); // fecha
					    for(Object[] x : this.lActividadesEconomicasBuscar) {
					    	
					    	if (x[1].toString().compareTo(partes[0].toString().trim()) == 0){
		
					    		this.contribuyenteSelected.getActividades().add(this.reg.getObjectById(ActividadEconomica.class.getName(), Long.parseLong(x[0].toString())));
					    		break;
					    	}
					    	
					    }
					}
					
				}
			}
			
			
			
		}
		
		
		
		

	}
	
	private Media logoFile;

	@Command
	@NotifyChange("*")
	public void uploadLogo(@BindingParam("file") Media file) {
		
		 if (file == null) {
		        this.mensajeInfo("No se ha seleccionado ningún archivo.");
		        return;
		    }

		 String fileName = file.getName().toLowerCase();
		    if (!fileName.endsWith(".png") && !fileName.endsWith(".jpg") ) {
		        this.mensajeInfo("Archivo no válido, debe ser .png o .jpg");
		        return;
		    }
		    
		    this.contribuyenteSelected.setLogo(file.getByteData());
		    this.logoFile = file;

		    this.mensajeInfo("Archivo subido correctamente.");

	}
	
	public Media getLogoFile() {
		return logoFile;
	}

	public void setLogoFile(Media logoFile) {
		this.logoFile = logoFile;
	}

	
	
	public Contribuyente getContribuyenteSelected() {
		return contribuyenteSelected;
	}

	public void setContribuyenteSelected(Contribuyente contribuyenteSelected) {
		this.contribuyenteSelected = contribuyenteSelected;
	}

	public boolean isOpCrearContribuyente() {
		return opCrearContribuyente;
	}

	public void setOpCrearContribuyente(boolean opCrearContribuyente) {
		this.opCrearContribuyente = opCrearContribuyente;
	}

	public boolean isOpEditarContribuyente() {
		return opEditarContribuyente;
	}

	public void setOpEditarContribuyente(boolean opEditarContribuyente) {
		this.opEditarContribuyente = opEditarContribuyente;
	}

	public boolean isOpBorrarContribuyente() {
		return opBorrarContribuyente;
	}

	public void setOpBorrarContribuyente(boolean opBorrarContribuyente) {
		this.opBorrarContribuyente = opBorrarContribuyente;
	}

	public String[] getFiltroColumns() {
		return filtroColumns;
	}

	public void setFiltroColumns(String[] filtroColumns) {
		this.filtroColumns = filtroColumns;
	}

	public boolean isEditar() {
		return editar;
	}

	public void setEditar(boolean editar) {
		this.editar = editar;
	}

	public List<Object[]> getlContribuyentes() {
		return lContribuyentes;
	}

	public void setlContribuyentes(List<Object[]> lContribuyentes) {
		this.lContribuyentes = lContribuyentes;
	}

	public boolean isOpAgregarContribuyenteUsuario() {
		return opAgregarContribuyenteUsuario;
	}

	public void setOpAgregarContribuyenteUsuario(boolean opAgregarContribuyenteUsuario) {
		this.opAgregarContribuyenteUsuario = opAgregarContribuyenteUsuario;
	}

	public boolean isOpInactivarContribuyenteUsuario() {
		return opInactivarContribuyenteUsuario;
	}

	public void setOpInactivarContribuyenteUsuario(boolean opInactivarContribuyenteUsuario) {
		this.opInactivarContribuyenteUsuario = opInactivarContribuyenteUsuario;
	}

	public Usuario getUsuarioSelected() {
		return usuarioSelected;
	}

	public void setUsuarioSelected(Usuario usuarioSelected) {
		this.usuarioSelected = usuarioSelected;
	}

	public List<ContribuyenteUsuario> getlContribuyentesUsuarios() {
		return lContribuyentesUsuarios;
	}

	public void setlContribuyentesUsuarios(List<ContribuyenteUsuario> lContribuyentesUsuarios) {
		this.lContribuyentesUsuarios = lContribuyentesUsuarios;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getBuscarTipoContribuyente() {
		return buscarTipoContribuyente;
	}

	public void setBuscarTipoContribuyente(String buscarTipoContribuyente) {
		this.buscarTipoContribuyente = buscarTipoContribuyente;
	}

	public List<Object[]> getlTiposContribuyentesBuscar() {
		return lTiposContribuyentesBuscar;
	}

	public void setlTiposContribuyentesBuscar(List<Object[]> lTiposContribuyentesBuscar) {
		this.lTiposContribuyentesBuscar = lTiposContribuyentesBuscar;
	}

	public List<Object[]> getlTiposTransaccionesBuscar() {
		return lTiposTransaccionesBuscar;
	}

	public void setlTiposTransaccionesBuscar(List<Object[]> lTiposTransaccionesBuscar) {
		this.lTiposTransaccionesBuscar = lTiposTransaccionesBuscar;
	}

	public String getBuscarTipoTransaccion() {
		return buscarTipoTransaccion;
	}

	public void setBuscarTipoTransaccion(String buscarTipoTransaccion) {
		this.buscarTipoTransaccion = buscarTipoTransaccion;
	}

	public List<Object[]> getlTiposImpuestosBuscar() {
		return lTiposImpuestosBuscar;
	}

	public void setlTiposImpuestosBuscar(List<Object[]> lTiposImpuestosBuscar) {
		this.lTiposImpuestosBuscar = lTiposImpuestosBuscar;
	}

	public String getBuscarTipoImpuesto() {
		return buscarTipoImpuesto;
	}

	public void setBuscarTipoImpuesto(String buscarTipoImpuesto) {
		this.buscarTipoImpuesto = buscarTipoImpuesto;
	}

	public List<Object[]> getlActividadesEconomicasBuscar() {
		return lActividadesEconomicasBuscar;
	}

	public void setlActividadesEconomicasBuscar(List<Object[]> lActividadesEconomicasBuscar) {
		this.lActividadesEconomicasBuscar = lActividadesEconomicasBuscar;
	}

	public String getBuscarActividadEconomica() {
		return buscarActividadEconomica;
	}

	public void setBuscarActividadEconomica(String buscarActividadEconomica) {
		this.buscarActividadEconomica = buscarActividadEconomica;
	}

	public List<Object[]> getlDistritosBuscar() {
		return lDistritosBuscar;
	}

	public void setlDistritosBuscar(List<Object[]> lDistritosBuscar) {
		this.lDistritosBuscar = lDistritosBuscar;
	}

	public String getBuscarDistrito() {
		return buscarDistrito;
	}

	public void setBuscarDistrito(String buscarDistrito) {
		this.buscarDistrito = buscarDistrito;
	}

	public ListModelList<Tipo> getEtiquetas() {
		return etiquetas;
	}

	public void setEtiquetas(ListModelList<Tipo> etiquetas) {
		this.etiquetas = etiquetas;
	}

	
	
	
	

}
