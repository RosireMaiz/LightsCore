package lights.core.googlecode.genericdao.dao.jpa;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;

import lights.core.configuracion.dao.EntityManagerHelper;
import lights.core.googlecode.genericdao.search.ISearch;
import lights.core.googlecode.genericdao.search.MetadataUtil;
import lights.core.googlecode.genericdao.search.jpa.JPASearchProcessor;

@SuppressWarnings("unchecked")
public class JPABaseDAO {

	private JPASearchProcessor searchProcessor;

	public void setSearchProcessor(JPASearchProcessor searchProcessor) {
		this.searchProcessor = searchProcessor;
	}

	protected JPASearchProcessor getSearchProcessor() {
		return searchProcessor;
	}

	private EntityManager entityManager = EntityManagerHelper.getEntityManager();

	/**
	 * Get the current EntityManager
	 */
	protected EntityManager em() {
		return entityManager;
	}

	protected MetadataUtil getMetadataUtil() {
		return searchProcessor.getMetadataUtil();
	}

	/**
	 * <p>
	 * Make a transient instance persistent and add it to the datastore. This
	 * operation cascades to associated instances if the association is mapped
	 * with cascade="persist". Throws an error if the entity already exists.
	 * 
	 * <p>
	 * Does not guarantee that the object will be assigned an identifier
	 * immediately. With <code>persist</code> a datastore-generated id may not
	 * be pulled until flush time.
	 */
	protected void _persist(Object... entities) {
		for (Object entity : entities) {
			
			if (entity != null) {
				em().getTransaction().begin();
				em().persist(entity);
				em().getTransaction().commit();
			}
				
		}
	}

	/**
	 * Remove the entity of the specified class with the specified id from the
	 * datastore.
	 * 
	 * @return <code>true</code> if the object is found in the datastore and
	 *         deleted, <code>false</code> if the item is not found.
	 */
	protected boolean _removeById(Class<?> type, Integer id) {
		if (id != null) {
			String entityName = getMetadataUtil().getEntityName(type);
			
			Query query = em().createQuery("select _it_.id" + entityName + " from " + entityName + " _it_ where _it_.id" + entityName + " = ?1").setParameter(1, id);
			if (query.getResultList().size() != 0) {
				em().getTransaction().begin();
				em().remove(em().getReference(type, id));
				em().getTransaction().commit();
				return true;
			}
		}
		return false;
	}

//	/**
//	 * Remove all the entities of the given type from the datastore that have
//	 * one of these ids.
//	 */
//	protected void _removeByIds(Class<?> type, Integer... ids) {
//		String entityName = getMetadataUtil().getEntityName(type);
//		
//		em().getTransaction().begin();
//		
//		for (Integer id : (List<Integer>) pullByIds("select _it_.id" + entityName, type, ids)) {
//			em().remove(em().getReference(type, id));
//		}
//		
//		em().getTransaction().commit();
//	}

	/**
	 * Remove the specified entity from the datastore.
	 * 
	 * @return <code>true</code> if the object is found in the datastore and
	 *         removed, <code>false</code> if the item is not found.
	 */
	protected boolean _removeEntity(Object entity) {
		if (entity != null) {
//			if (em().contains(entity)) {
//				em().remove(entity);
//				return true;
//			} else {
				Integer id = getMetadataUtil().getId(entity);
				return _removeById(entity.getClass(), id);
//			}
		}
		return false;
	}

//	/**
//	 * Remove the specified entities from the datastore.
//	 */
//	protected void _removeEntities(Object... entities) {
//		for (Object entity : entities) {
//			_removeEntity(entity);
//		}
//	}

	/**
	 * Return the persistent instance of the given entity class with the given
	 * identifier, or null if there is no such persistent instance.
	 */
	protected <T> T _find(Class<T> type, Integer id) {
		return em().find(type, id);
	}

