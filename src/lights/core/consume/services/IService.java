package lights.core.consume.services;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;

import lights.core.annotations.Consume;
import lights.core.enums.TypeQuery;
import lights.core.payload.request.IPayloadRequest;
import lights.core.payload.response.IPayloadResponse;

@SuppressWarnings("unchecked")
public abstract class IService<T extends IPayloadResponse<V>, U extends IPayloadRequest<V>, V> {

	public static final String ERROR_UNKNOWN = "Error Code: ???-Error Desconocido (Consumidor Servicios Web):";

	public static final String NEW = "new";
	public static final String UPDATE = "update";
	public static final String DELETE = "delete";
	public static final String COUNT = "count";
	public static final String FIND_ONE = "find/one";
	public static final String FIND_PAGINATION = "find/pagination";
	public static final String FIND_CRITERIA = "find/criteria";
	public static final String FIND_ALL = "find/all";
	
	public String encodeURL(String value) {
		value = value.replace("%", "%2171");
		value = value.replace("/", "%3391");
		
		return value;
	}
	
	public String getUrlService(String tag) {
		return getUrlEndPoint() + "/" + tag;
	}
	
	public String getUrlEndPoint() {
		try {
			return Class.forName("karen.core.crux.session.DataCenter")
					.getMethod("getEndPoint", new Class<?>[] {})
					.invoke(null, new Object[] {})
					+ "/" + getClass().getAnnotation(Consume.class).value();
		} catch (Exception e) {
			e.printStackTrace();
			return "";
		}
	}
	
	public Integer getIdSesion() {
		try {
			return (Integer) Class.forName("karen.core.crux.session.DataCenter")
					.getMethod("getIdSesion", new Class<?>[] {})
					.invoke(null, new Object[] {});
		} catch (Exception e) {
			return -1;
		}
	}
	
	public String getAccessToken() {
		try {
			return (String) Class.forName("karen.core.crux.session.DataCenter")
					.getMethod("getAccessToken", new Class<?>[] {})
					.invoke(null, new Object[] {});
		} catch (Exception e) {
			return "";
		}
	}
	
	public T consultarUno(Integer id) {
		return doGet(getUrlService(FIND_ONE)  + "/" + getIdSesion() + "/" + getAccessToken() + "/" + id);
	};
	
	public T consultarTodos() {
		return consultarTodos("");
	};
	
	public T consultarTodos(String orderBy) {
		return doGet(getUrlService(FIND_ALL) + getPathToOrderBy(orderBy) +  "/" + getIdSesion() + "/" + getAccessToken());
	};
	
	public T consultarCriterios(TypeQuery typeQuery, Map<String, String> criterios) {
		return consultarCriterios(typeQuery, criterios, "");
	};
	
	public T consultarCriterios(TypeQuery typeQuery, Map<String, String> criterios, String orderBy) {
		String urlCriterios = "typeQueryToFind=" + typeQuery.name();

		for (String key : criterios.keySet()) {
			urlCriterios += ("&" + key + "=" + criterios.get(key));
		}
		
		return doGet(getUrlService(FIND_CRITERIA) + getPathToOrderBy(orderBy) + "/" + getIdSesion() + "/" + getAccessToken() + "?" + urlCriterios);
	};
	
	public T consultarPaginacionCriterios(Integer totalElementsByPage, Integer page, TypeQuery typeQuery, 
			Map<String, String> criterios) {
		return consultarPaginacionCriterios(totalElementsByPage, page, typeQuery, criterios, "");
	};
	
	public T consultarPaginacionCriterios(Integer totalElementsByPage, Integer page, TypeQuery typeQuery,
			Map<String, String> criterios, String orderBy) {
		String urlCriterios = "typeQueryToFind=" + typeQuery.name();

		for (String key : criterios.keySet()) {
			urlCriterios += ("&" + key + "=" + criterios.get(key));
		}

		return doGet(getUrlService(FIND_PAGINATION) + getPathToOrderBy(orderBy) + "/" + getIdSesion() + "/" + getAccessToken() + "/" + totalElementsByPage + "/" + page + "?" + urlCriterios);
	};
	
