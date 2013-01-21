package jp.sf.amateras.mockquery.mock;

import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.NClob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Savepoint;
import java.sql.Statement;
import java.sql.Struct;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import jp.sf.amateras.mockquery.NestedApplicationException;
import jp.sf.amateras.mockquery.CallableStatementResultSetHandler;
import jp.sf.amateras.mockquery.PreparedStatementResultSetHandler;
import jp.sf.amateras.mockquery.StatementResultSetHandler;

/**
 * Mock implementation of <code>Connection</code>.
 */
public class MockConnection implements Connection
{
    private StatementResultSetHandler statementHandler;
    private PreparedStatementResultSetHandler preparedStatementHandler;
    private CallableStatementResultSetHandler callableStatementHandler;
    private DatabaseMetaData metaData;
    private Map savepoints;
    private int savepointCount;
    private boolean closed;
    private boolean autoCommit;
    private boolean readOnly;
    private int holdability;
    private int level;
    private Map typeMap;
    private String catalog ;
    private int numberCommits;
    private int numberRollbacks;
    private Properties clientInfo;
    
    public MockConnection()
    {
        statementHandler = new StatementResultSetHandler();
        preparedStatementHandler = new PreparedStatementResultSetHandler();
        callableStatementHandler = new CallableStatementResultSetHandler();
        metaData = new MockDatabaseMetaData();
        ((MockDatabaseMetaData)metaData).setConnection(this);
        closed = false;
        autoCommit = false;
        readOnly = false;
        holdability = ResultSet.HOLD_CURSORS_OVER_COMMIT;
        try
        {  
            level = metaData.getDefaultTransactionIsolation();
        }
        catch(SQLException exc)
        {
            throw new NestedApplicationException(exc);
        }
        typeMap = new HashMap();
        savepoints = new HashMap();
        savepointCount = 0;
        catalog = null;
        numberCommits = 0;
        numberRollbacks = 0;
        clientInfo = new Properties();
    }
    
    public void setMetaData(DatabaseMetaData metaData) throws SQLException
    {
        if(metaData != null && metaData instanceof MockDatabaseMetaData)
        {
            ((MockDatabaseMetaData)metaData).setConnection(this);
        }
        this.metaData = metaData;
    }
    
    public int getNumberCommits()
    {
        return numberCommits;
    }
    
    public int getNumberRollbacks()
    {
        return numberRollbacks;
    }
    
    public Map getSavepointMap()
    {
        return Collections.unmodifiableMap(savepoints);
    }
    
    public void resetNumberCommits()
    {
        numberCommits = 0;
    }
    
    public void resetNumberRollbacks()
    {
        numberRollbacks = 0;
    }
    
    public void resetSavepointMap()
    {
        savepoints.clear();
    }
    
    public StatementResultSetHandler getStatementResultSetHandler()
    {
        return statementHandler;
    }
    
    public PreparedStatementResultSetHandler getPreparedStatementResultSetHandler()
    {
        return preparedStatementHandler;
    }
    
    public CallableStatementResultSetHandler getCallableStatementResultSetHandler()
    {
        return callableStatementHandler;
    }
    