	/**
	 * Return the all the persistent instances of the given entity class with
	 * the given identifiers. An array of entities is returned that matches the
	 * same order of the ids listed in the call. For each entity that is not
	 * found in the datastore, a null will be inserted in its place in the
	 * return array.
	 */
	protected <T> T[] _find(Class<T> type, Integer... ids) {
		Object[] retList = (Object[]) Array.newInstance(type, ids.length);
		for (Object entity : pullByIds("select _it_", type, ids)) {
			Integer id = getMetadataUtil().getId(entity);

			for (int i = 0; i < ids.length; i++) {
				if (id.equals(ids[i])) {
					retList[i] = entity;
					// don't break. the same id could be in the list twice.
				}
			}
		}

		return (T[]) retList;
	}

	protected <T> T _getReference(Class<T> type, Integer id) {
		return em().getReference(type, id);
	}

	protected <T> T[] _getReferences(Class<T> type, Integer... ids) {
		T[] retList = (T[]) Array.newInstance(type, ids.length);
		for (int i = 0; i < ids.length; i++) {
			retList[i] = _getReference(type, ids[i]);
		}
		return retList;
	}

	/**
	 * Get a list of all the entities of the specified class.
	 */
	protected <T> List<T> _all(Class<T> type) {
		return em().createQuery("select _it_ from " + getMetadataUtil().getEntityName(type) + " _it_").getResultList();
	}

	/**
	 * <p>
	 * Copy the state of the given object onto the persistent object with the
	 * same identifier. If there is no persistent instance currently associated
	 * with the session, it will be loaded. Return the persistent instance. If
	 * the given instance is unsaved, save a copy and return it as a newly
	 * persistent instance.
	 * 
	 * <p>
	 * The instance that is passed in does not become associated with the
	 * session. This operation cascades to associated instances if the
	 * association is mapped with cascade="merge".
	 */
	protected <T> T _merge(T entity) {
		em().getTransaction().begin();
		em().merge(entity);
		em().getTransaction().commit();
		return entity;
	}

	/**
	 * <p>
	 * Copy the state of the given objects onto the persistent objects with the
	 * same identifier. If there is no persistent instance currently associated
	 * with the session, it will be loaded. Return the persistent instances. If
	 * a given instance is unsaved, save a copy and return it as a newly
	 * persistent instance.
	 * 
	 * <p>
	 * The instances that are passed in do not become associated with the
	 * session. This operation cascades to associated instances if the
	 * association is mapped with cascade="merge".
	 */
	protected <T> T[] _merge(Class<T> arrayType, T... entities) {
		T[] retList = (T[]) Array.newInstance(arrayType, entities.length);
		for (int i = 0; i < entities.length; i++) {
			retList[i] = _merge(entities[i]);
		}
		return retList;
	}

	/**
	 * If an entity with the same ID already exists in the database, merge the
	 * changes into that entity. If not persist the given entity. In either
	 * case, a managed entity with the changed values is returned. It may or may
	 * not be the same object as was passed in.
	 */
	protected <T> T _persistOrMerge(T entity) {
		if (entity == null)
			return null;
//		if (em().contains(entity))
//			return entity;
		Integer id = getMetadataUtil().getId(entity);
		if (!validId(id)) {
			_persist(entity);
			return entity;
		}
		T prev = em().find((Class<T>) entity.getClass(), id);
		if (prev == null) {
			_persist(entity);
			return entity;
		} else {
			return _merge(entity);
		}
	}

	/**
	 * <p>
	 * For each entity: If an entity with the same ID already exists in the
	 * database, merge the changes into that entity. If not persist the given
	 * entity. In either case, a managed entity with the changed values is
	 * returned. It may or may not be the same object as was passed in.
	 * 
	 * <p>
	 * This version of the method allows the array type to be specified.
	 * 
	 * @return an array containing each managed entity corresponding to the
	 *         entities passed in.
	 */
	protected <T> T[] _persistOrMerge(Class<T> arrayType, T... entities) {
		T[] retList = (T[]) Array.newInstance(arrayType, entities.length);
		for (int i = 0; i < entities.length; i++) {
			retList[i] = _persistOrMerge(entities[i]);
		}
		return retList;
	}

