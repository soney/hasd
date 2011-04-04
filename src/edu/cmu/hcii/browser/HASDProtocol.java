package edu.cmu.hcii.browser;

import java.awt.Point;

import edu.cmu.hcii.browser.actions.HASDAction;
import edu.cmu.hcii.browser.actions.LineClickedAction;


public class HASDProtocol {
	private static final int WAITING = 0;
	private static final int COMMAND = 1;

	private static final int COMMAND_LINE_CLICKED_LINE_NUM = 2;
	private static final int COMMAND_LINE_CLICKED_PATH = 3;
	private static final int COMMAND_LINE_CLICKED_POINT = 4;
	private static final int COMMAND_LINE_CLICKED_EDITOR_TEXT = 5;

	private int state = WAITING;

	private LineClickedAction line_clicked_action;

	public HASDAction processInput(String theInput) {
		if (state == WAITING) {
			state = COMMAND;
		}
		else if (state == COMMAND) {
			if(theInput.equals("line_clicked")) {
				state = COMMAND_LINE_CLICKED_LINE_NUM;

				this.line_clicked_action = new LineClickedAction();
			}
		}

		else if(state == COMMAND_LINE_CLICKED_LINE_NUM) {
			int line_number = Integer.parseInt(theInput);
			state = COMMAND_LINE_CLICKED_PATH;
			this.line_clicked_action.line_num = line_number;
		}
		else if(state == COMMAND_LINE_CLICKED_PATH) {
			state = COMMAND_LINE_CLICKED_POINT;
			this.line_clicked_action.path = theInput;
		}
		else if(state == COMMAND_LINE_CLICKED_POINT) {
			state = COMMAND_LINE_CLICKED_EDITOR_TEXT;
			int comma_loc = theInput.indexOf(",");
			int x = Integer.parseInt(theInput.substring(0, comma_loc));
			int y = Integer.parseInt(theInput.substring(comma_loc+1));
			this.line_clicked_action.point = new Point(x,y);
		}
		else if(state == COMMAND_LINE_CLICKED_EDITOR_TEXT) {
			if(theInput.equals("__HASD__ END PROGRAM")) {
				state = COMMAND;
				return this.line_clicked_action;
			}
			else {
				this.line_clicked_action.currentFile.add(theInput);
			}
		}
		return null;
	}
}