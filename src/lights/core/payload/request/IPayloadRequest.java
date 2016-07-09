package lights.core.payload.request;

import java.util.HashMap;
import java.util.List;

public abstract class IPayloadRequest<T> {
	
	protected HashMap<String, Object> parametros = new HashMap<String, Object>();

	protected Integer idSesion;
	
	protected String accessToken;
	
	public Integer getIdSesion() {
		return idSesion;
	}

	public void setIdSesion(Integer idSesion) {
		this.idSesion = idSesion;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}

	public HashMap<String, Object> getParametros() {
		return parametros;
	}
	
	public void setParametros(HashMap<String, Object> parametros) {
		this.parametros = parametros;
	}

	public void setParametro(String key, Object value) {
		this.parametros.put(key, value);
	}
	
	public Object getParametro(String key) {
		return this.parametros.get(key);
	}
	
	public abstract List<T> getObjetos();

	public abstract void setObjetos(List<T> objetos);

	public abstract T getObjeto();

	public abstract void setObjeto(T objeto);
	
}