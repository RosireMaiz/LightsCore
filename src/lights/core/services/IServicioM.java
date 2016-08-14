package lights.core.services;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;
import javax.persistence.Table;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.json.JSONObject;

import com.google.gson.Gson;

import lights.core.googlecode.genericdao.dao.jpa.BaseDAO;
import lights.core.googlecode.genericdao.search.Filter;
import lights.core.googlecode.genericdao.search.MetadataUtil;
import lights.core.googlecode.genericdao.search.Search;
import lights.core.googlecode.genericdao.search.Sort;
import lights.core.payload.request.IPayloadRequest;
import lights.core.payload.response.IPayloadResponse;
import ve.smile.seguridad.enums.AccionEnum;

public abstract class IServicioM<T> {
	
	public static final String CONTACT = "El error ya fue registrado, por favor contacte al administrador.";
	public static final String ERROR_UNKNOWN = "Error Code: ???-Error Desconocido (Servicios Web). " + CONTACT;
	public static final String ERROR_1 = "Error Code: 001-Sesión no válida.";
	public static final String ERROR_2 = "Error Code: 002-Sesión inactiva. ";
	public static final String ERROR_3 = "Error Code: 003-Ha ocurrido un error al construir el error. " + CONTACT;
	public static final String ERROR_4 = "Error Code: 004-Ha ocurrido un error y el registro no pudo ser eliminado";
	public static final String ERROR_5 = "Error Code: 005-La tabla sobre la cual se quiere realizar esta operacion no se encuentra registrada en la base de datos";
	public static final String ERROR_6 = "Error Code: 006-El método (DAO) utilizado no se encuentra registrada en la base de datos";
	public static final String ERROR_7 = "Error Code: 007-Propiedad %s no encontrada en la entidad";
	public static final String ERROR_8 = "Error Code: 008-Access Token Inválida. ";
	public static final String INFORMATION_1 = "Information Code: 001-Registro %d no encontrado";
	public static final String INFORMATION_2 = "Information Code: 002-No se ha encontrado ningún registro";
	
	public static final String SUCCESS_1 = "Success Code: 001-Registro consultado con exito";
	public static final String SUCCESS_2 = "Success Code: 002-Registros consultados con exito";
	public static final String SUCCESS_3 = "Success Code: 003-Registro eliminado con exito";
	public static final String SUCCESS_4 = "Success Code: 004-Registro incluido con exito";
	public static final String SUCCESS_5 = "Success Code: 005-Registro modificado con exito";
	public static final String SUCCESS_6 = "Success Code: 006-Registros incluidos con exito";
	public static final String SUCCESS_7 = "Success Code: 007-Cantidad consultada con exito";
	
	public static final String WARNING_MESSAGE = "W";
	public static final String INFORMATION_MESSAGE = "I";
	public static final String ERROR_MESSAGE = "E";
	public static final String SUCCESS_MESSAGE = "S";
	
	public static final String SEPARATOR = ";_;_;";
	
	@GET
	@Path("/find/one/{idSesion}/{accessToken}/{id}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String pathConsultarUno(@PathParam("idSesion") Integer idSesion, @PathParam("accessToken") String accessToken,
			@PathParam("id") Integer id) {
		try {
			if (validarSesion(idSesion, accessToken)) {
				return consultarUno(idSesion, id);
			}
		} catch (Exception e) {
			return buildAnswerError(e);
		}
		
		return buildAnswerError(new Exception(ERROR_UNKNOWN));
	}
	
	@GET
	@Path("/find/all{orderby:(/orderby/[^/]+?)?}/{idSesion}/{accessToken}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String pathConsultarTodos(@PathParam("idSesion") Integer idSesion, @PathParam("accessToken") String accessToken,
			@PathParam("orderby") String orderBy) {
		try {
			if (validarSesion(idSesion, accessToken)) {
				return consultarTodos(idSesion, orderBy);
			}
		} catch (Exception e) {
			return buildAnswerError(e);
		}
		
		return buildAnswerError(new Exception(ERROR_UNKNOWN));
	}

