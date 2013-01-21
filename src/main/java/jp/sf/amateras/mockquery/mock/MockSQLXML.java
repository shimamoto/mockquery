package jp.sf.amateras.mockquery.mock;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.sql.SQLException;
import java.sql.SQLXML;

import javax.xml.transform.Result;
import javax.xml.transform.Source;

/**
 * Mock implementation of <code>MockSQLXML</code>.
 * Uses JDOM for XML handling.
 */
public class MockSQLXML implements SQLXML, Cloneable
{
	public MockSQLXML()
    {
		// TODO 
    }

	public MockSQLXML(String stringContent)
    {
		// TODO 
    }

	public void free() throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public InputStream getBinaryStream() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public OutputStream setBinaryStream() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Reader getCharacterStream() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Writer setCharacterStream() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public String getString() throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public void setString(String value) throws SQLException {
		// TODO Auto-generated method stub
		
	}

	public Source getSource(Class sourceClass) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

	public Result setResult(Class resultClass) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
