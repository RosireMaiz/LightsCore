package lights.core.services;

import java.util.Calendar;

import ve.smile.seguridad.dao.AuditoriaDAO;
import ve.smile.seguridad.dao.SesionDAO;
import ve.smile.seguridad.dao.TablaDAO;
import ve.smile.seguridad.dto.Auditoria;
import ve.smile.seguridad.dto.Sesion;
import ve.smile.seguridad.dto.Tabla;

public abstract class FachadaService<T> extends IServicioM<T> {

	@Override
	public boolean validarSesion(Integer idSesion, String accessToken)
			throws Exception {
		Sesion sesion = new SesionDAO().find(idSesion);
		if (sesion == null) {
			throw new Exception(ERROR_1);
		}

		if (sesion.getAccessToken() == null || accessToken == null
				|| !sesion.getAccessToken().equals(accessToken)) {
			throw new Exception(ERROR_8);
		}

		if (sesion.getEstado().equals(Sesion.INACTIVO)) {
			throw new Exception(ERROR_2);
		}

		return true;
	}

	@Override
	public void auditar(Integer idSesion, String nombreTabla, Integer accion,
			Integer registroId, String datos) throws Exception {
		Sesion sesion = new SesionDAO().find(idSesion);

		if (sesion == null) {
			throw new Exception(ERROR_1);
		}

		Tabla tabla = new TablaDAO().findByNombre(nombreTabla);

		if (tabla == null) {
			throw new Exception(ERROR_5);
		}

		Auditoria auditoria = new Auditoria(sesion, tabla, accion, registroId,
				datos, Calendar.getInstance().getTimeInMillis());

		new AuditoriaDAO().save(auditoria);
	}

}