	@GET
	@Path("/find/criteria{orderby:(/orderby/[^/]+?)?}/{idSesion}/{accessToken}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String pathConsultarPorCriterios(@PathParam("idSesion") Integer idSesion, @PathParam("accessToken") String accessToken,
			@PathParam("orderby") String orderBy, @Context UriInfo info) {
		MultivaluedMap<String, String> multivaluesMapFilters = info.getQueryParameters();
		
		if (multivaluesMapFilters.size() <= 1) {
			return pathConsultarTodos(idSesion, accessToken, orderBy);
		}
		
		try {
			TypeQuery typeQuery;
			
			try {
				typeQuery = TypeQuery.valueOf(multivaluesMapFilters.get("typeQueryToFind").get(0));
			} catch (Exception e) {
				typeQuery = TypeQuery.LIKE;
			}
			
			Map<String, Object> mapaFiltros = buildMapFromMultiValuesMap(multivaluesMapFilters);
			
			if (validarSesion(idSesion, accessToken)) {
				return consultarPorCriterios(idSesion, typeQuery, mapaFiltros, orderBy);
			}
		} catch (Exception e) {
			return buildAnswerError(e);
		}
		
		return buildAnswerError(new Exception(ERROR_UNKNOWN));
	}
	
	@GET
	@Path("/find/pagination{orderby:(/orderby/[^/]+?)?}/{idSesion}/{accessToken}/{count}/{page}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String pathConsultarPorCriteriosPaginacion(@PathParam("idSesion") Integer idSesion, @PathParam("accessToken") String accessToken,
			@PathParam("count") Integer count, @PathParam("page") Integer page, 
			@PathParam("orderby") String orderBy, @Context UriInfo info) {
		MultivaluedMap<String, String> multivaluesMapFilters = info.getQueryParameters();
		
		try {
			if (validarSesion(idSesion, accessToken)) {
				if (multivaluesMapFilters.size() <= 1) {
					return consultarPaginacion(idSesion, count, page, new Search(), orderBy);
				}
				
				TypeQuery typeQuery;
				
				try {
					typeQuery = TypeQuery.valueOf(multivaluesMapFilters.get("typeQueryToFind").get(0));
				} catch (Exception e) {
					typeQuery = TypeQuery.LIKE;
				}
				
				Map<String, Object> mapaFiltros = buildMapFromMultiValuesMap(multivaluesMapFilters);
				
				return consultarPaginacion(idSesion, count, page, buildSearchFromMap(typeQuery, mapaFiltros), orderBy);
			}
		} catch (Exception e) {
			return buildAnswerError(e);
		}
		
		return buildAnswerError(new Exception(ERROR_UNKNOWN));
	}

	@GET
	@Path("/delete/{idSesion}/{accessToken}/{id}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String pathEliminar(@PathParam("idSesion") Integer idSesion, @PathParam("accessToken") String accessToken, @PathParam("id") Integer id) {
		try {
			if (validarSesion(idSesion, accessToken)) {
				return eliminar(idSesion, id);
			}
		} catch (Exception e) {
			return buildAnswerError(e);
		}
		
		return buildAnswerError(new Exception(ERROR_UNKNOWN));
	}
	
	@GET
	@Path("/count/{idSesion}/{accessToken}")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public String pathCount(@PathParam("idSesion") Integer idSesion, @PathParam("accessToken") String accessToken) {
		try {
			if (validarSesion(idSesion, accessToken)) {
				return contar(idSesion);
			}
		} catch (Exception e) {
			return buildAnswerError(e);
		}
		
		return buildAnswerError(new Exception(ERROR_UNKNOWN));
	}

	@POST
	@Path("/new")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@SuppressWarnings({ "unchecked" })
	public String pathIncluir(String data) {
		Gson gson = new Gson();
		
		IPayloadRequest<T> request = (IPayloadRequest<T>) gson.fromJson(data, getClassPayloadRequest());
		
		try {
			if (validarSesion(request.getIdSesion(), request.getAccessToken())) {
				return incluir(request.getIdSesion(), (T) request.getObjeto());
			}
		} catch (Exception e) {
			return buildAnswerError(e);
		}
		
		return buildAnswerError(new Exception(ERROR_UNKNOWN));
	}
	
//	@POST
//	@Path("/incluirTodos")
//	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
//	@SuppressWarnings({ "unchecked" })
//	public String pathIncluirTodos(String data) {
//		Gson gson = new Gson();
//		
//		IPayloadRequest<T> request = (IPayloadRequest<T>) gson.fromJson(data, getClassPayloadRequest());
//		
//		try {
//			if (validarSesion(request.getIdSesion())) {
//				return incluirTodos(request.getIdSesion(), request.getObjetos());
//			}
//		} catch (Exception e) {
//			return buildAnswerError(e);
//		}
//		
//		return buildAnswerError(new Exception(ERROR_UNKNOWN));
//	}
	