	/**
	 * Search for objects based on the search parameters in the specified
	 * <code>ISearch</code> object.
	 * 
	 * @see ISearch
	 */
	@SuppressWarnings("rawtypes")
	protected List _search(ISearch search) {
		if (search == null)
			throw new NullPointerException("Search is null.");
		if (search.getSearchClass() == null)
			throw new NullPointerException("Search class is null.");

		return getSearchProcessor().search(em(), search);
	}

	/**
	 * Same as <code>_search(ISearch)</code> except that it uses the specified
	 * search class instead of getting it from the search object. Also, if the
	 * search object has a different search class than what is specified, an
	 * exception is thrown.
	 */
	@SuppressWarnings("rawtypes")
	protected List _search(Class<?> searchClass, ISearch search) {
		if (search == null)
			throw new NullPointerException("Search is null.");
		if (searchClass == null)
			throw new NullPointerException("Search class is null.");
		if (search.getSearchClass() != null && !search.getSearchClass().equals(searchClass))
			throw new IllegalArgumentException("Search class does not match expected type: " + searchClass.getName());

		return getSearchProcessor().search(em(), searchClass, search);
	}

	/**
	 * Returns the total number of results that would be returned using the
	 * given <code>ISearch</code> if there were no paging or maxResult limits.
	 * 
	 * @see ISearch
	 */
	protected int _count(ISearch search) {
		if (search == null)
			throw new NullPointerException("Search is null.");
		if (search.getSearchClass() == null)
			throw new NullPointerException("Search class is null.");

		return getSearchProcessor().count(em(), search);
	}

	/**
	 * Same as <code>_count(ISearch)</code> except that it uses the specified
	 * search class instead of getting it from the search object. Also, if the
	 * search object has a different search class than what is specified, an
	 * exception is thrown.
	 */
	protected int _count(Class<?> searchClass, ISearch search) {
		if (search == null)
			throw new NullPointerException("Search is null.");
		if (searchClass == null)
			throw new NullPointerException("Search class is null.");
		if (search.getSearchClass() != null && !search.getSearchClass().equals(searchClass))
			throw new IllegalArgumentException("Search class does not match expected type: " + searchClass.getName());

		return getSearchProcessor().count(em(), searchClass, search);
	}

	/**
	 * Returns the number of instances of this entity in the datastore.
	 */
	protected int _count(Class<?> type) {
		return ((Number) em().createQuery("select count(_it_) from " + getMetadataUtil().getEntityName(type) + " _it_").getSingleResult()).intValue();
	}

//	/**
//	 * Returns a <code>SearchResult</code> object that includes the list of
//	 * results like <code>search()</code> and the total length like
//	 * <code>searchLength</code>.
//	 * 
//	 * @see ISearch
//	 */
//	protected SearchResult _searchAndCount(ISearch search) {
//		if (search == null)
//			throw new NullPointerException("Search is null.");
//		if (search.getSearchClass() == null)
//			throw new NullPointerException("Search class is null.");
//
//		return getSearchProcessor().searchAndCount(em(), search);
//	}
//
//	/**
//	 * Same as <code>_searchAndCount(ISearch)</code> except that it uses the
//	 * specified search class instead of getting it from the search object.
//	 * Also, if the search object has a different search class than what is
//	 * specified, an exception is thrown.
//	 */
//	protected SearchResult _searchAndCount(Class<?> searchClass, ISearch search) {
//		if (search == null)
//			throw new NullPointerException("Search is null.");
//		if (searchClass == null)
//			throw new NullPointerException("Search class is null.");
//		if (search.getSearchClass() != null && !search.getSearchClass().equals(searchClass))
//			throw new IllegalArgumentException("Search class does not match expected type: " + searchClass.getName());
//
//		return getSearchProcessor().searchAndCount(em(), searchClass, search);
//	}

	/**
	 * Search for a single result using the given parameters.
	 * 
	 * @throws NoResultException
	 *             if there is no result
	 * @throws NonUniqueResultException
	 *             if more than one result
	 */
	protected Object _searchUnique(ISearch search) throws NonUniqueResultException, NoResultException {
		if (search == null)
			throw new NullPointerException("Search is null.");
		if (search.getSearchClass() == null)
			throw new NullPointerException("Search class is null.");

		return getSearchProcessor().searchUnique(em(), search);
	}

