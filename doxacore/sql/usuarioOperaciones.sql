SELECT  
	distinct o.operacion,
	u.account
FROM usuariosroles ur
left join usuarios u on u.usuarioid = ur.usuarioid
left join rolesoperaciones ro on ro.rolid = ur.rolid
left join operaciones o on o.operacionid = ro.operacionid
WHERE o.abremodulo = ?1 and u.account = '?2';