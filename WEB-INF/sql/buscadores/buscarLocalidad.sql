SELECT

l.localidadid,
l.codigo_sifen,
l.localidad,
di.distrito,
de.departamento

FROM localidades l
LEFT JOIN distritos di on di.distritoid = l.distritoid
LEFT JOIN departamentos de on de.departamentoid = di.departamentoid
order by l.localidadid;