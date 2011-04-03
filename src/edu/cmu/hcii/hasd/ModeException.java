package edu.cmu.hcii.hasd;

public class ModeException extends Exception {
	private static final long serialVersionUID = 1L;
	boolean javaMode = false;

    public boolean isJavaMode() {
            return javaMode;
    }

    public void setJavaMode(boolean javaMode) {
            this.javaMode = javaMode;
    }


}