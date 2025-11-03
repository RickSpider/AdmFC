package com.admFC.sistema.main;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.zkoss.bind.BindUtils;
import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.BindingParam;
import org.zkoss.bind.annotation.Command;
import org.zkoss.bind.annotation.Init;
import org.zkoss.bind.annotation.NotifyChange;
import org.zkoss.zk.ui.util.Notification;
import org.zkoss.zul.ListModelList;

import com.admFC.modelo.Contribuyente;
import com.admFC.util.TemplateViewModelLocal;
import com.doxacore.components.Statbox;
import com.doxacore.components.finder.FinderInterface;
import com.doxacore.components.finder.FinderModel;
import com.doxacore.report.ReportBigExcel;

public class DashboardVM extends TemplateViewModelLocal implements FinderInterface {
	
	private ListModelList<Statbox> lStatsComprobantes = new ListModelList<Statbox>();	
	private ListModelList<Statbox> lStatsEventos = new ListModelList<Statbox>();
	private List<Object[]> listaDetallada;
	private List<Object[]> listaDetalladaOri;
	private Contribuyente contribuyenteSelected;
	private String titulo="DashboardVM";
	private Date desde = new Date();
	private Date hasta;
	private List<Object[]> lContribuyentes;
	
	private FinderModel contribuyenteFinder;

	@Init(superclass = true)
	public void initDashboardVM() {
		
		
		desde = this.um.modificarHorasMinutosSegundos(new Date(), 0,0,0,0);
		hasta = this.um.modificarHorasMinutosSegundos(this.desde, 23, 59, 59, 999);
		
		
		this.inicializarFiltros();
		cargarDatos();
		
		inicializarFinders();
		
	}

	@AfterCompose(superclass = true)
	public void afterComposeDashboardVM() {
		
	}

	
	@Override
	protected void inicializarOperaciones() {
		// TODO Auto-generated method stub
		
	}
	
	private String filtroColumns[];

	private void inicializarFiltros() {

		this.filtroColumns = new String[9];

		for (int i = 0; i < this.filtroColumns.length; i++) {

			this.filtroColumns[i] = "";

		}

	}
	
