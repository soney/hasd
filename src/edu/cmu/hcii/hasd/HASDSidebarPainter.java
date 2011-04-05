package edu.cmu.hcii.hasd;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.PrintWriter;

import javax.swing.JComponent;

import processing.app.Base;
import processing.app.syntax.TextAreaPainter;

/**
 * Handles painting the left sidebar where "print points" or other
 * line-specific interactions could happen.
 *
 */
public class HASDSidebarPainter extends JComponent {
	private static final long serialVersionUID = 1L;
	private TextAreaPainter textAreaPainter;
	private HASDTextArea textArea;

	public HASDSidebarPainter(HASDTextArea textArea, TextAreaPainter textAreaPainter) {
		this.textArea = textArea;
		this.textAreaPainter = textAreaPainter;
		setAutoscrolls(true);
		setDoubleBuffered(true);
		setOpaque(true);
		this.addMouseListener(new MouseEventHandler());
	}

	public void paint(Graphics gfx)
	{
		Rectangle clipRect = gfx.getClipBounds();

		gfx.setColor(Color.black);
		gfx.fillRect(clipRect.x,clipRect.y,clipRect.width,clipRect.height);

		// We don't use yToLine() here because that method doesn't
		// return lines past the end of the document
		int height = textAreaPainter.getFontHeight();
		int firstLine = textArea.getFirstLine();
		int firstInvalid = firstLine + clipRect.y / height;
		// Because the clipRect's height is usually an even multiple
		// of the font height, we subtract 1 from it, otherwise one
		// too many lines will always be painted.
		int lastInvalid = firstLine + (clipRect.y + clipRect.height - 1) / height;

		for (int line = firstInvalid; line <= lastInvalid; line++) {
			Color c = Color.gray;
			if (line < textArea.getLineCount()) {
				HASDLineModel m =
					(HASDLineModel)textArea.getTokenMarker().getLineModelAt(line);
				if (m != null && m.isPrintPoint)
					c = Color.red;
			}
			gfx.setColor(c);
			int y = textArea.lineToY(line);
			gfx.fillRect(0, y + 3, getWidth(), height);
		}

		int h = clipRect.y + clipRect.height;
		repaint(0,h,getWidth(),getHeight() - h);
	}
	final HASDSidebarPainter self = this;
	class MouseEventHandler extends MouseAdapter {
		public void mousePressed(MouseEvent event) {
			int height = textAreaPainter.getFontHeight();
			int line = textArea.getFirstLine() + event.getY() / height;
			
			PrintWriter server_out = Base.server_out;
			server_out.println("line_clicked");
			server_out.println(line);
			server_out.println(self.textArea.editor.getSketch().getFolder());
			server_out.println(event.getXOnScreen()+","+event.getYOnScreen());
			server_out.println(self.textArea.editor.getText());
			server_out.println("__HASD__ END PROGRAM");
			
			if(server_out.checkError()) {
				System.out.println("ERROR");
			}
		}
		/*
                public void mouseEntered(MouseEvent e) {
                //	System.out.println("Mouse over");
                    HASDSidebarPainter.this.repaint();
                }
                public void mouseExited(MouseEvent e) {
                	//System.out.println("Mouse over");
                    HASDSidebarPainter.this.repaint();
                }
		 */
	}
}