	@POST
	@Path("/update")
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	@SuppressWarnings("unchecked")
	public String pathModificar(String data) {
		Gson gson = new Gson();
		
		IPayloadRequest<T> request = (IPayloadRequest<T>) gson.fromJson(data, getClassPayloadRequest());
		
		try {
			if (validarSesion(request.getIdSesion(), request.getAccessToken())) {
				return modificar(request.getIdSesion(), (T) request.getObjeto());
			}
		} catch (Exception e) {
			return buildAnswerError(e);
		}
		
		return buildAnswerError(new Exception(ERROR_UNKNOWN));
	}
	
	public String consultarUno(Integer idSesion, Integer id) throws Exception {
		T t = getDAO().find(id);
		
		auditar(idSesion, getTable() , AccionEnum.CONSULTAR.ordinal(), getPath().substring(1) + ".CONSULTAR_UNO", 0, String.valueOf(id));
		
		if (t == null) {
			return buildAnswerWarning(String.format(INFORMATION_1, id));
		}
		
		return buildAnswerSuccess(t, String.format(SUCCESS_1));
	};

	public String consultarTodos(Integer idSesion, String orderBy) throws Exception {
		List<T> objects = null;
		
		if (orderBy.equals("")) {
			objects = getDAO().findAll();
		} else {
			Search search = new Search();
			search.addSort(getSortFromStringPath(orderBy));//TODO VALIDAR PROPIEDAD VALIDA

			objects = getDAO().search(search);
		}
		
		auditar(idSesion, getTable() , AccionEnum.CONSULTAR.ordinal(), getPath().substring(1) + ".CONSULTAR_TODOS", 0, "");
		
		if (objects.size() == 0) {
			return buildAnswerInformation(objects, INFORMATION_2);
		}
		
		return buildAnswerSuccess(objects, SUCCESS_2);
	};
	
	public String consultarPorCriterios(Integer idSesion, TypeQuery typeQuery, Map<String, Object> mapaFiltros, String orderBy) throws Exception {
		List<T> objects = null;
		
		Search search = buildSearchFromMap(typeQuery, mapaFiltros);
		
		if (orderBy.equals("")) {
			objects = getDAO().search(search);
		} else {
			search.addSort(getSortFromStringPath(orderBy));//TODO VALIDAR PROPIEDAD VALIDA

			objects = getDAO().search(search);
		}
		
		String datos = "";
		
		for (Filter filter : search.getFilters()) {
			if (datos.length() > 0) {
				datos += SEPARATOR;
			}
			
			datos += filter.getProperty() + " " + getOperator(filter.getOperator()) + " " + filter.getValue();
		}
		
		auditar(idSesion, getTable() , AccionEnum.CONSULTAR.ordinal(), getPath().substring(1) + ".CONSULTAR_CRITERIOS", 0, datos);
		
		if (objects.size() == 0) {
			return buildAnswerInformation(objects, INFORMATION_2);
		}
		
		return buildAnswerSuccess(objects, SUCCESS_2);
	};
	
