Select 
ce.id, 
ce.creado, 
c.nombre,
ce.cdc, 
ce.numero,
ce.estado ,
ce.enviado, 
ce.envioporlote, 
ce.enviadolote, 
l.nro,
tce.tipocomprobanteelectronico
from comprobanteselectronicos ce
left join tiposcomprobanteselectronicos tce on tce.tipocomprobanteelectronicoid = ce.tipocomprobanteelectronicoid 
left join lotes l on l.loteid = ce.loteid
left join contribuyentes c on c.contribuyenteid = ce.contribuyenteid
WHERE ce.creado BETWEEN '?1' AND '?2'
AND c.contribuyenteid = ?3
order by id desc;