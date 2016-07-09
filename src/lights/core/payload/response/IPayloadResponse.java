package lights.core.payload.response; 

import java.util.List;

public interface IPayloadResponse<T> {
	
	public static String IS_OK = "isOK";
	public static String MENSAJE = "mensaje";
	public static String ID_SESION = "idSesion";
	public static String ACCESS_TOKEN = "accessToken";
	public static String COUNT = "count";
	
	public Object getInformacion(String key);

	public void setInformacion(String key, Object informacion);

	public List<T> getObjetos();

	public void setObjetos(List<T> data);
}
