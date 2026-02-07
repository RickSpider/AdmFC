Select 

e.eventoid,
--c.nombre,
TO_CHAR(e.creado, 'yyyy-mm-dd HH24:MI:SS') as creado,
TO_CHAR(e.fecha, 'yyyy-mm-dd HH24:MI:SS') as fecha,
COALESCE(e.cdc,(e.timbrado||'_'||e.establecimiento||'-'||e.punto_expedicion||'-('||e.numero_ini||', '||e.numero_fin||')')),
--e.timbrado,
--(e.establecimiento||'-'||e.punto_expedicion||'-('||e.numero_ini||', '||e.numero_fin||')') as intevalo,
e.motivo,
e.enviado,
e.estado,
e.mensaje,
tce.tipocomprobanteelectronico,
e.ambiente

from Eventos e
left join contribuyentes c on c.contribuyenteid = e.contribuyenteid
left join tiposcomprobanteselectronicos tce on tce.tipocomprobanteelectronicoid = e.tipocomprobanteelectronicoid 
WHERE c.contribuyenteid = ?3
AND e.fecha BETWEEN '?1' AND '?2' 
order by eventoid desc;