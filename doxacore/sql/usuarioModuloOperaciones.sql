SELECT  
	distinct o.operacion,
	u.account,
	m.modulo
FROM usuariosroles ur
left join usuarios u on u.usuarioid = ur.usuarioid
left join rolesoperaciones ro on ro.rolid = ur.rolid
left join operaciones o on o.operacionid = ro.operacionid
left join modulos m on m.moduloid = o.moduloid
WHERE o.abremodulo = false and u.account = '?1' and m.modulo = '?2' ;