	@Command
	@NotifyChange("listaDetallada")
	public void filtrarContribuyente() {

		this.listaDetallada = this.filtrarListaObject(this.filtroColumns, this.listaDetalladaOri);

	}
	
	
	
	
	private void cargarDatos() {
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		this.lStatsComprobantes.clear();
		this.lStatsEventos.clear();	 
		
		String sqlComprobante = "SELECT \n" + 
				"-- ##CONTRIBUYENTEID## \n"+
				"    COALESCE(SUM(CASE WHEN ce.estado like '%Aprobado%' THEN 1 ELSE 0 END),0) AS aprobados,\n" + 
				"    COALESCE(SUM(CASE WHEN ce.estado like '%Rechazado%' THEN 1 ELSE 0 END),0) AS rechazados,\n" + 
				"    COALESCE(SUM(CASE WHEN ce.estado isnull OR ce.estado = '' OR ce.estado like '%Pendiente%' THEN 1 ELSE 0 END),0) AS nulos\n" + 
				"	--##CONTRIBUYENTEN## \n"+
				"	--##ETIQUETAS## \n"+
				"FROM comprobanteselectronicos ce \n" +
				"join contribuyentes c on c.contribuyenteid = ce.contribuyenteid \n"+
				"--##NOMASTER## \n"+
				"WHERE ce.creado BETWEEN '"+sdf.format(desde)+"' AND '"+sdf.format(hasta)+"' \n"+
				"and c.habilitado = true \n"+
				"--##USUARIO## \n"+
				"--##CONTRIBUYENTE## \n"+
				"--##GROUP## \n" +	
				"--##ORDER## \n"+	
				";";
		
		String sqlEvento = "SELECT \n" + 
				"-- ##CONTRIBUYENTEID## \n"+
				"    COALESCE(SUM(CASE WHEN e.estado like '%Aprobado%' THEN 1 ELSE 0 END),0) AS aprobados,\n" + 
				"    COALESCE(SUM(CASE WHEN e.estado like '%Rechazado%' THEN 1 ELSE 0 END),0) AS rechazados,\n" + 
				"    COALESCE(SUM(CASE WHEN e.estado isnull OR estado = '' OR e.estado like '%Pendiente%' THEN 1 ELSE 0 END),0) AS nulos\n" + 
				"	--##CONTRIBUYENTEN## \n"+	
				"FROM eventos e\n"+
				"join contribuyentes c on c.contribuyenteid = e.contribuyenteid \n"+
				"--##NOMASTER## \n"+
				"WHERE fecha BETWEEN '"+sdf.format(desde)+"' AND '"+sdf.format(hasta)+"' \n"+
				"and c.habilitado = true \n"+
				"--##USUARIO## \n"+
				"--##CONTRIBUYENTE## \n"+
				"--##GROUP## \n" +	
				"--##ORDER## \n"+	
				";";
		
		//Usuario user = this.getCurrentUser();
		
		if (!this.isUserRolMaster() && !this.isUserRolAdmin()) {
			
			sqlComprobante = sqlComprobante.replace("--##NOMASTER##", " JOIN contribuyentesusuarios cu on cu.contribuyenteid = ce.contribuyenteid ")
					.replace("--##USUARIO##", "AND cu.usuarioid = "+this.getCurrentUser().getUsuarioid() +" \n" );
			
			sqlEvento = sqlEvento.replace("--##NOMASTER##", " JOIN contribuyentesusuarios cu on cu.contribuyenteid = e.contribuyenteid ")
					.replace("--##USUARIO##", "AND cu.usuarioid = "+this.getCurrentUser().getUsuarioid() +" \n" );
			
		}
		
		if (this.contribuyenteSelected != null) {
			
			sqlComprobante = sqlComprobante.replace("--##CONTRIBUYENTE##", "AND ce.contribuyenteid = "+this.contribuyenteSelected.getContribuyenteid()+" ");
			sqlEvento = sqlEvento.replace("--##CONTRIBUYENTE##", "AND e.contribuyenteid = "+this.contribuyenteSelected.getContribuyenteid()+" ");
		}
		
		//System.out.println(sqlComprobante);
		
		List<Object[]> resultComprobante = this.reg.sqlNativo(sqlComprobante);
		
		int totalComprobantes = Integer.parseInt(resultComprobante.get(0)[0].toString()) + Integer.parseInt(resultComprobante.get(0)[1].toString()) +Integer.parseInt(resultComprobante.get(0)[2].toString());
		
		lStatsComprobantes.add(new Statbox("0","TOTAL",String.valueOf(totalComprobantes),"fa-file-text-o",Statbox.styleBlackDarker));
		lStatsComprobantes.add(new Statbox("1","APROBADOS",resultComprobante.get(0)[0].toString(),"fa-check",Statbox.styleGreenDarker,true,
				toPorcentaje(Double.parseDouble(resultComprobante.get(0)[0].toString()),totalComprobantes)));
		lStatsComprobantes.add(new Statbox("2","RECHAZADOS",resultComprobante.get(0)[1].toString(),"fa-close",Statbox.styleRedDarker,true,
				toPorcentaje(Double.parseDouble(resultComprobante.get(0)[1].toString()),totalComprobantes)));
		lStatsComprobantes.add(new Statbox("3","PENDIENTE/SIN ESTADO",resultComprobante.get(0)[2].toString(),"fa-file-o",Statbox.stylePurpleDarker,true,
				toPorcentaje(Double.parseDouble(resultComprobante.get(0)[2].toString()),totalComprobantes)));
		
	//	System.out.println(sqlEvento);
		
		List<Object[]> resultEvento = this.reg.sqlNativo(sqlEvento);
		
		int totalEventos = Integer.parseInt(resultEvento.get(0)[0].toString()) + Integer.parseInt(resultEvento.get(0)[1].toString()) +Integer.parseInt(resultEvento.get(0)[2].toString());
		
		lStatsEventos.add(new Statbox("0","TOTAL",String.valueOf(totalEventos),"fa-file-text-o",Statbox.styleOrangeDarker));
		lStatsEventos.add(new Statbox("1","APROBADOS",resultEvento.get(0)[0].toString(),"fa-check",Statbox.styleGreenDarker,true,
				toPorcentaje(Double.parseDouble(resultEvento.get(0)[0].toString()),totalEventos)));
		lStatsEventos.add(new Statbox("2","RECHAZADOS",resultEvento.get(0)[1].toString(),"fa-close",Statbox.styleRedDarker,true,
				toPorcentaje(Double.parseDouble(resultEvento.get(0)[1].toString()),totalEventos)));
		lStatsEventos.add(new Statbox("3","PENDIENTE/SIN ESTADO",resultEvento.get(0)[2].toString(),"fa-file-o",Statbox.stylePurpleDarker,true,
				toPorcentaje(Double.parseDouble(resultEvento.get(0)[2].toString()),totalEventos)));
		
		
		sqlComprobante = sqlComprobante.replace("-- ##CONTRIBUYENTEID##", "c.contribuyenteid,")
				.replace("--##GROUP##", "group by c.contribuyenteid")
				.replace("--##ORDER##", "order by c.contribuyenteid asc")
				.replace("--##CONTRIBUYENTEN##", ", c.nombre, c.ambiente")
				.replace("--##ETIQUETAS##", " , (SELECT STRING_AGG(DISTINCT t.tipo, ', ')\n"
						+ "     FROM contribuyentesetiquetas ceti\n"
						+ "     JOIN tipos t ON t.tipoid = ceti.etiquetaid\n"
						+ "     WHERE ceti.contribuyenteid = c.contribuyenteid\n"
						+ "    ) AS etiquetas \n");
				
			
		
		sqlEvento = sqlEvento.replace("-- ##CONTRIBUYENTEID##", "c.contribuyenteid,")
				.replace("--##GROUP##", "group by c.contribuyenteid")
				.replace("--##ORDER##", "order by c.contribuyenteid asc")
				.replace("--##CONTRIBUYENTEN##", ", c.nombre");
		
		System.out.println(sqlComprobante);
		
		resultComprobante = this.reg.sqlNativo(sqlComprobante);
		resultEvento = this.reg.sqlNativo(sqlEvento);
		
		//System.out.println(resultComprobante);
		
		this.listaDetallada = new ArrayList<Object[]>();
		
		for (Object[] x : resultComprobante) {
			
			Object[] o = new Object[9];
			
			
			o[0] = x[4];
			o[1] = ((Long) x[1] == 0) ? "" : x[1];
			o[2] = ((Long) x[2] == 0) ? "" : x[2];
			o[3] = ((Long) x[3] == 0) ? "" : x[3];
			o[4] = "";
			o[5] = "";
			o[6] = "";
			o[7] = x[5];
			o[8] = x[6];
			
			for (Object[] y : resultEvento) {
				
				if (y[0].toString().compareTo(x[0].toString()) ==0 ) {
					
					o[4] = ((Long) y[1] == 0) ? "" : y[1];
					o[5] = ((Long) y[2] == 0) ? "" : y[2];
					o[6] = ((Long) y[3] == 0) ? "" : y[3];
					
					break;
					
				}
				
			}
			
			
			
			this.listaDetallada.add(o);
			
		}
		
		this.listaDetalladaOri = this.listaDetallada;

	}
	
