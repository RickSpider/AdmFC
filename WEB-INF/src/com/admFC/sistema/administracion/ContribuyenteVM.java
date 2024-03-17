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
import java.util.Date;
import java.util.List;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.util.Notification;
import org.zkoss.zul.Window;

import com.admFC.modelo.ActividadEconomica;
import com.admFC.modelo.Contribuyente;
import com.admFC.modelo.ContribuyenteContacto;
import com.admFC.modelo.ContribuyenteUsuario;
import com.admFC.modelo.Distrito;
import com.admFC.modelo.TipoContribuyente;
import com.admFC.modelo.TipoImpuesto;
import com.admFC.modelo.TipoTransaccion;
import com.admFC.util.ParamsLocal;
import com.admFC.util.TemplateViewModelLocal;
import com.doxacore.modelo.Auditoria;
import com.doxacore.modelo.Rol;
import com.doxacore.modelo.Usuario;
import com.doxacore.modelo.UsuarioRol;
import com.doxacore.util.UtilStaticMetodos;
import com.google.gson.Gson;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

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

	@Init(superclass = true)
	public void initContribuyenteVM() {

		cargarContribuyentes();
		inicializarFiltros();

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

		if (!this.isUserRolMaster()) {

			sql = sql.replace("--", "").replace("?1", this.getCurrentUser().getUsuarioid() + "");

		}

		this.lContribuyentes = this.reg.sqlNativo(sql);
		this.lContribuyentesOri = this.lContribuyentes;

	}

	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[5];

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

		this.nombre = "";
		this.email = "";

		if (contribuyenteid != -1) {

			if (!this.opEditarContribuyente)
				return;

			this.editar = true;
			this.contribuyenteSelected = this.reg.getObjectById(Contribuyente.class.getName(), contribuyenteid);

			this.buscarTipoContribuyente = "";
			this.buscarTipoTransaccion = "";
			this.buscarTipoImpuesto = "";
			this.buscarDistrito = "";

			if (this.contribuyenteSelected.getDistrito() != null) {

				this.buscarDistrito = this.contribuyenteSelected.getDistrito().getDistrito();

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

		this.save(this.contribuyenteSelected);
		
		this.guardarCert();

		this.contribuyenteSelected = null;

		this.cargarContribuyentes();

		this.modal.detach();

		this.auditoria.setUsuario(this.getCurrentUser().getAccount());

		if (editar) {

			Notification.show("Contribuyente Actualizado.");
			this.auditoria.setSentencia("UPDATE");
			this.editar = false;

		} else {

			this.auditoria.setSentencia("INSERT");
			Notification.show("El Contribuyente fue agregado.");
		}

		this.reg.saveObject(this.auditoria, "SYSTEM");

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

	// distrito
	private List<Object[]> lDistritosBuscarOri = null;
	private List<Object[]> lDistritosBuscar = null;
	private String buscarDistrito = "";

	@Command
	@NotifyChange("lDistritosBuscar")
	public void generarListaDistritos() {

		String sql = this.um.getSql("buscadores/buscarDistrito.sql");

		this.lDistritosBuscar = this.reg.sqlNativo(sql);

		this.lDistritosBuscarOri = this.lDistritosBuscar;

	}

	@Command
	@NotifyChange("lDistritosBuscar")
	public void filtrarDistritoBuscar() {

		this.lDistritosBuscar = this.filtrarListaObject(buscarDistrito, this.lDistritosBuscarOri);

	}

	@Command
	@NotifyChange("buscarDistrito")
	public void onSelectDistrito(@BindingParam("id") long id) {

		this.contribuyenteSelected.setDistrito(this.reg.getObjectById(Distrito.class.getName(), id));
		this.buscarDistrito = this.contribuyenteSelected.getDistrito().getDistrito();

	}

	// tipoImpuesto
	private List<Object[]> lTiposImpuestosBuscarOri = null;
	private List<Object[]> lTiposImpuestosBuscar = null;
	private String buscarTipoImpuesto = "";

	@Command
	@NotifyChange("lTiposImpuestosBuscar")
	public void generarListaTiposImpuestos() {

		String sql = this.um.getSql("buscadores/buscarTipoImpuesto.sql");

		this.lTiposImpuestosBuscar = this.reg.sqlNativo(sql);

		this.lTiposImpuestosBuscarOri = this.lTiposImpuestosBuscar;

	}

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

	@Command
	@NotifyChange("lTiposTransaccionesBuscar")
	public void generarListaTiposTransacciones() {

		String sql = this.um.getSql("buscadores/buscarTipoTransaccion.sql");

		this.lTiposTransaccionesBuscar = this.reg.sqlNativo(sql);

		this.lTiposTransaccionesBuscarOri = this.lTiposTransaccionesBuscar;

	}

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

	@Command
	@NotifyChange("lTiposContribuyentesBuscar")
	public void generarListaTiposContribuyentes() {

		String sql = this.um.getSql("buscadores/buscarTipoContribuyente.sql");

		this.lTiposContribuyentesBuscar = this.reg.sqlNativo(sql);

		this.lTiposContribuyentesBuscarOri = this.lTiposContribuyentesBuscar;

	}

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

	@Command
	@NotifyChange("lActividadesEconomicasBuscar")
	public void generarListaActividadesEconomicas() {

		String sql = this.um.getSql("buscadores/buscarActividadEconomica.sql");

		this.lActividadesEconomicasBuscar = this.reg.sqlNativo(sql);

		this.lActividadesEconomicasBuscarOri = this.lActividadesEconomicasBuscar;

	}

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

		System.out.println("formato:" + file.getName());

		if (file.getName().contains(".pfx")) {

			System.out.println("es el archivo");

			Date vencimiento = this.getVencimientoKey(file.getByteData());

			if (vencimiento != null) {

				certFile = file;

				this.contribuyenteSelected.setVencimientokey(vencimiento);

				String directorio = this.getSistemaPropiedad("FE_CERTDIR").getValor();

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

}
