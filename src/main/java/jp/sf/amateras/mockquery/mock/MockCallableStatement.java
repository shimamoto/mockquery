package jp.sf.amateras.mockquery.mock;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jp.sf.amateras.mockquery.NestedApplicationException;
import jp.sf.amateras.mockquery.AbstractOutParameterResultSetHandler;
import jp.sf.amateras.mockquery.util.StreamUtil;

/**
 * Mock implementation of <code>CallableStatement</code>.
 */
public class MockCallableStatement extends MockPreparedStatement implements CallableStatement
{
    private AbstractOutParameterResultSetHandler resultSetHandler;
    private Map paramObjects = new HashMap();
    private Set registeredOutParameterSetIndexed = new HashSet();
    private Set registeredOutParameterSetNamed = new HashSet();
    private List batchParameters = new ArrayList();
    private Map lastOutParameters = null;
    private boolean wasNull = false;
    
    public MockCallableStatement(Connection connection, String sql)
    {
        super(connection, sql);
    }

    public MockCallableStatement(Connection connection, String sql, int resultSetType, int resultSetConcurrency)
    {
        super(connection, sql, resultSetType, resultSetConcurrency);
    }

    public MockCallableStatement(Connection connection, String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability)
    {
        super(connection, sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }
    
    public void setCallableStatementResultSetHandler(AbstractOutParameterResultSetHandler resultSetHandler)
    {
        super.setPreparedStatementResultSetHandler(resultSetHandler);
        this.resultSetHandler = resultSetHandler;
    }
    
    public Map getNamedParameterMap()
    {
        return Collections.unmodifiableMap(paramObjects);
    }
    
    public Map getParameterMap()
    {
        Map parameterMap = new HashMap(getIndexedParameterMap());
        parameterMap.putAll(getNamedParameterMap());
        return Collections.unmodifiableMap(parameterMap);
    }
    
    public Object getParameter(String name)
    {
        return paramObjects.get(name);
    }
    
    public void clearParameters() throws SQLException
    {
        super.clearParameters();
        paramObjects.clear();
    }
    
    public Set getNamedRegisteredOutParameterSet()
    {
        return Collections.unmodifiableSet(registeredOutParameterSetNamed);
    }
    
    public boolean isOutParameterRegistered(int index)
    {
        return registeredOutParameterSetIndexed.contains(new Integer(index));
    }
    
    public Set getIndexedRegisteredOutParameterSet()
    {
        return Collections.unmodifiableSet(registeredOutParameterSetIndexed);
    }
    
    public boolean isOutParameterRegistered(String parameterName)
    {
        return registeredOutParameterSetNamed.contains(parameterName);
    }
    
    public void clearRegisteredOutParameter()
    {
        registeredOutParameterSetIndexed.clear();
        registeredOutParameterSetNamed.clear();
    }
    
    public ResultSet executeQuery() throws SQLException
    {
        ResultSet resultSet = executeQuery(getParameterMap());
        lastOutParameters = getOutParameterMap();
        return resultSet;
    }
    
    public int executeUpdate() throws SQLException
    {
        int updateCount = executeUpdate(getParameterMap());
        lastOutParameters = getOutParameterMap();
        return updateCount;
    }
    
    public void addBatch() throws SQLException
    {
        batchParameters.add(new HashMap(getParameterMap()));
    }

    public int[] executeBatch() throws SQLException
    {
        return executeBatch(batchParameters);
    }
    
    public void registerOutParameter(int parameterIndex, int sqlType) throws SQLException
    {
        registeredOutParameterSetIndexed.add(new Integer(parameterIndex));
    }

    public void registerOutParameter(int parameterIndex, int sqlType, int scale) throws SQLException
    {
        registerOutParameter(parameterIndex, sqlType);
    }

    public void registerOutParameter(int parameterIndex, int sqlType, String typeName) throws SQLException
    {
        registerOutParameter(parameterIndex, sqlType);
    }
    
    public void registerOutParameter(String parameterName, int sqlType) throws SQLException
    {
        registeredOutParameterSetNamed.add(parameterName);
    }
    
    public void registerOutParameter(String parameterName, int sqlType, int scale) throws SQLException
    {
        registerOutParameter(parameterName, sqlType);
    }
    
    public void registerOutParameter(String parameterName, int sqlType, String typeName) throws SQLException
    {
        registerOutParameter(parameterName, sqlType);
    }
        
    public boolean wasNull() throws SQLException
    {
        return wasNull;
    }
    
    public Object getObject(int parameterIndex) throws SQLException
    {
        wasNull = false;
        Object returnValue = null;
        if(null != lastOutParameters)
        {
            returnValue = lastOutParameters.get(new Integer(parameterIndex));
        }
        if(null == returnValue) wasNull = true;
        return returnValue;
    }
    
    public Object getObject(int parameterIndex, Map map) throws SQLException
    {
        return getObject(parameterIndex);
    }

    public byte getByte(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).byteValue();
            return new Byte(value.toString()).byteValue();
        }
        return 0;
    }

    public double getDouble(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).doubleValue();
            return new Double(value.toString()).doubleValue();
        }
        return 0;
    }

    public float getFloat(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).floatValue();
            return new Float(value.toString()).floatValue();
        }
        return 0;
    }

    public int getInt(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).intValue();
            return new Integer(value.toString()).intValue();
        }
        return 0;
    }

    public long getLong(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).longValue();
            return new Long(value.toString()).longValue();
        }
        return 0;
    }

    public short getShort(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).shortValue();
            return new Short(value.toString()).shortValue();
        }
        return 0;
    }

    public boolean getBoolean(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Boolean) return ((Boolean)value).booleanValue();
            return new Boolean(value.toString()).booleanValue();
        }
        return false;
    }

    public byte[] getBytes(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof byte[]) return (byte[])value;
            try
            {
                return value.toString().getBytes("ISO-8859-1");
            } 
            catch(UnsupportedEncodingException exc)
            {
                throw new NestedApplicationException(exc);
            }
        }
        return null;
    }

    public String getString(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value) return value.toString();
        return null;
    }
    
    public String getNString(int parameterIndex) throws SQLException
    {
        return getString(parameterIndex);
    }

    public BigDecimal getBigDecimal(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Number) return new BigDecimal(((Number)value).doubleValue());
            return new BigDecimal(value.toString());
        }
        return null;
    }

    public BigDecimal getBigDecimal(int parameterIndex, int scale) throws SQLException
    {
        return getBigDecimal(parameterIndex);
    }

    public URL getURL(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof URL) return (URL)value;
            try
            {
                return new URL(value.toString());
            }
            catch(MalformedURLException exc)
            {
            
            }
        }
        return null;
    }

    public Array getArray(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Array) return (Array)value;
            return new MockArray(value);
        }
        return null;
    }

    public Blob getBlob(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Blob) return (Blob)value;
            return new MockBlob(getBytes(parameterIndex));
        }
        return null;
    }

    public Clob getClob(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Clob) return (Clob)value;
            return new MockClob(getString(parameterIndex));
        }
        return null;
    }

    public NClob getNClob(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof NClob) return (NClob)value;
            if(value instanceof Clob) return getNClobFromClob((Clob)value);
            return new MockNClob(getString(parameterIndex));
        }
        return null;
    }

    public SQLXML getSQLXML(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof SQLXML) return (SQLXML)value;
            return new MockSQLXML(getString(parameterIndex));
        }
        return null;
    }

    public Reader getCharacterStream(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Reader) return (Reader)value;
            return new StringReader(getString(parameterIndex));
        }
        return null;
    }

    public Reader getNCharacterStream(int parameterIndex) throws SQLException
    {
        return getCharacterStream(parameterIndex);
    }

    public Date getDate(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Date) return (Date)value;
            return Date.valueOf(value.toString());
        }
        return null;
    }
    
    public Date getDate(int parameterIndex, Calendar calendar) throws SQLException
    {
        return getDate(parameterIndex);
    }

    public Ref getRef(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Ref) return (Ref)value;
            return new MockRef(value);
        }
        return null;
    }

    public Time getTime(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Time) return (Time)value;
            return Time.valueOf(value.toString());
        }
        return null;
    }
    
    public Time getTime(int parameterIndex, Calendar calendar) throws SQLException
    {
        return getTime(parameterIndex);
    }

    public Timestamp getTimestamp(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof Timestamp) return (Timestamp)value;
            return Timestamp.valueOf(value.toString());
        }
        return null;
    }
    
    public Timestamp getTimestamp(int parameterIndex, Calendar calendar) throws SQLException
    {
        return getTimestamp(parameterIndex);
    }
    
    public RowId getRowId(int parameterIndex) throws SQLException
    {
        Object value = getObject(parameterIndex);
        if(null != value)
        {
            if(value instanceof RowId) return (RowId)value;
            return new MockRowId(getBytes(parameterIndex));
        }
        return null;
    }
    
    public Object getObject(String parameterName) throws SQLException
    {
        wasNull = false;
        Object returnValue = null;
        if(null != lastOutParameters)
        {
            returnValue = lastOutParameters.get(parameterName);
        }
        if(null == returnValue) wasNull = true;
        return returnValue;
    }
    
    public Object getObject(String parameterName, Map map) throws SQLException
    {
        return getObject(parameterName);
    }
    
    public byte getByte(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).byteValue();
            return new Byte(value.toString()).byteValue();
        }
        return 0;
    }

    public double getDouble(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).doubleValue();
            return new Double(value.toString()).doubleValue();
        }
        return 0;
    }

    public float getFloat(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).floatValue();
            return new Float(value.toString()).floatValue();
        }
        return 0;
    }

    public int getInt(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).intValue();
            return new Integer(value.toString()).intValue();
        }
        return 0;
    }

    public long getLong(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).longValue();
            return new Long(value.toString()).longValue();
        }
        return 0;
    }

    public short getShort(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Number) return ((Number)value).shortValue();
            return new Short(value.toString()).shortValue();
        }
        return 0;
    }

    public boolean getBoolean(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Boolean) return ((Boolean)value).booleanValue();
            return new Boolean(value.toString()).booleanValue();
        }
        return false;
    }

    public byte[] getBytes(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof byte[]) return (byte[])value;
            try
            {
                return value.toString().getBytes("ISO-8859-1");
            } 
            catch(UnsupportedEncodingException exc)
            {
                throw new NestedApplicationException(exc);
            }
        }
        return null;
    }
    
    public String getString(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value) return value.toString();
        return null;
    }

    public String getNString(String parameterName) throws SQLException
    {
        return getString(parameterName);
    }

    public BigDecimal getBigDecimal(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Number) return new BigDecimal(((Number)value).doubleValue());
            return new BigDecimal(value.toString());
        }
        return null;
    }
    
    public URL getURL(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof URL) return (URL)value;
            try
            {
                return new URL(value.toString());
            }
            catch(MalformedURLException exc)
            {
            
            }
        }
        return null;
    }
    
    public Array getArray(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Array) return (Array)value;
            return new MockArray(value);
        }
        return null;
    }

    public Blob getBlob(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Blob) return (Blob)value;
            return new MockBlob(getBytes(parameterName));
        }
        return null;
    }

    public Clob getClob(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Clob) return (Clob)value;
            return new MockClob(getString(parameterName));
        }
        return null;
    }

    public NClob getNClob(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof NClob) return (NClob)value;
            if(value instanceof Clob) return getNClobFromClob((Clob)value);
            return new MockNClob(getString(parameterName));
        }
        return null;
    }

    public SQLXML getSQLXML(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof SQLXML) return (SQLXML)value;
            return new MockSQLXML(getString(parameterName));
        }
        return null;
    }

    public Reader getCharacterStream(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Reader) return (Reader)value;
            return new StringReader(getString(parameterName));
        }
        return null;
    }

    public Reader getNCharacterStream(String parameterName) throws SQLException
    {
        return getCharacterStream(parameterName);
    }

    public Date getDate(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Date) return (Date)value;
            return Date.valueOf(value.toString());
        }
        return null;
    }
    
    public Date getDate(String parameterName, Calendar calendar) throws SQLException
    {
        return getDate(parameterName);
    }
    
    public Ref getRef(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Ref) return (Ref)value;
            return new MockRef(value);
        }
        return null;
    }

    public Time getTime(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Time) return (Time)value;
            return Time.valueOf(value.toString());
        }
        return null;
    }

    public Time getTime(String parameterName, Calendar calendar) throws SQLException
    {
        return getTime(parameterName);
    }
    
    public Timestamp getTimestamp(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof Timestamp) return (Timestamp)value;
            return Timestamp.valueOf(value.toString());
        }
        return null;
    }

    public Timestamp getTimestamp(String parameterName, Calendar calendar) throws SQLException
    {
        return getTimestamp(parameterName);
    }
    
    public RowId getRowId(String parameterName) throws SQLException
    {
        Object value = getObject(parameterName);
        if(null != value)
        {
            if(value instanceof RowId) return (RowId)value;
            return new MockRowId(getBytes(parameterName));
        }
        return null;
    }

    public void setByte(String parameterName, byte byteValue) throws SQLException
    {
        setObject(parameterName, new Byte(byteValue));
    }

    public void setDouble(String parameterName, double doubleValue) throws SQLException
    {
        setObject(parameterName, new Double(doubleValue));
    }

    public void setFloat(String parameterName, float floatValue) throws SQLException
    {
        setObject(parameterName, new Float(floatValue));
    }

    public void setInt(String parameterName, int intValue) throws SQLException
    {
        setObject(parameterName, new Integer(intValue));
    }

    public void setNull(String parameterName, int sqlType) throws SQLException
    {
        setObject(parameterName, null);
    }
    
    public void setNull(String parameterName, int sqlType, String typeName) throws SQLException
    {
        setNull(parameterName, sqlType);
    }

    public void setLong(String parameterName, long longValue) throws SQLException
    {
        setObject(parameterName, new Long(longValue));
    }

    public void setShort(String parameterName, short shortValue) throws SQLException
    {
        setObject(parameterName, new Short(shortValue));
    }

    public void setBoolean(String parameterName, boolean booleanValue) throws SQLException
    {
        setObject(parameterName, new Boolean(booleanValue));
    }

    public void setBytes(String parameterName, byte[] byteArray) throws SQLException
    {
        setObject(parameterName, byteArray);
    }
    
    public void setAsciiStream(String parameterName, InputStream stream) throws SQLException
    {
        setBinaryStream(parameterName, stream);
    }

    public void setAsciiStream(String parameterName, InputStream stream, int length) throws SQLException
    {
        setBinaryStream(parameterName, stream, length);
    }
    
    public void setAsciiStream(String parameterName, InputStream stream, long length) throws SQLException
    {
        setBinaryStream(parameterName, stream, length);
    }

    public void setBinaryStream(String parameterName, InputStream stream) throws SQLException
    {
        byte[] data = StreamUtil.getStreamAsByteArray(stream);
        setObject(parameterName, new ByteArrayInputStream(data));
    }

    public void setBinaryStream(String parameterName, InputStream stream, int length) throws SQLException
    {
        byte[] data = StreamUtil.getStreamAsByteArray(stream, length);
        setObject(parameterName, new ByteArrayInputStream(data));
    }

    public void setBinaryStream(String parameterName, InputStream stream, long length) throws SQLException
    {
        setBinaryStream(parameterName, stream, (int)length);
    }
    
    public void setCharacterStream(String parameterName, Reader reader) throws SQLException
    {
        String data = StreamUtil.getReaderAsString(reader);
        setObject(parameterName, new StringReader(data));
    }

    public void setCharacterStream(String parameterName, Reader reader, int length) throws SQLException
    {
        String data = StreamUtil.getReaderAsString(reader, length);
        setObject(parameterName, new StringReader(data));
    }

    public void setCharacterStream(String parameterName, Reader reader, long length) throws SQLException
    {
        setCharacterStream(parameterName, reader, (int)length);
    }
    
    public void setNCharacterStream(String parameterName, Reader reader) throws SQLException
    {
        setCharacterStream(parameterName, reader);
    }

    public void setNCharacterStream(String parameterName, Reader reader, long length) throws SQLException
    {
        setCharacterStream(parameterName, reader, length);
    }

    public void setBlob(String parameterName, Blob blob) throws SQLException
    {
        setObject(parameterName, blob);
    }
    
    public void setBlob(String parameterName, InputStream inputStream) throws SQLException
    {
        byte[] data = StreamUtil.getStreamAsByteArray(inputStream);
        setBlob(parameterName, new MockBlob(data));
    }
    
    public void setBlob(String parameterName, InputStream inputStream, long length) throws SQLException
    {
        byte[] data = StreamUtil.getStreamAsByteArray(inputStream, (int)length);
        setBlob(parameterName, new MockBlob(data));
    }

    public void setClob(String parameterName, Clob clob) throws SQLException
    {
        setObject(parameterName, clob);
    }
    
    public void setClob(String parameterName, Reader reader) throws SQLException
    {
        String data = StreamUtil.getReaderAsString(reader);
        setClob(parameterName, new MockClob(data));
    }
    
    public void setClob(String parameterName, Reader reader, long length) throws SQLException
    {
        String data = StreamUtil.getReaderAsString(reader, (int)length);
        setClob(parameterName, new MockClob(data));
    }

    public void setNClob(String parameterName, NClob nClob) throws SQLException
    {
        setObject(parameterName, nClob);
    }

    public void setNClob(String parameterName, Reader reader) throws SQLException
    {
        String data = StreamUtil.getReaderAsString(reader);
        setNClob(parameterName, new MockNClob(data));
    }
    
    public void setNClob(String parameterName, Reader reader, long length) throws SQLException
    {
        String data = StreamUtil.getReaderAsString(reader, (int)length);
        setNClob(parameterName, new MockNClob(data));
    }

    public void setSQLXML(String parameterName, SQLXML xmlObject) throws SQLException
    {
        setObject(parameterName, xmlObject);
    }

    public void setString(String parameterName, String string) throws SQLException
    {
        setObject(parameterName, string);
    }

    public void setNString(String parameterName, String string) throws SQLException
    {
        setObject(parameterName, string);
    }

    public void setBigDecimal(String parameterName, BigDecimal bigDecimal) throws SQLException
    {
        setObject(parameterName, bigDecimal);
    }

    public void setURL(String parameterName, URL url) throws SQLException
    {
        setObject(parameterName, url);
    }

    public void setDate(String parameterName, Date date) throws SQLException
    {
        setObject(parameterName, date);
    }

    public void setTime(String parameterName, Time time) throws SQLException
    {
        setObject(parameterName, time);
    }
    
    public void setTimestamp(String parameterName, Timestamp timestamp) throws SQLException
    {
        setObject(parameterName, timestamp);
    }

    public void setDate(String parameterName, Date date, Calendar calendar) throws SQLException
    {
        setDate(parameterName, date);
    }

    public void setTime(String parameterName, Time time, Calendar calendar) throws SQLException
    {
        setTime(parameterName, time);
    }

    public void setTimestamp(String parameterName, Timestamp timestamp, Calendar calendar) throws SQLException
    {
        setTimestamp(parameterName, timestamp);
    }
    
    public void setRowId(String parameterName, RowId rowId) throws SQLException
    {
        setObject(parameterName, rowId);
    }

    public void setObject(String parameterName, Object object) throws SQLException
    {
        paramObjects.put(parameterName, object);
    }

    public void setObject(String parameterName, Object object, int targetSqlType) throws SQLException
    {
        setObject(parameterName, object);
    }
    
    public void setObject(String parameterName, Object object, int targetSqlType, int scale) throws SQLException
    {
        setObject(parameterName, object);
    }
    
    private Map getOutParameterMap()
    {
        Map outParameter = resultSetHandler.getOutParameter(getSQL(), getParameterMap());
        if(null == outParameter)
        {
            outParameter = resultSetHandler.getOutParameter(getSQL());
        }
        if(null == outParameter)
        {
            outParameter = resultSetHandler.getGlobalOutParameter();
        }
        if(resultSetHandler.getMustRegisterOutParameters())
        {
            return filterNotRegisteredParameters(outParameter);
        }
        return outParameter;
    }
    
    private Map filterNotRegisteredParameters(Map outParameter)
    {
        Map filteredMap = new HashMap();
        Iterator keys = outParameter.keySet().iterator();
        while(keys.hasNext())
        {
            Object nextKey = keys.next();
            if(registeredOutParameterSetIndexed.contains(nextKey) || registeredOutParameterSetNamed.contains(nextKey))
            {
                filteredMap.put(nextKey, outParameter.get(nextKey));
            }
        }
        return Collections.unmodifiableMap(filteredMap);
    }
    
    private NClob getNClobFromClob(Clob clobValue) throws SQLException
    {
        return new MockNClob(clobValue.getSubString(1, (int)clobValue.length()));
    }
}
