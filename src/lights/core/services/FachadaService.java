package lights.core.services;

import java.util.Calendar;

import lights.seguridad.dao.AuditoriaDAO;
import lights.seguridad.dao.MetodoDaoDAO;
import lights.seguridad.dao.SesionDAO;
import lights.seguridad.dao.TablaDAO;
import lights.seguridad.dto.Auditoria;
import lights.seguridad.dto.MetodoDao;
import lights.seguridad.dto.Sesion;
import lights.seguridad.dto.Tabla;

public abstract class FachadaService<T> extends IServicioM<T>{

	@Override
	public boolean validarSesion(Integer idSesion, String accessToken) throws Exception {
		Sesion sesion =  new SesionDAO().find(idSesion);
		if (sesion == null) {
			throw new Exception(ERROR_1);
		}
		
		if (sesion.getAccessToken() == null || accessToken == null || !sesion.getAccessToken().equals(accessToken)) {
			throw new Exception(ERROR_8);
		}
		
		if (sesion.getEstado().equals(Sesion.INACTIVO)) {
			throw new Exception(ERROR_2);
		}
		
		return true;
	}

	@Override
	public void auditar(Integer idSesion, String nombreTabla, Integer accion,
			String nombreMetodoDao, Integer registroId, String datos) throws Exception {
		Sesion sesion = new SesionDAO().find(idSesion);
		
		if (sesion == null) {
			throw new Exception(ERROR_1);
		}
		
		Tabla tabla = new TablaDAO().findByNombre(nombreTabla);
		
		if (tabla == null) {
			throw new Exception(ERROR_5);
		}
		
		MetodoDao metodoDao = new MetodoDaoDAO().findByNombre(nombreMetodoDao);
		
		if (metodoDao == null) {
			throw new Exception(ERROR_6);
		}
		
		Auditoria auditoria = new Auditoria(sesion, tabla, accion, metodoDao, metodoDao, registroId, datos, Calendar.getInstance().getTimeInMillis());
		
		new AuditoriaDAO().save(auditoria);
	}

}
