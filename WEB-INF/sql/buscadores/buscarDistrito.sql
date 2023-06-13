SELECT 

di.distritoid, 
di.codigo_sifen,
di.distrito,
de.departamentoid,
de.departamento 

FROM distritos di
LEFT JOIN departamentos de on de.departamentoid = di.departamentoid
order by di.distritoid asc;