	private double toPorcentaje(double parcial, double total) {
		
		double resultado = (100*parcial)/total;
		
		//retorna redondeado a dos cifras
		return Math.round(resultado*100.0)/100.0;
		
	}
	
	@NotifyChange("*")
	public void onChangeFiltroFechas() {
		
		this.cargarDatos();
		
	}
	
	@Command
	@NotifyChange("*")
	public void refrescarDatos() {
		
		this.cargarDatos();
		Notification.show("Refrescando Datos.");
		
	}
	
	public void onClickXXX() {
		
		Notification.show("*********");
		
	}
	
	//==============Seccion Finder==============
	
	@NotifyChange("*")
	public void inicializarFinders() {
		
		String sqlContribuyente = "Select c.contribuyenteid as id, c.nombre as contribuyente, (c.ruc||'-'||c.dv) as ruc, ambiente as ambiente from contribuyentes c \n"
				+ "--left join contribuyentesusuarios cu on cu.contribuyenteid = c.contribuyenteid \n"  
				+ "where c.habilitado = true \n "
				+ "-- and cu.usuarioid = ?1 \n"
				+ "order by c.contribuyenteid asc;";
		
		//String sqlContribuyente  = this.um.getSql("contribuyente/listaContribuyentes.sql");

		if (!this.isUserRolMaster() && !this.isUserRolAdmin()) {

			sqlContribuyente = sqlContribuyente.replace("--", "").replace("?1", this.getCurrentUser().getUsuarioid() + "");

		}
		
		contribuyenteFinder = new FinderModel("Contribuyente", sqlContribuyente);
		
		this.lContribuyentes = this.reg.sqlNativo(sqlContribuyente);
		
		//System.out.println("El tamaño de lContribuyetes es: "+this.lContribuyentes.size());
	}
	

	public void generarFinders(@BindingParam("finder") String finder) {
		
		if (finder.compareTo(this.contribuyenteFinder.getNameFinder()) == 0) {
			
			this.contribuyenteFinder.generarListFinder();
			BindUtils.postNotifyChange(null, null, this.contribuyenteFinder, "listFinder");
			
		}
		
	}
	
	@Command
	public void finderFilter(@BindingParam("filter") String filter, @BindingParam("finder") String finder) {
		
		if (finder.compareTo(this.contribuyenteFinder.getNameFinder()) == 0) {
			
			this.contribuyenteFinder.setListFinder(this.filtrarListaObject(filter, this.contribuyenteFinder.getListFinderOri()));
			BindUtils.postNotifyChange(null, null, this.contribuyenteFinder, "listFinder");
			
		}
		
	}
	
	@Command
	@NotifyChange("*")
	public void onSelectetItemFinder(@BindingParam("id") Long id, @BindingParam("finder") String finder) {
		
		if (finder.compareTo(this.contribuyenteFinder.getNameFinder()) == 0) {
			
			this.contribuyenteSelected = this.reg.getObjectById(Contribuyente.class.getName(), id);
		//	BindUtils.postNotifyChange(null, null, this, "contribuyenteFinder");
		}		
		
		this.cargarDatos();
		
	}

