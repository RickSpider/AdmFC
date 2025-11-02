Select 
c.contribuyenteid,  
c.nombre, 
c.ruc||'-'||c.dv,  
c.ambiente, 
c.habilitado,
COALESCE(STRING_AGG(t.tipo, ', ' ORDER BY ce.etiquetaid), '') AS etiquetas,
c.vencimientokey
from contribuyentes c
left join contribuyentesetiquetas ce on ce.contribuyenteid = c.contribuyenteid
left join tipos t on t.tipoid = ce.etiquetaid
--left join contribuyentesusuarios cu on cu.contribuyenteid = c.contribuyenteid 
--where cu.usuarioid = ?1
GROUP BY 
    c.contribuyenteid, 
    c.nombre, 
    c.ruc, 
    c.dv, 
    c.ambiente, 
    c.habilitado, 
    c.vencimientokey
order by contribuyenteid asc;