	public String consultarPaginacion(Integer idSesion, Integer totalElementsByPage, Integer page, 
			Search search, String orderBy) throws Exception {
		Integer totalElementsInTable = getDAO().count(search);
		
		boolean firstPage = true;
		boolean lastPage = true;
		
		Integer totalPaginas = totalElementsInTable / totalElementsByPage;
		
		if (totalElementsInTable % totalElementsByPage > 0) {
			totalPaginas++;
		}
		
		if (page > totalPaginas) {
			if (totalPaginas == 0) {
				page = 0;
			} else {
				page = 1;
			}
		}
		
		if (page > 1) {
			firstPage = false;
		}
		
		if (page != totalPaginas) {
			lastPage = false;
		}
		
		Integer firstResult = page * totalElementsByPage - totalElementsByPage;
		Integer maxResults = totalElementsByPage;
		
		if (firstResult + totalElementsByPage > totalElementsInTable) {
			maxResults = totalElementsInTable - firstResult;
		}
		
		search.setFirstResult(firstResult);
		search.setMaxResults(maxResults);
		
		if (orderBy.equals("")) {
			search.addSort("id" + getClassParameterizedType().getSimpleName(), false);
		} else {
			search.addSort(getSortFromStringPath(orderBy));
		}
		
		List<T> objects = getDAO().search(search);
		
		Map<String, Object> mapaPaginacion = new HashMap<String, Object>();
		
		mapaPaginacion.put("page", page);
		mapaPaginacion.put("totalElementsByPage", totalElementsByPage);
		mapaPaginacion.put("totalElementsInTable", totalElementsInTable);
		mapaPaginacion.put("totalPaginas", totalPaginas);
		mapaPaginacion.put("totalElementsReturned", objects.size());
		mapaPaginacion.put("firstPage", firstPage);
		mapaPaginacion.put("lastPage", lastPage);
		
		String datos = "totalElementsByPage=" + totalElementsByPage + SEPARATOR + "page=" + page;
		
		for (Filter filter : search.getFilters()) {
			datos += SEPARATOR + filter.getProperty() + " " + getOperator(filter.getOperator()) + " " + filter.getValue();
		}
		
		auditar(idSesion, getTable() , AccionEnum.CONSULTAR.ordinal(), getPath().substring(1) + ".CONSULTAR_PAGINACION_CRITERIOS", 0, datos);
		
		if (objects.size() == 0) {
			return buildAnswerInformation(objects, INFORMATION_2, mapaPaginacion);
		}

		return buildAnswerSuccess(objects, SUCCESS_2, mapaPaginacion);
	};
	
	public String eliminar(Integer idSesion, Integer id) throws Exception {
		auditar(idSesion, getTable() , AccionEnum.ELIMINAR.ordinal(), getPath().substring(1) + ".ELIMINAR", id, "");
		
		if (getDAO().removeById(id)) {
			return buildAnswerSuccess(SUCCESS_3);
		}
		return buildAnswerError(new Exception(ERROR_4));
	};
	
	public String contar(Integer idSesion) throws Exception {
		auditar(idSesion, getTable() , AccionEnum.CONSULTAR.ordinal(), getPath().substring(1) + ".CONTAR", 0, "");
		
		Integer count = getDAO().count(new Search());
		
		Map<String, Object> mapa = new HashMap<String, Object>();
		
		mapa.put(IPayloadResponse.COUNT, count);
		
		return buildAnswerSuccess(SUCCESS_7, mapa);
	};
	
	public String incluir(Integer idSesion, T object) throws Exception {
		Object o = getDAO().save(object);
		
		String datos = getDataFromObjectToAuditoria(o);
		
		Map<String, Object> mapa = new HashMap<String, Object>();
		
		mapa.put("id", getMetadataUtil().getId(o));
		
		auditar(idSesion, getTable() , AccionEnum.INCLUIR.ordinal(), getPath().substring(1) + ".INCLUIR", getMetadataUtil().getId(o), datos);
		
		return buildAnswerSuccess(SUCCESS_4, mapa);
	};
	
//	public String incluirTodos(Integer idSesion, List<T> objects) throws Exception {
//		Map<String, Object> mapa = new HashMap<String, Object>();
//		
//		int i = 1;
//		
//		for (T object : objects) {
//			Object o = getDAO().save(object);
//			
//			String datos = "";
//			
//			mapa.put("id" + i++, getMetadataUtil().getId(o));
//			
//			auditar(idSesion, getTable() , AccionEnum.INCLUIR.ordinal(), getPath().substring(1) + ".INCLUIR", getMetadataUtil().getId(o), datos);
//		}
//		return buildAnswerSuccess(SUCCESS_4, mapa);
//	};
	
	public String modificar(Integer idSesion, T object) throws Exception {
		Object o = getDAO().save(object);
		
		String datos = getDataFromObjectToAuditoria(o);
		
		auditar(idSesion, getTable() , AccionEnum.MODIFICAR.ordinal(), getPath().substring(1) + ".MODIFICAR", getMetadataUtil().getId(o), datos);
		
		return buildAnswerSuccess(SUCCESS_5);
	};
	
