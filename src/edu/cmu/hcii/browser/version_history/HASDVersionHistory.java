package edu.cmu.hcii.browser.version_history;

import java.util.ArrayList;
import java.util.List;

public class HASDVersionHistory {
	private List<HASDVersion> versions = new ArrayList<HASDVersion>();
	
	private HASDVersionHistory() {}
	
	public static HASDVersionHistory generate(String directory) {
		HASDVersionHistory history = new HASDVersionHistory();
		
		System.out.println("generate" + directory);
		
		return history;
	}
}
