Select 

l.loteid,
c.nombre,
TO_CHAR(l.fecha, 'yyyy-mm-dd HH24:MI:SS'),
l.nro,
l.estado

from lotes l
left join contribuyentes c on c.contribuyenteid = l.contribuyenteid
WHERE l.fecha BETWEEN '?1' AND '?2'
AND c.contribuyenteid = ?3
order by l.loteid desc;