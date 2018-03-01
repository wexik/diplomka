/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package exceptions;

/**
 *	Exception thrown when the processing email has malformed information and the object instance cannot be created from the data provided.
 * @author Matej Briškár
 */
public class MalformedMessageException extends Exception {


	private static final long serialVersionUID = 1L;

	public MalformedMessageException() {
    }
    
}
