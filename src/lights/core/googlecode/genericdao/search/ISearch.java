package lights.core.googlecode.genericdao.search;

import java.util.List;

public interface ISearch {

	public int getFirstResult();

	public int getMaxResults();

	public int getPage();

	public Class<?> getSearchClass();

	public List<Filter> getFilters();

	public boolean isDisjunction();

	public List<Sort> getSorts();

	public List<Field> getFields();
	
	public boolean isDistinct();

	public List<String> getFetches();

//	/**
//	 * Result mode tells the search what form to use for the results. Options
//	 * include <code>RESULT_AUTO</code>, <code>RESULT_ARRAY</code>, <code>
//	 * RESULT_LIST</code>
//	 * , <code>RESULT_MAP</code> and <code>RESULT_SINGLE
//	 * </code>.
//	 * 
//	 * @see #RESULT_AUTO
//	 * @see #RESULT_ARRAY
//	 * @see #RESULT_LIST
//	 * @see #RESULT_MAP
//	 * @see #RESULT_SINGLE
//	 */
//	public int getResultMode();

}