	@Command
	public void exportarExcel() {
		
		/*if(this.contribuyenteSelected == null) {
			
			this.mensajeInfo("Debes seleccionar un contribuyente.");
			
			return;
			
		}*/
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ");
		List<String[]> titulos = new ArrayList<String[]>();
		
		DecimalFormatSymbols dfs = new DecimalFormatSymbols(new Locale("es", "ES"));
		dfs.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("#,##0.##",dfs);
		
		String[] t1 = {"Reporte Detallado de Cantidades"};
		
		String[] t3 = {"Fecha Desde:", sdf.format(this.desde)};
		String[] t4 = {"Fecha Hasta:", sdf.format(this.hasta)};
		String[] espacioBlanco = {""};

		titulos.add(t1);
		
		titulos.add(espacioBlanco);
		titulos.add(t3);
		titulos.add(t4);
		titulos.add(espacioBlanco);
		
		List<String[]> headersDatos = new ArrayList<String[]>();
		String [] hd1 =  {"AMBIENTE", "CONTRIBUYENTE", "ETIQUETAS","COMP. APROBADOS", "COMP. RECHAZADOS","COMP. PENDIENTES","EVENTOS APROBADOS", "EVENTOS RECHAZADOS", "EVENTOS PENDIENTES" };
		headersDatos.add(hd1);
		
		List<Object[]> detalles = new ArrayList<>();
		
		for (Object[] ox : this.listaDetallada) {
			
			Object[] o = new Object[9];
			
				
			o[0] = ox[7].toString();
			o[1] = ox[0].toString();
			o[2] = ox[8].toString();
			o[3] = df.format(Double.parseDouble((ox[1] == null || ox[1].toString().trim().isEmpty()) ? "0" : ox[1].toString()));
			o[4] = df.format(Double.parseDouble((ox[2] == null || ox[2].toString().trim().isEmpty()) ? "0" : ox[2].toString()));
			o[5] = df.format(Double.parseDouble((ox[3] == null || ox[3].toString().trim().isEmpty()) ? "0" : ox[3].toString()));
			o[6] = df.format(Double.parseDouble((ox[4] == null || ox[4].toString().trim().isEmpty()) ? "0" : ox[4].toString()));
			o[7] = df.format(Double.parseDouble((ox[5] == null || ox[5].toString().trim().isEmpty()) ? "0" : ox[5].toString()));
			o[8] = df.format(Double.parseDouble((ox[6] == null || ox[6].toString().trim().isEmpty()) ? "0" : ox[6].toString()));
			
			detalles.add(o);
		}
		
		ReportBigExcel re = new ReportBigExcel("TOTAL_CE");
		re.descargar(titulos, headersDatos, detalles);
	}
	
	public String getTitulo() {
		return titulo;
	}

	public void setTitulo(String titulo) {
		this.titulo = titulo;
	}

	public ListModelList<Statbox> getlStatsComprobantes() {
		return lStatsComprobantes;
	}

	public void setlStatsComprobantes(ListModelList<Statbox> lStatsComprobantes) {
		this.lStatsComprobantes = lStatsComprobantes;
	}

	public ListModelList<Statbox> getlStatsEventos() {
		return lStatsEventos;
	}

	public void setlStatsEventos(ListModelList<Statbox> lStatsEventos) {
		this.lStatsEventos = lStatsEventos;
	}

	public Date getDesde() {
		return desde;
	}

	public void setDesde(Date desde) {
		this.desde = desde;
	}

	public Date getHasta() {
		return hasta;
	}

	public void setHasta(Date hasta) {
		this.hasta = hasta;
	}

	public Contribuyente getContribuyenteSelected() {
		return contribuyenteSelected;
	}

	public void setContribuyenteSelected(Contribuyente contribuyenteSelected) {
		this.contribuyenteSelected = contribuyenteSelected;
	}

	public FinderModel getContribuyenteFinder() {
		return contribuyenteFinder;
	}

	public void setContribuyenteFinder(FinderModel contribuyenteFinder) {
		this.contribuyenteFinder = contribuyenteFinder;
	}

	public List<Object[]> getlContribuyentes() {
		return lContribuyentes;
	}

	public void setlContribuyentes(List<Object[]> lContribuyentes) {
		this.lContribuyentes = lContribuyentes;
	}

	public List<Object[]> getListaDetallada() {
		return listaDetallada;
	}

	public void setListaDetallada(List<Object[]> listaDetallada) {
		this.listaDetallada = listaDetallada;
	}

	public String[] getFiltroColumns() {
		return filtroColumns;
	}

	public void setFiltroColumns(String[] filtroColumns) {
		this.filtroColumns = filtroColumns;
	}
	
	

}
