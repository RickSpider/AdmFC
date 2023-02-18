Select 

l.loteid,
c.nombre,
l.fecha,
l.nro,
l.estado

from lotes l
left join contribuyentes c on c.contribuyenteid = l.contribuyenteid
WHERE l.fecha BETWEEN '?1' AND '?2'
AND c.contribuyenteid = ?3
order by l.loteid desc;