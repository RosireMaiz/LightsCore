package lights.core.configuracion.dao;

//import java.util.ResourceBundle;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

public class EntityManagerHelper {

    private static EntityManagerFactory emf; 
    private static ThreadLocal<EntityManager> threadLocal;
    
//    private static ResourceBundle resource = ResourceBundle.getBundle("luces.delnorte.configuracion.luces_dao");

    static {
//    	emf = Persistence.createEntityManagerFactory(resource.getString("dao.persistenceUnitName"));
    	emf = Persistence.createEntityManagerFactory("persistenceUnit");
    	threadLocal = new ThreadLocal<EntityManager>();
    }

    public static EntityManager getEntityManager() {
		EntityManager entityManager = threadLocal.get();
		
		if (entityManager == null || !entityManager.isOpen()) {
			entityManager = emf.createEntityManager();
		    threadLocal.set(entityManager);
		}
		
		entityManager.clear();
		
		return entityManager;
    }

    public static void closeEntityManager() {
		EntityManager entityManager = threadLocal.get();
		
		threadLocal.set(null);
		if (entityManager != null) {
			entityManager.close();
		}
    }

    public static void beginTransaction() {
    	getEntityManager().getTransaction().begin();
    }

    public static void commit() {
    	getEntityManager().getTransaction().commit();
    }  

    public static void rollback() {
    	getEntityManager().getTransaction().rollback();
    } 

    public static Query createQuery(String query) {
    	return getEntityManager().createQuery(query);
    }

}
