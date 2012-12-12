package org.mixare.lib.gui;

public class Object3DException extends Exception{

	private String msg;
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public Object3DException(String err) {
		super();
		msg = err;
	}
	
	public Object3DException() {
		
	}
	
	public String getErr() {
		return this.msg;
	}

}
