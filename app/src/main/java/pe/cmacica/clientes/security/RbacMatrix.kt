package pe.cmacica.clientes.security

/** Matriz de permisos RBAC — Criterio 4 rúbrica académica. */
object RbacMatrix {
    fun canAccessOwnAccount(role: UserRole): Boolean = role == UserRole.CLIENTE

    fun canCreateSolicitud(role: UserRole): Boolean = role == UserRole.CLIENTE

    fun canViewReportes(role: UserRole): Boolean =
        role == UserRole.SUPERVISOR || role == UserRole.ADMINISTRADOR

    fun canManageContadores(role: UserRole): Boolean = role == UserRole.ADMINISTRADOR
}
