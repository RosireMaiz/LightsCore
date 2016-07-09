package lights.core.googlecode.genericdao.search;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Stack;
import java.util.regex.Pattern;

import javax.persistence.Table;

public class MetadataUtil implements IMetadataUtil {

	private Class<?> classEntity;
	
	public MetadataUtil(Class<?> classEntity) {
		super();
		this.classEntity = classEntity;
	}

	@Override
	public Integer getId(Object o) {
		Class<?>[] c = {};
		Object[] parametersVoid = {};
		
		try {
			Method getMethod = classEntity.getMethod("getId" + classEntity.getSimpleName(), c);
			
			return (Integer) getMethod.invoke(o, parametersVoid);
			
		} catch (NoSuchMethodException | SecurityException | 
				IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
			
			return null;
		}
	}

	@Override
	public String getEntityName() {
//		return classEntity.getAnnotation(Table.class).name();
		return classEntity.getSimpleName();
	}
	
	public String getEntityName(Class<?> rootClass) {
//		return rootClass.getAnnotation(Table.class).name();
		return classEntity.getSimpleName();
	}
	
	public boolean isClass(Class<?> rootClass, String property, Class<?> questionClass) {
		rootClass = getJavaClass(rootClass, property);
		
		if (rootClass.equals(questionClass)) {
			return true;
		}
		
		return false;
	}

	public boolean isString(Class<?> rootClass, String property) {
		return isClass(rootClass, property, String.class);
	}
	
	public boolean isCollection(Class<?> rootClass, String property) {
		return isClass(rootClass, property, Collection.class);
	}
	
	public Class<?> getJavaClass(Class<?> rootClass, String property) {
		String[] tokens = property.split(Pattern.quote("."));
		
		Stack<String> pilaTokens = new Stack<String>();
		
		for (int i = tokens.length - 1; i >= 0; i--) {
			pilaTokens.push(tokens[i]);
		}
		
		while(!pilaTokens.isEmpty()) {
			try {
				rootClass = rootClass.getDeclaredField(pilaTokens.pop()).getType();
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		return rootClass;
	}

	public boolean isId(Class<?> rootClass, String property) {
		Class<?> principalClass = null;
		
		String[] tokens = property.split(Pattern.quote("."));
		String nombre = "";
		
		Stack<String> pilaTokens = new Stack<String>();
		
		for (int i = tokens.length - 1; i >= 0; i--) {
			pilaTokens.push(tokens[i]);
		}
		
		while(!pilaTokens.isEmpty()) {
			try {
				nombre = pilaTokens.pop();
				principalClass = rootClass;
				rootClass = rootClass.getDeclaredField(nombre).getType();
			} catch (NoSuchFieldException | SecurityException e) {
				e.printStackTrace();
				return false;
			}
		}
		
		if (nombre.equals("id" + getEntityName(principalClass)) && rootClass.equals(Integer.class)) {
			return true;
		}
		
		return false;
	}
	
	public boolean isEntity(Class<?> rootClass, String property) {
		rootClass = getJavaClass(rootClass, property);
		
		try {
			String nombre = rootClass.getAnnotation(Table.class).name();
			
			if (nombre != null) {
				return true;
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}
}