	/**
	 * Same as <code>_searchUnique(ISearch)</code> except that it uses the
	 * specified search class instead of getting it from the search object.
	 * Also, if the search object has a different search class than what is
	 * specified, an exception is thrown.
	 * 
	 * @throws NoResultException
	 *             if there is no result
	 * @throws NonUniqueResultException
	 *             if more than one result
	 */
	protected Object _searchUnique(Class<?> searchClass, ISearch search) throws NonUniqueResultException,
			NoResultException {
		if (search == null)
			throw new NullPointerException("Search is null.");
		if (searchClass == null)
			throw new NullPointerException("Search class is null.");
		if (search.getSearchClass() != null && !search.getSearchClass().equals(searchClass))
			throw new IllegalArgumentException("Search class does not match expected type: " + searchClass.getName());

		return getSearchProcessor().searchUnique(em(), searchClass, search);
	}

	/**
	 * Returns true if the object is connected to the current hibernate session.
	 */
	protected boolean _contains(Object o) {
		return em().contains(o);
	}

	/**
	 * Flushes changes in the hibernate cache to the datastore.
	 */
	protected void _flush() {
		em().flush();
	}

	/**
	 * Refresh the content of the given entity from the current datastore state.
	 */
	protected void _refresh(Object... entities) {
		for (Object entity : entities) {
			if (entity != null)
				em().refresh(entity);
		}
	}

	protected boolean _exists(Object entity) {
		if (entity == null)
			return false;
		if (em().contains(entity))
			return true;
		return _exists(entity.getClass(), getMetadataUtil().getId(entity));
	}

	protected boolean _exists(Class<?> type, Integer id) {
		if (type == null)
			throw new NullPointerException("Type is null.");
		if (!validId(id))
			return false;

		String entityName = getMetadataUtil().getEntityName(type);
		
		Query query = em().createQuery("select _it_.id" + entityName + " from " + getMetadataUtil().getEntityName(type) + " _it_ where _it_.id" + entityName + " = :id");
		query.setParameter("id", id);
		return query.getResultList().size() == 1;
	}

	protected boolean[] _exists(Class<?> type, Integer... ids) {
		if (type == null)
			throw new NullPointerException("Type is null.");

		boolean[] ret = new boolean[ids.length];

		String entityName = getMetadataUtil().getEntityName(type);
		
		for (Integer id : (List<Integer>) pullByIds("select _it_.id" + entityName, type, ids)) {
			for (int i = 0; i < ids.length; i++) {
				if (id.equals(ids[i])) {
					ret[i] = true;
					// don't break. the same id could be in the list twice.
				}
			}
		}

		return ret;
	}

	private List<?> pullByIds(String select, Class<?> type, Integer[] ids) {
		List<Integer> nonNulls = new LinkedList<Integer>();
		
		String entityName = getMetadataUtil().getEntityName(type);

		StringBuilder sb = new StringBuilder(select);
		sb.append(" from ");
		sb.append(getMetadataUtil().getEntityName(type));
		sb.append(" _it_ where ");
		for (Integer id : ids) {
			if (id != null) {
				if (nonNulls.size() == 0)
					sb.append("_it_.id" + entityName + " = ?1");
				else
					sb.append(" or _it_.id" + entityName + " = ?").append(nonNulls.size() + 1);
				nonNulls.add(id);
			}
		}
		if (nonNulls.size() == 0)
			return new ArrayList<Object>(0);

		Query query = em().createQuery(sb.toString());
		int idx = 1;
		for (Integer id : nonNulls) {
			query.setParameter(idx++, id);
		}
		return query.getResultList();
	}

	private boolean validId(Integer id) {
		if (id == null)
			return false;
		if (id instanceof Number && ((Number) id).equals(0))
			return false;
		return true;
	}
	
	public Query createQuery(String ql) {
		return entityManager.createQuery(ql);
	}
}
