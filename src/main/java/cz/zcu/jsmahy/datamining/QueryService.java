package cz.zcu.jsmahy.datamining;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;

/**
 * TODO
 *
 * @author Jakub Smrha
 * @since 1.0
 */
public class QueryService extends Service<ResultSet> {
	private static final Logger LOGGER = LogManager.getLogger(QueryService.class);
	private final QueryExecution query;

	public QueryService(final QueryExecution query) {
		this.query = Objects.requireNonNull(query);
	}

	@Override
	protected Task<ResultSet> createTask() {
		return new Task<>() {
			@Override
			protected ResultSet call() {
				LOGGER.info("Executing query:\n{}", query.getQuery());
				return query.execSelect();
			}
		};
	}
}
