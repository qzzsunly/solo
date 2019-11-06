package cn.sexycode.util.core.exception;

/**
 * Indicates a problem performing the selection/resolution.
 *
 * @author Steve Ebersole
 */
public class StrategySelectionException extends RuntimeException {
	/**
	 * Constructs a StrategySelectionException using the specified message.
	 *
	 * @param message A message explaining the exception condition.
	 */
	public StrategySelectionException(String message) {
		super( message );
	}

	/**
	 * Constructs a StrategySelectionException using the specified message and cause.
	 *
	 * @param message A message explaining the exception condition.
	 * @param cause The underlying cause.
	 */
	public StrategySelectionException(String message, Throwable cause) {
		super( message, cause );
	}
}
