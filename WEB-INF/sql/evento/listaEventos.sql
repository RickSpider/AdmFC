Select 

e.eventoid,
c.nombre,
e.fecha,
e.cdc,
e.motivo,
e.enviado,
e.estado,
e.mensaje

from Eventos e
left join contribuyentes c on c.contribuyenteid = e.contribuyenteid
WHERE e.fecha BETWEEN '?1' AND '?2'
AND c.contribuyenteid = ?3
order by eventoid desc;