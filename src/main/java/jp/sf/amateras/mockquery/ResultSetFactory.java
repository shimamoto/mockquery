package jp.sf.amateras.mockquery;

import jp.sf.amateras.mockquery.mock.MockResultSet;

/**
 * Interface for <code>ResultSet</code> factories.
 */
public interface ResultSetFactory
{
    public MockResultSet create(String id);
}
