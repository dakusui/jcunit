package com.github.dakusui.lisj;


public class FormResult {
	private int nextPosition;
	private Object value;
	private int numPositions;
	
	public FormResult(int nextPosition, int numPositions, Object value) {
		this.nextPosition = nextPosition;
		this.numPositions = numPositions;
		this.value = value;
	}
	
	public int nextPosition() {
		return nextPosition;
	}
	
	public void nextPosition(int nextPosition) {
		this.nextPosition = nextPosition;
	}
	
	public Object value() {
		return value;
	}
	
	public void value(Object value) {
		this.value = value;
	}

	public void incrementPosition() {
		this.nextPosition++;
	}

	public int numPositions() {
		return this.numPositions;
	}
}