    public Statement createStatement() throws SQLException
    {
        MockStatement statement = new MockStatement(this);
        getStatementResultSetHandler().addStatement(statement);
        return statement;
    }
    
    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException
    {
        MockStatement statement = new MockStatement(this, resultSetType, resultSetConcurrency);
        getStatementResultSetHandler().addStatement(statement);
        return statement;
    }
    
    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        MockStatement statement = new MockStatement(this, resultSetType, resultSetConcurrency, resultSetHoldability);
        getStatementResultSetHandler().addStatement(statement);
        return statement;
    }
    
    public CallableStatement prepareCall(String sql) throws SQLException
    {
        MockCallableStatement statement = new MockCallableStatement(this, sql);
        getCallableStatementResultSetHandler().addCallableStatement(statement);
        return statement;
    }
    
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        MockCallableStatement statement = new MockCallableStatement(this, sql, resultSetType, resultSetConcurrency);
        getCallableStatementResultSetHandler().addCallableStatement(statement);
        return statement;
    }
    
    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        MockCallableStatement statement = new MockCallableStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        getCallableStatementResultSetHandler().addCallableStatement(statement);
        return statement;
    }
    
    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        MockPreparedStatement statement = new MockPreparedStatement(this, sql);
        getPreparedStatementResultSetHandler().addPreparedStatement(statement);
        return statement;
    }
    
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException
    {
        MockPreparedStatement statement = new MockPreparedStatement(this, sql, resultSetType, resultSetConcurrency);
        getPreparedStatementResultSetHandler().addPreparedStatement(statement);
        return statement;
    }
    
    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException
    {
        MockPreparedStatement statement = new MockPreparedStatement(this, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
        getPreparedStatementResultSetHandler().addPreparedStatement(statement);
        return statement;
    }
    
    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException
    {
        verifyAutoGeneratedKeysParameter(autoGeneratedKeys);
        MockPreparedStatement statement = new MockPreparedStatement(this, sql, autoGeneratedKeys == Statement.RETURN_GENERATED_KEYS);;
        getPreparedStatementResultSetHandler().addPreparedStatement(statement);
        return statement;
    }
    
    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
        return prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }
    
    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
        return prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
    }
    
    public Blob createBlob() throws SQLException
    {
        return new MockBlob(new byte[0]);
    }

    public Clob createClob() throws SQLException
    {
        return new MockClob("");
    }

    public NClob createNClob() throws SQLException
    {
        return new MockNClob("");
    }

    public SQLXML createSQLXML() throws SQLException
    {
        return new MockSQLXML();
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException
    {
        MockArray array = new MockArray(elements);
        array.setBaseTypeName(typeName);
        return array;
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException
    {
        return new MockStruct(typeName, attributes);
    }

    private void verifyAutoGeneratedKeysParameter(int autoGeneratedKeys) throws SQLException
    {
        if(Statement.RETURN_GENERATED_KEYS != autoGeneratedKeys && Statement.NO_GENERATED_KEYS != autoGeneratedKeys)
        {
            throw new SQLException("autoGeneratedKeys must be either Statement.RETURN_GENERATED_KEYS or Statement.NO_GENERATED_KEYS");
        }
    }
    
    public void close() throws SQLException
    {
        closed = true;
    }
    
    public boolean getAutoCommit() throws SQLException
    {
        return autoCommit;
    }
    
    public String getCatalog() throws SQLException
    {
        return catalog;
    }
    
    public int getHoldability() throws SQLException
    {
        return holdability;
    }
    
    public DatabaseMetaData getMetaData() throws SQLException
    {
        return metaData;
    }
    
    public int getTransactionIsolation() throws SQLException
    {
        return level;
    }
    
    public Map getTypeMap() throws SQLException
    {
        return typeMap;
    }
    
    public SQLWarning getWarnings() throws SQLException
    {
        return null;
    }
    
    public Properties getClientInfo() throws SQLException
    {
        Properties properties = new Properties();
        properties.putAll(clientInfo);
        return properties;
    }

    public String getClientInfo(String name) throws SQLException
    {
        return clientInfo.getProperty(name);
    }

    public boolean isClosed() throws SQLException
    {
        return closed;
    }
    
    public boolean isReadOnly() throws SQLException
    {
        return readOnly;
    }
    
    public boolean isValid(int timeout) throws SQLException
    {
        return !isClosed();
    }

    public String nativeSQL(String sql) throws SQLException
    {
        return sql;
    }
    
    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
        this.autoCommit = autoCommit;
    }
    
    public void setCatalog(String catalog) throws SQLException
    {
        this.catalog = catalog;
    }
    
    public void setHoldability(int holdability) throws SQLException
    {
        this.holdability = holdability;
    }
    
    public void setReadOnly(boolean readOnly) throws SQLException
    {
        this.readOnly = readOnly;
    }
    
    public Savepoint setSavepoint() throws SQLException
    {
        return setSavepoint("");
    }
    
    public Savepoint setSavepoint(String name) throws SQLException
    {
        MockSavepoint savePoint = new MockSavepoint(name, savepointCount);
        savepoints.put(new Integer(savePoint.getSavepointId()), savePoint);
        savepointCount++;
        return savePoint;
    }
    
    public void setTransactionIsolation(int level) throws SQLException
    {
        this.level = level;
    }
    
    public void setTypeMap(Map typeMap) throws SQLException
    {
        this.typeMap = typeMap;
    }
    
    public void setClientInfo(Properties properties)
    {
        clientInfo.clear();
        clientInfo.putAll(properties);
    }

    public void setClientInfo(String name, String value)
    {
        clientInfo.setProperty(name, value);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        MockSavepoint currentSavepoint = (MockSavepoint)savepoints.get(new Integer(savepoint.getSavepointId()));
        if(currentSavepoint.isReleased())
        {
            throw new SQLException("Savepoint with id " + currentSavepoint.getSavepointId() + " and name " 
                    + currentSavepoint.getSavepointName() + " is released");
        }
        currentSavepoint.setReleased(true);
    }
    
    public void commit() throws SQLException
    {
        numberCommits++;
    }
    
    public void rollback() throws SQLException
    {
        numberRollbacks++;
    }
    
    public void rollback(Savepoint savepoint) throws SQLException
    {
        MockSavepoint currentSavepoint = (MockSavepoint)savepoints.get(new Integer(savepoint.getSavepointId()));
        if(currentSavepoint.isReleased())
        {
            throw new SQLException("Savepoint with id " + currentSavepoint.getSavepointId() + " and name " + currentSavepoint.getSavepointName() + " is released");
        }
        currentSavepoint.setRolledBack(true);
        numberRollbacks++;
    }
    
    public void clearWarnings() throws SQLException
    {
        
    }
    
    public boolean isWrapperFor(Class iface) throws SQLException
    {
        return false;
    }

    public Object unwrap(Class iface) throws SQLException
    {
        throw new SQLException("No object found for " + iface);
    }
}
