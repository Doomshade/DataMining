package git.doomshade.datamining.data;

/**
 * The result from a query
 *
 * @author Jakub Šmrha
 * @version 1.0
 */
public final class DBDataResult {

    private final String data;
    private final RDFFormat rdfFormat;

    public DBDataResult(String data, RDFFormat rdfFormat) {
        this.data = data;
        this.rdfFormat = rdfFormat;
    }

    public String getData() {
        return data;
    }

    public RDFFormat getFormat() {
        return rdfFormat;
    }

    @Override
    public String toString() {
        return "DBDataResult{" +
                "data='" + data + '\'' +
                ", rdfFormat=" + rdfFormat +
                '}';
    }
}
