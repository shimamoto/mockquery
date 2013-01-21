package jp.sf.amateras.mockquery;

/**
 * Will be thrown by the <code>verify</code> methods
 * of all test modules.
 */
public class VerifyFailedException extends RuntimeException
{
	private static final long serialVersionUID = 1L;

	public VerifyFailedException()
    {
        super();
    }

    public VerifyFailedException(String message)
    {
        super(message);
    }
}
