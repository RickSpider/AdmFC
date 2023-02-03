package com.admFC.util;


import java.util.ArrayList;
import java.util.List;

import org.zkoss.bind.annotation.AfterCompose;
import org.zkoss.bind.annotation.Init;

import com.admFC.modelo.Contribuyente;
import com.admFC.modelo.ContribuyenteUsuario;
import com.doxacore.TemplateViewModel;

public abstract class TemplateViewModelLocal extends TemplateViewModel{
	
	@Init(superclass = true)
	public void initTemplateViewModelLocal() {
		
	}

	@AfterCompose(superclass = true)
	public void afterComposeTemplateViewModelLocal() {
		
	}
	
	protected List<Contribuyente> getContribuyentesPorUsuario() {
		
		List<Contribuyente> lc = new ArrayList();
		
		List<ContribuyenteUsuario> lcu;
		
		if (this.isUserRolMaster()) {
			
			lc = this.reg.getAllObjectsByCondicionOrder(Contribuyente.class.getName(), null, "contribuyenteid asc");
			
			return lc;
			
		}else {
			
			lcu = this.reg.getAllObjectsByCondicionOrder(ContribuyenteUsuario.class.getName(), "usuarioid = "+this.getCurrentUser().getUsuarioid(), "contribuyenteid asc");
			
		}
		
		
		
		for (ContribuyenteUsuario x : lcu) {
			
			lc.add(x.getContribuyente());
			
		}
		
		return lc;
		
	}
	
	
	
}
