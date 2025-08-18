Select 
c.contribuyenteid,  
c.nombre, 
c.ruc||'-'||c.dv,  
c.ambiente, 
c.habilitado,
c.vencimientokey
from contribuyentes c
--left join contribuyentesusuarios cu on cu.contribuyenteid = c.contribuyenteid 
--where cu.usuarioid = ?1
order by contribuyenteid asc;