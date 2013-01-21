package jp.sf.amateras.mockquery.mock;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import jp.sf.amateras.mockquery.NestedApplicationException;
import jp.sf.amateras.mockquery.AbstractResultSetHandler;
import jp.sf.amateras.mockquery.util.SQLUtil;
import jp.sf.amateras.mockquery.util.ArrayUtil;

/**
 * Mock implementation of <code>Statement</code>.
 */
public class MockStatement implements Statement
{
    private AbstractResultSetHandler resultSetHandler;
    private ResultSet[] currentResultSets = null;
    private int[] currentUpdateCounts = null;
    private int currentResultSetIndex = 0;
    private int currentUpdateCountIndex = 0;
    private List batches = new ArrayList();
    private String cursorName = "";
    private int querySeconds = 0;
    private int maxRows = 0;
    private int maxFieldSize = 0;
    private int fetchDirection = ResultSet.FETCH_FORWARD;
    private int fetchSize = 0;
    private int resultSetType = ResultSet.TYPE_FORWARD_ONLY;
    private int resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
    private int resultSetHoldability = ResultSet.HOLD_CURSORS_OVER_COMMIT;
    private MockResultSet lastGeneratedKeys = null;
    private boolean closed = false;
    private boolean poolable = false;
    private Connection connection;
    
    public MockStatement(Connection connection)
    {
        this.connection = connection;
        this.resultSetType = ResultSet.TYPE_FORWARD_ONLY;
        this.resultSetConcurrency = ResultSet.CONCUR_READ_ONLY;
        try
        {
            this.resultSetHoldability = connection.getMetaData().getResultSetHoldability();
        }
        catch(SQLException exc)
        {
            throw new NestedApplicationException(exc);
        }
    }
    
    public MockStatement(Connection connection, int resultSetType, int resultSetConcurrency)
    {
        this.connection = connection;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        try
        {
            this.resultSetHoldability = connection.getMetaData().getResultSetHoldability();
        }
        catch(SQLException exc)
        {
            throw new NestedApplicationException(exc);
        } 
    }
    