	public String getDataFromObjectToAuditoria(Object object) {
		JSONObject jsonObject = new JSONObject(object);
		
//		String data = "";
//		
//		for (Object key : jsonObject.keySet()) {
//			if (data.length() > 0) {
//				data += SEPARATOR;
//			}
//			
//			jsonObject.getString("");
//			
//			data += key + "=" + jsonObject.get((String) key);
//		}
		
		return jsonObject.toString();
	}
	
	public MetadataUtil getMetadataUtil() throws Exception {
		return new MetadataUtil(getClassParameterizedType());
	}
	
	public Class<?> getClassParameterizedType() throws Exception {
		Type superClass = getClass().getGenericSuperclass();
		
		Type tArgument = ((ParameterizedType) superClass).getActualTypeArguments()[0];

		String className = tArgument.toString().split(" ")[1];
		
		try {
			return Class.forName(className);
		} catch (ClassNotFoundException e) {
			return null;
		}
	}
	
	public Object castTo(Class<?> classToCast, String value) throws Exception {
		if (classToCast.equals(Integer.class)) {
			return Integer.valueOf(value);
		}
		if (classToCast.equals(Long.class)) {
			return Long.valueOf(value);
		}
		if (classToCast.equals(String.class)) {
			return decodeUrl(value);
		}
		return value; 
	}
	
	public String decodeUrl(String value) {
//		System.out.println("Antes: " + value);
		value = value.replace("%C3%91", "Ã‘");
		value = value.replace("%C3%93", "Ã“");
		value = value.replace("%C3%9A", "Ãš");
		value = value.replace("%C3%8D", "Ã�");
		value = value.replace("%C3%89", "Ã‰");
		value = value.replace("%C3%81", "Ã�");
		value = value.replace("%20", " ");

		
//		value = value.replace("%3391", "/");
//		value = value.replace("%2171", "%");
//		System.out.println("Despues: " + value);
		return value;
	}
	
	public Search buildSearchFromMap(TypeQuery typeQuery, Map<String, Object> mapaFiltros) throws Exception {
		Search s = new Search();

		for (String key : mapaFiltros.keySet()) {
			if (mapaFiltros.get(key).getClass().equals(String.class)) {
				if (typeQuery.equals(TypeQuery.EQUAL)) {
					s.addFilterEqual(key, mapaFiltros.get(key));
				} else {//typeQuery.equals(TypeQuery.LIKE)
					s.addFilterLike(key, "%" + mapaFiltros.get(key) + "%");
				}
			} else {
				s.addFilterEqual(key, mapaFiltros.get(key));
			}
		}
		
		return s;
	}
	
	public enum TypeQuery {
		EQUAL, LIKE;
	}
	
	public Map<String, Object> buildMapFromMultiValuesMap(MultivaluedMap<String, String> multivaluesMapFilters) throws Exception {
		Map<String, Object> mapFilters = new HashMap<String, Object>();
		MetadataUtil metadataUtil = getMetadataUtil();
		
		for (String key : multivaluesMapFilters.keySet()) {
			if (key.equals("typeQueryToFind")) {
				continue;
			}
			Class<?> clase = metadataUtil.getJavaClass(getClassParameterizedType(), key);
			
			if (clase == null) {
				throw new Exception(String.format(ERROR_7, key));
			}
			mapFilters.put(key, castTo(clase, multivaluesMapFilters.get(key).get(0)));
		}
		
		return mapFilters;
	}
	
	public String getPath() throws Exception {
		return this.getClass().getAnnotation(Path.class).value();
	}
	
	public String getTable() throws Exception {
		return getClassParameterizedType().getAnnotation(Table.class).name();
	}
	
	public String getNameClassPluralToShowToUser() throws Exception {
//		return getClassParameterizedType().getSimpleName();
		return "Registros";
	}
	
	public String getNameClassSingularToShowToUser() throws Exception {
//		return getClassParameterizedType().getSimpleName();
		return "Registro";
	}
	
	private String buildAnswerGeneral(String typeMessage, boolean isOk, T object, String mensaje) throws Exception {
		return buildAnswerGeneral(typeMessage, isOk, object, mensaje, new HashMap<String, Object>());
	}
	
