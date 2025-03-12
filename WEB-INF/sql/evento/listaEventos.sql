Select 

e.eventoid,
c.nombre,
TO_CHAR(e.creado, 'yyyy-mm-dd HH24:MI:SS') as creado,
TO_CHAR(e.fecha, 'yyyy-mm-dd HH24:MI:SS') as fecha,
e.cdc,
e.timbrado,
(e.establecimiento||'-'||e.punto_expedicion||'-('||e.numero_ini||', '||e.numero_fin||')') as intevalo,
e.motivo,
e.enviado,
e.estado,
e.mensaje,
e.ambiente

from Eventos e
left join contribuyentes c on c.contribuyenteid = e.contribuyenteid
WHERE e.fecha BETWEEN '?1' AND '?2'
AND c.contribuyenteid = ?3
order by eventoid desc;