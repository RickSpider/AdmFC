Select 
c.contribuyenteid,  
c.ruc||'-'||c.dv,  
c.nombre, 
c.ambiente, 
case when c.habilitado = true then 'Activo' else 'Inactivo' end as estadoHabilitado,
COALESCE(STRING_AGG(t.tipo, ', ' ORDER BY ce.etiquetaid), '') AS etiquetas,
TO_CHAR(c.vencimientokey, 'DD/MM/YYYY') AS vencimientokey
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