	private String buildAnswerGeneral(String typeMessage, boolean isOk, T object, String mensaje, Map<String, Object> datosExtras) throws Exception {
		IPayloadResponse<T> payloadResponse = getPayload();
		
		if (datosExtras == null) {
			datosExtras = new HashMap<String, Object>();
		}
		
		for (String key : datosExtras.keySet()) {
			payloadResponse.setInformacion(key, datosExtras.get(key));
		}
				
		ArrayList<T> objetos = new ArrayList<T>();
		
		objetos.add(object);
		
		payloadResponse.setObjetos(objetos);
		
		payloadResponse.setInformacion(IPayloadResponse.MENSAJE, typeMessage + ":" + mensaje);
		payloadResponse.setInformacion(IPayloadResponse.IS_OK, isOk);
		
		return new JSONObject(payloadResponse).toString();
	}
	
	private String buildAnswerGeneral(String typeMessage, boolean isOk, List<T> objects, String mensaje) throws Exception {
		return buildAnswerGeneral(typeMessage, isOk, objects, mensaje, new HashMap<String, Object>());
	}
	
	private String buildAnswerGeneral(String typeMessage, boolean isOk, List<T> objects, String mensaje, Map<String, Object> datosExtras) throws Exception {
		IPayloadResponse<T> payloadResponse = getPayload();
		
		if (datosExtras == null) {
			datosExtras = new HashMap<String, Object>();
		}
		
		for (String key : datosExtras.keySet()) {
			payloadResponse.setInformacion(key, datosExtras.get(key));
		}
		
		payloadResponse.setObjetos(objects);
		
		payloadResponse.setInformacion("mensaje", typeMessage + ":" + mensaje);
		payloadResponse.setInformacion("isOK", new Boolean(isOk));
		
		return new JSONObject(payloadResponse).toString();
	}
	
	private String buildAnswerGeneral(String typeMessage, boolean isOk, String mensaje) throws Exception {
		return buildAnswerGeneral(typeMessage, isOk, mensaje, new HashMap<String, Object>());
	}
	
	private String buildAnswerGeneral(String typeMessage, boolean isOk, String mensaje, Map<String, Object> datosExtras) throws Exception {
		IPayloadResponse<T> payloadResponse = getPayload();
		
		if (datosExtras == null) {
			datosExtras = new HashMap<String, Object>();
		}
		
		for (String key : datosExtras.keySet()) {
			payloadResponse.setInformacion(key, datosExtras.get(key));
		}
		
		payloadResponse.setInformacion("mensaje", typeMessage + ":" + mensaje);
		payloadResponse.setInformacion("isOK", new Boolean(isOk));
		
		return new JSONObject(payloadResponse).toString();
	}
	
	@SuppressWarnings("unchecked")
	public IPayloadResponse<T> getPayload() throws Exception {
		String className = getClassParameterizedType().getName().replace(".dto.", ".payload.response.Payload") + "Response";
		
		return (IPayloadResponse<T>) Class.forName(className).newInstance();
	}
	
	public String buildAnswerWarning(T object, String mensaje) throws Exception {
		return buildAnswerGeneral(WARNING_MESSAGE, true, object, mensaje);
	}
	
	public String buildAnswerWarning(List<T> objects, String mensaje) throws Exception {
		return buildAnswerGeneral(WARNING_MESSAGE, true, objects, mensaje);
	}
	
	public String buildAnswerWarning(String mensaje) throws Exception {
		return buildAnswerGeneral(WARNING_MESSAGE, true, mensaje);
	}
	
	public String buildAnswerSuccess(T object, String mensaje) throws Exception {
		return buildAnswerGeneral(SUCCESS_MESSAGE, true, object, mensaje);
	}
	
	public String buildAnswerSuccess(List<T> objects, String mensaje) throws Exception {
		return buildAnswerGeneral(SUCCESS_MESSAGE, true, objects, mensaje, new HashMap<String, Object>());
	}
	
	public String buildAnswerSuccess(List<T> objects, String mensaje, Map<String, Object> map) throws Exception {
		return buildAnswerGeneral(SUCCESS_MESSAGE, true, objects, mensaje, map);
	}
	
