/**
 * you can put a one sentence description of your tool here.
 *
 * ##copyright##
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 * 
 * @author		##author##
 * @modified	##date##
 * @version		##version##
 */

package edu.cmu.hcii.hasd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

import processing.app.Editor;
import processing.app.syntax.PdeTextAreaDefaults;
import processing.app.tools.Tool;


public class HASD implements Tool {
	private Editor editor;
	
 
	public String getMenuTitle() {
		return "HASD";
	}
 
	public void init(Editor theEditor) {
		
/*
	
		try{
			
			out.close();
			in.close();
			kkSocket.close();
		}
		catch(IOException e) {
			e.printStackTrace();
		}
	*/	
this.editor = theEditor;
		editor.setCustomToolbar(new HASDToolbar(this, editor, editor.getToolbarMenu()), this);
		editor.setCustomTextArea(new HASDTextArea(this, new PdeTextAreaDefaults(), editor), this);
	}
 
	public void run() {
		 
	}
	
}