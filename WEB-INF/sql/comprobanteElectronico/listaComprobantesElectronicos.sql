Select 
ce.id, 
TO_CHAR(ce.creado, 'yyyy-mm-dd HH24:MI:SS'), 
c.nombre,
ce.cdc, 
ce.numero,
ce.envioporlote, 
ce.enviadolote, 
ce.enviado, 
l.nro,
ce.estado ,
tce.tipocomprobanteelectronico,
ce.enlaceqr,
ce.ambiente,
ce.respuesta
from comprobanteselectronicos ce
left join tiposcomprobanteselectronicos tce on tce.tipocomprobanteelectronicoid = ce.tipocomprobanteelectronicoid 
left join lotes l on l.loteid = ce.loteid
left join contribuyentes c on c.contribuyenteid = ce.contribuyenteid
WHERE ce.creado BETWEEN '?1' AND '?2'
AND c.contribuyenteid = ?3
order by id desc;