	public String buildAnswerSuccess(String mensaje) throws Exception {
		return buildAnswerGeneral(SUCCESS_MESSAGE, true, mensaje, new HashMap<String, Object>());
	}
	
	public String buildAnswerSuccess(String mensaje, Map<String, Object> map) throws Exception {
		return buildAnswerGeneral(SUCCESS_MESSAGE, true, mensaje, map);
	}
	
	public String buildAnswerInformation(T object, String mensaje) throws Exception {
		return buildAnswerGeneral(INFORMATION_MESSAGE, true, object, mensaje);
	}
	
	public String buildAnswerInformation(List<T> objects, String mensaje) throws Exception {
		return buildAnswerInformation(objects, mensaje, new HashMap<String, Object>());
	}
	
	public String buildAnswerInformation(List<T> objects, String mensaje, Map<String, Object> map) throws Exception {
		return buildAnswerGeneral(INFORMATION_MESSAGE, true, objects, mensaje, map);
	}
	
	public String buildAnswerInformation(String mensaje) throws Exception {
		return buildAnswerGeneral(INFORMATION_MESSAGE, true, mensaje);
	}
	
	public String buildAnswerError(Exception e) {
		try {
			if (e.getMessage() != null && e.getMessage().contains("Error Code:")) {
				String codeError = getCodeOfError(e.getMessage());
				
				if (!codeError.equals("001") && !codeError.equals("002")) {
					e.printStackTrace();//Mensaje en el log de glassfish
				}
				
				return buildAnswerGeneral(ERROR_MESSAGE, false, e.getMessage());
			}
			
			e.printStackTrace();//Mensaje en el log de glassfish
			return buildAnswerGeneral(ERROR_MESSAGE, false, ERROR_UNKNOWN);
		} catch (Exception e1) {
			e1.printStackTrace();//Mensaje en el log de glassfish
			return "{\"informacion\":{\"isOK\":false,\"mensaje\":\"E:" + ERROR_3 + "\"}}";
		}
	}
	
	public String getCodeOfError(String message) {
		try {
			return message.substring(12, 15);
		} catch (Exception e) {
			return "UNKNOWN";
		}
	}
	
	@SuppressWarnings("unchecked")
	public BaseDAO<T> getDAO() throws Exception {
		String className = getClassParameterizedType().getName().replace(".dto.", ".dao.") + "DAO";
		
		return (BaseDAO<T>) Class.forName(className).newInstance();
	}
	
	public Query createQuery(String ql) {
		try {
			return getDAO().createQuery(ql);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public Class<?> getClassPayloadRequest() {
		try {
			String className = getClassParameterizedType().getName().replace(".dto.", ".payload.request.Payload") + "Request";;
			
			return Class.forName(className);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Sort getSortFromStringPath(String orderby) {
		orderby = orderby.split("/")[2]; // --  '/orderby/propiety=value'
		
		return new Sort(orderby.split("=")[0], new Boolean(orderby.split("=")[1]));
	}
	
	public String getOperator(Integer id) {
		switch (id) {
		case 0:
			return "EQUAL";
//		case 1:
//			return "NOT_EQUAL";
//		case 2:
//			return "LESS_THAN";
//		case 3:
//			return "GREATER_THAN";
//		case 4:
//			return "LESS_OR_EQUAL";
//		case 5:
//			return "GREATER_OR_EQUAL";
		case 6:
			return "LIKE";
//		case 7:
//			return "ILIKE";
//		case 8:
//			return "IN";
//		case 9:
//			return "NOT_IN";
//		case 10:
//			return "NULL";
//		case 11:
//			return "NOT_NULL";
//		case 12:
//			return "EMPTY";
//		case 13:
//			return "NOT_EMPTY";
//		case 100:
//			return "AND";
//		case 101:
//			return "OR";
//		case 102:
//			return "NOT";
//		case 200:
//			return "SOME";
//		case 201:
//			return "ALL";
//		case 202:
//			return "NONE";
//		case 999:
//			return "CUSTOM";
		default:
			return "UNKNOWN";
		}
	}
	
	public abstract boolean validarSesion(Integer idSesion, String accessToken) throws Exception;
	
	public abstract void auditar(Integer idSesion, String fkTabla, Integer accion, 
			String fkMetodoDao, Integer registroId, String datos) throws Exception;
}