    public MockStatement(Connection connection, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
    {
        this.connection = connection;
        this.resultSetType = resultSetType;
        this.resultSetConcurrency = resultSetConcurrency;
        this.resultSetHoldability = resultSetHoldability;
    }
    
    public boolean isClosed()
    {
        return closed;
    }
    
    public void setResultSetHandler(AbstractResultSetHandler resultSetHandler)
    {
        this.resultSetHandler = resultSetHandler;
    }
    
    protected void setResultSets(ResultSet[] resultSets)
    {
        closeCurrentResultSets();
        this.currentUpdateCounts = null;
        this.currentResultSets = resultSets;
        this.currentResultSetIndex = 0;
        this.currentUpdateCountIndex = 0;
    }
    
    protected void setUpdateCounts(int[] updateCounts)
    {
        closeCurrentResultSets();
        this.currentResultSets = null;
        this.currentUpdateCounts = updateCounts;
        this.currentResultSetIndex = 0;
        this.currentUpdateCountIndex = 0;
    }
    
    public String getCursorName()
    {
        return cursorName;
    }
    
    public ResultSet executeQuery(String sql) throws SQLException
    {
        SQLException exception = resultSetHandler.getSQLException(sql);
        if(null != exception)
        {
            throw exception;
        }
        resultSetHandler.addExecutedStatement(sql);
        if(resultSetHandler.hasMultipleResultSets(sql))
        {
            MockResultSet[] results = resultSetHandler.getResultSets(sql);
            if(null != results) return cloneAndSetMultipleResultSets(results);
        }
        else
        {
            MockResultSet result = resultSetHandler.getResultSet(sql);
            if(null != result) return cloneAndSetSingleResultSet(result);
        }
        if(resultSetHandler.hasMultipleGlobalResultSets())
        {
            return cloneAndSetMultipleResultSets(resultSetHandler.getGlobalResultSets());
        }
        return cloneAndSetSingleResultSet(resultSetHandler.getGlobalResultSet());
    }
    
    private MockResultSet cloneAndSetSingleResultSet(MockResultSet result)
    {
        result = cloneResultSet(result);
        if(null != result)
        {
            resultSetHandler.addReturnedResultSet(result);
        }
        setResultSets(new MockResultSet[] {result});
        setLastGeneratedKeysResultSet(null);
        return result;
    }
    
    private MockResultSet cloneAndSetMultipleResultSets(MockResultSet[] results)
    {
        results = cloneResultSets(results);
        if(null != results)
        {
            resultSetHandler.addReturnedResultSets(results);
        }
        setResultSets(results);
        setLastGeneratedKeysResultSet(null);
        if(null != results && results.length > 0)
        {
            return results[0];
        }
        return null;
    }
    
    private void closeCurrentResultSets()
    {
        if(null != currentResultSets)
        {
            for(int ii = 0; ii < currentResultSets.length; ii++)
            {
                try
                {
                    if(null != currentResultSets[ii])
                    {
                        currentResultSets[ii].close();
                    }
                } 
                catch(SQLException exc)
                {
                    throw new NestedApplicationException(exc);
                }
            }
        }
    }

    public int executeUpdate(String sql) throws SQLException
    {
        SQLException exception = resultSetHandler.getSQLException(sql);
        if(null != exception)
        {
            throw exception;
        }
        resultSetHandler.addExecutedStatement(sql);
        if(resultSetHandler.hasMultipleUpdateCounts(sql))
        {
            Integer[] returnValues = resultSetHandler.getUpdateCounts(sql);
            if(null != returnValues)
            {
                return setMultipleUpdateCounts((int[])ArrayUtil.convertToPrimitiveArray(returnValues));
            }
        }
        else
        {
            Integer returnValue = resultSetHandler.getUpdateCount(sql);
            if(null != returnValue)
            {
                return setSingleUpdateCount(returnValue.intValue());
            }
        }
        if(resultSetHandler.hasMultipleGlobalUpdateCounts())
        {
            return setMultipleUpdateCounts(resultSetHandler.getGlobalUpdateCounts());
        }
        return setSingleUpdateCount(resultSetHandler.getGlobalUpdateCount());
    }
    
    private int setSingleUpdateCount(int updateCount)
    {
        setUpdateCounts(new int[] {updateCount});
        setLastGeneratedKeysResultSet(null);
        return updateCount;
    }
    
    private int setMultipleUpdateCounts(int[] updateCounts)
    {
        setUpdateCounts(updateCounts);
        setLastGeneratedKeysResultSet(null);
        if(null != updateCounts && updateCounts.length > 0)
        {
            return updateCounts[0];
        }
        return 0;
    }
    
    public boolean execute(String sql) throws SQLException
    {
        boolean callExecuteQuery = isQuery(sql);
        if(callExecuteQuery)
        {
            executeQuery(sql);
        }
        else
        {
            executeUpdate(sql);
        }
        return callExecuteQuery;
    }
    
    public int[] executeBatch() throws SQLException
    {
        int[] results = new int[batches.size()];
        SQLException exception = null;
        for(int ii = 0; ii < results.length; ii++)
        {
            String nextSQL = (String)batches.get(ii);
            if(isQuery(nextSQL))
            {
                exception = prepareFailedResult(results, ii, "SQL " + batches.get(ii) + " in the list of batches returned a ResultSet.", null);
            }
            else
            {
                try
                {
                    results[ii] = executeUpdate(nextSQL);
                } 
                catch(SQLException exc)
                {
                    exception = prepareFailedResult(results, ii, null, exc);
                }
            }
            if(null != exception && !resultSetHandler.getContinueProcessingOnBatchFailure())
            {
                throw exception;
            }
        }
        if(null != exception)
        {
            throw new BatchUpdateException(exception.getMessage(), exception.getSQLState(), exception.getErrorCode(), results);
        }
        return results;
    }
    
    protected SQLException prepareFailedResult(int[] actualResults, int index, String message, SQLException caughtException)
    {
        actualResults[index] = -3;
        if(caughtException instanceof BatchUpdateException)
        {
            return caughtException;
        }
        else
        {
            int[] partialResults = (int[])ArrayUtil.truncateArray(actualResults, index);
            if(null == caughtException)
            {
                return new BatchUpdateException(message, partialResults);
            }
            else
            {
                return new BatchUpdateException(caughtException.getMessage(), caughtException.getSQLState(), caughtException.getErrorCode(), partialResults);
            }
        }
    }
    
    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException
    {
        int updateCount = executeUpdate(sql);
        setGeneratedKeysResultSet(sql, autoGeneratedKeys);
        return updateCount;
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException
    {
        return executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException
    {
        return executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException
    {
        boolean isQuery = execute(sql);
        setGeneratedKeysResultSet(sql, autoGeneratedKeys);
        return isQuery;
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException
    {
        return execute(sql, Statement.RETURN_GENERATED_KEYS);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException
    {
        return execute(sql, Statement.RETURN_GENERATED_KEYS);
    }
    
    private void setGeneratedKeysResultSet(String sql, int autoGeneratedKeys) throws SQLException
    {
        if(Statement.RETURN_GENERATED_KEYS != autoGeneratedKeys && Statement.NO_GENERATED_KEYS != autoGeneratedKeys)
        {
            throw new SQLException("autoGeneratedKeys must be either Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS");
        }
        if(Statement.RETURN_GENERATED_KEYS == autoGeneratedKeys)
        {
            setLastGeneratedKeysResultSet(determineGeneratedKeysResultSet(sql));
        }
        else
        {
            setLastGeneratedKeysResultSet(null);
        }
    }
    
    protected void setLastGeneratedKeysResultSet(MockResultSet generatedKeys)
    {
        lastGeneratedKeys = generatedKeys;
    }

    protected MockResultSet determineGeneratedKeysResultSet(String sql)
    {
        MockResultSet generatedKeys = resultSetHandler.getGeneratedKeys(sql);
        if(null != generatedKeys) return generatedKeys;
        return resultSetHandler.getGlobalGeneratedKeys();
    }

    public void close() throws SQLException
    {
        closed = true;
    }

    public int getMaxFieldSize() throws SQLException
    {
        return maxFieldSize;
    }

    public void setMaxFieldSize(int maxFieldSize) throws SQLException
    {
        this.maxFieldSize = maxFieldSize;
    }

    public int getMaxRows() throws SQLException
    {
        return maxRows;
    }

    public void setMaxRows(int maxRows) throws SQLException
    {
        this.maxRows = maxRows;
    }

    public void setEscapeProcessing(boolean enable) throws SQLException
    {

    }

    public int getQueryTimeout() throws SQLException
    {
        return querySeconds;
    }

    public void setQueryTimeout(int querySeconds) throws SQLException
    {
        this.querySeconds = querySeconds;
    }

    public void cancel() throws SQLException
    {

    }

    public SQLWarning getWarnings() throws SQLException
    {
        return null;
    }

    public void clearWarnings() throws SQLException
    {

    }

    public void setCursorName(String cursorName) throws SQLException
    {
        this.cursorName = cursorName;
    }

    protected boolean isQuery(String sql)
    {
        boolean isQuery;
        Boolean returnsResultSet = resultSetHandler.getReturnsResultSet(sql);
        if(null != returnsResultSet)
        {
            isQuery = returnsResultSet.booleanValue();
        }
        else
        {
            isQuery = SQLUtil.isSelect(sql);
        }
        return isQuery;
    }

    public ResultSet getResultSet() throws SQLException
    {
        if(null == currentResultSets) return null;
        if(currentResultSetIndex >= currentResultSets.length) return null;
        return currentResultSets[currentResultSetIndex];
    }

    public int getUpdateCount() throws SQLException
    {
        if(null == currentUpdateCounts) return -1;
        if(currentUpdateCountIndex >= currentUpdateCounts.length) return -1;
        return currentUpdateCounts[currentUpdateCountIndex];
    }
    
    public boolean getMoreResults(int current) throws SQLException
    {
        return getMoreResults(current != Statement.KEEP_CURRENT_RESULT);
    }

    public boolean getMoreResults() throws SQLException
    {
        return getMoreResults(true);
    }
    
    private boolean getMoreResults(boolean doCloseCurrentResult) throws SQLException
    {
        if(null != currentResultSets)
        {
            if(currentResultSetIndex < currentResultSets.length)
            {
                if(null != currentResultSets[currentResultSetIndex] && doCloseCurrentResult)
                {
                    currentResultSets[currentResultSetIndex].close();
                }
                currentResultSetIndex++;
            }
            return (currentResultSetIndex < currentResultSets.length);
        }
        else if(null != currentUpdateCounts)
        {
            if(currentUpdateCountIndex < currentUpdateCounts.length)
            {
                currentUpdateCountIndex++;
            }
        }
        return false;
    }

    public void setFetchDirection(int fetchDirection) throws SQLException
    {
        this.fetchDirection = fetchDirection;
    }

    public int getFetchDirection() throws SQLException
    {
        return fetchDirection;
    }

    public void setFetchSize(int fetchSize) throws SQLException
    {   
        this.fetchSize = fetchSize;
    }

    public int getFetchSize() throws SQLException
    {
        return fetchSize;
    }

    public void addBatch(String sql) throws SQLException
    {
        batches.add(sql);
    }

    public void clearBatch() throws SQLException
    {
        batches.clear();
    }

    public Connection getConnection() throws SQLException
    {
        return connection;
    }

    public ResultSet getGeneratedKeys() throws SQLException
    {
        if(null == lastGeneratedKeys)
        {
            MockResultSet resultSet = new MockResultSet("Last statement did not generate any keys");
            resultSet.setStatement(this);
            return resultSet;
        }
        return cloneResultSet(lastGeneratedKeys);
    }
    
    public int getResultSetType() throws SQLException
    {
        return resultSetType;
    }
    
    public int getResultSetConcurrency() throws SQLException
    {
        return resultSetConcurrency;
    }
    
    public int getResultSetHoldability() throws SQLException
    {
        return resultSetHoldability;
    }
    
    public boolean isPoolable() throws SQLException
    {
        return poolable;
    }

    public void setPoolable(boolean poolable) throws SQLException
    {
        this.poolable = poolable;
    }

    public boolean isWrapperFor(Class iface) throws SQLException
    {
        return false;
    }

    public Object unwrap(Class iface) throws SQLException
    {
        throw new SQLException("No object found for " + iface);
    }
    
    protected MockResultSet cloneResultSet(MockResultSet resultSet)
    {
        if(null == resultSet) return null;
        MockResultSet clone = (MockResultSet)resultSet.clone();
        clone.setStatement(this);
        return clone;
    }
    
    protected MockResultSet[] cloneResultSets(MockResultSet[] resultSets)
    {
        if(null == resultSets) return null;
        MockResultSet[] clonedResultsSets = new MockResultSet[resultSets.length];
        for(int ii = 0; ii < resultSets.length; ii++)
        {
            if(null != resultSets[ii])
            {
                clonedResultsSets[ii] = (MockResultSet)resultSets[ii].clone();
                clonedResultsSets[ii].setStatement(this);
            }
        }
        return clonedResultsSets;
    }
}
