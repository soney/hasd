package edu.cmu.hcii.browser.actions;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

public class LineClickedAction implements HASDAction {
	public List<String> currentFile = new ArrayList<String>();
	public int line_num;
	public String path = null;
	public Point point = null;
}