	public T consultarPaginacion(Integer totalElementsByPage, Integer page) {
		return consultarPaginacion(totalElementsByPage, page, "");
	};
	
	public T consultarPaginacion(Integer totalElementsByPage, Integer page, String orderBy) {
		return doGet(getUrlService(FIND_PAGINATION) + getPathToOrderBy(orderBy) + "/" + getIdSesion() + "/" + getAccessToken() + "/" + totalElementsByPage + "/" + page);
	};
	
	public T eliminar(Integer id) {
		return doGet(getUrlService(DELETE) + "/" + getIdSesion() + "/" + getAccessToken() + "/" + id);
	};
	
	public T contar() {
		return doGet(getUrlService(COUNT) + "/" + getIdSesion() + "/" + getAccessToken());
	};
	
	public T incluir(V object) {
		return doPost(getUrlService(NEW), object);
	};
	
	public T modificar(V object) {
		return doPost(getUrlService(UPDATE), object);
	};
	
	public T doPost(String urlServiceToConsume, V object) {
		return doPost(urlServiceToConsume, object, false, new HashMap<String, Object>());
	}
	
	public T doPost(String urlServiceToConsume, V object, HashMap<String, Object> parametros) {
		return doPost(urlServiceToConsume, object, false, parametros);
	}
	
	public T doPost(String urlServiceToConsume, V object, Boolean free, HashMap<String, Object> parametros) {
		T payload = null;

		try {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters()
	        	.add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

			U request = newInstanceOfPayloadRequest();
			
			if (!free) {
				request.setIdSesion(getIdSesion());
				request.setAccessToken(getAccessToken());
			}

			request.setObjeto(object);
			request.setParametros(parametros);

			Gson gson = new Gson();

			String data = restTemplate.postForObject(urlServiceToConsume, request, String.class);

			payload = (T) gson.fromJson(data, getClassPayloadResponse());
		} catch (Exception e) {
			return buildAnswerError(e);
		}

		return payload;
	}
	
	public T doGet(String urlServiceToConsume) {
		T payload = null;

		try {
			RestTemplate restTemplate = new RestTemplate();
			restTemplate.getMessageConverters()
	        	.add(0, new StringHttpMessageConverter(Charset.forName("UTF-8")));

			Gson gson = new Gson();

			String data = restTemplate.getForObject(urlServiceToConsume, String.class);

			payload = (T) gson.fromJson(data, getClassPayloadResponse());
		} catch (Exception e) {
			return buildAnswerError(e);
		}

		return payload;
	}
	
	private T buildAnswerError(Exception e) {
		T payload;
		e.printStackTrace();
		try {
			payload = newInstanceOfPayloadResponse();
			
			payload.setInformacion("isOK", false);

			payload.setInformacion("mensaje", "E:" + ERROR_UNKNOWN + e.getCause());

			return payload;
		} catch (Exception e1) {
			return null;
		}
	}
	
	private T newInstanceOfPayloadResponse() {
		try {			
			return (T) getClassPayloadResponse().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	private U newInstanceOfPayloadRequest() {
		try {			
			return (U) getClassPayloadRequest().newInstance();
		} catch (Exception e) {
			return null;
		}
	}
	
	public Class<?> getClassPayloadResponse() {
		try {
			Type superClass = getClass().getGenericSuperclass();

			Type tArgument = ((ParameterizedType) superClass).getActualTypeArguments()[0];

			String className = tArgument.toString().split(" ")[1];

			return Class.forName(className);
		} catch (Exception e) {
			return null;
		}
		
	}
	
	public Class<?> getClassPayloadRequest() {
		try {
			Type superClass = getClass().getGenericSuperclass();

			Type tArgument = ((ParameterizedType) superClass).getActualTypeArguments()[1];

			String className = tArgument.toString().split(" ")[1];

			return Class.forName(className);
		} catch (Exception e) {
			return null;
		}		
	}
	
	private String getPathToOrderBy(String orderBy) {
		if (orderBy.equals("")) {
			return "";
		}
		return "/orderby/" + orderBy;
	}
}
