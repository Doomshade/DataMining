package cz.zcu.jsmahy.datamining.query;

/**
 * A request
 *
 * @author Jakub Šmrha
 * @version 28.03.2022
 * @since 28.03.2022
 */
public interface ISparqlRequest {
	/**
	 * Adds a restriction to the request
	 *
	 * @param restriction the restriction
	 */
	void addRestriction(Restriction restriction);

	/**
	 * Adds restrictions to the request
	 *
	 * @param restrictions the restrictions
	 */
	void addRestrictions(Iterable<? extends Restriction> restrictions);

	/**
	 * @return
	 */
	Iterable<Restriction> getRestrictions();

	String getRequestPage();

	String getNamespace();

	String getLink();
}
