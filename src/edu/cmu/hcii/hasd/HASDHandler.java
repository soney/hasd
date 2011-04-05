package edu.cmu.hcii.hasd;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import javax.swing.JFrame;

import processing.app.Editor;
import processing.app.SketchCode;
import processing.app.syntax.JEditTextArea;
import processing.app.syntax.SyntaxDocument;
import processing.app.syntax.TextAreaPainter;
import processing.app.syntax.TextAreaPainter.Highlight;
import bsh.ConsoleInterface;
import bsh.EvalError;
import bsh.Interpreter;

public class HASDHandler implements ConsoleInterface {

		private JFrame canvasFrame;
        HASDPApplet applet;
        private PrintStream outputStream;
        private Editor editor;
        
        private Interpreter interpreter;

        private ArrayList<SnapshotModel> snapshots = new ArrayList<SnapshotModel>();

        private boolean wasLastRunInteractive = false;

        public static boolean logTerminationMessage = true;

        private HASDLineModel lastExecutedLineModel = null;

        public int linesExecutedCount = 0; // TODO: refactor all this crap also this will overflow

        private static final boolean USEHIGHLIGHT = false;

        public HASDHandler(Editor editor) {
        		this.editor = editor;

        }

        
        public void handleRun(boolean present) {
                wasLastRunInteractive = false;
                editor.handleRun(present);
        }

       
        public void handleStop() {
                if (wasLastRunInteractive) {
                        applet.stop();
                        canvasFrame.dispose();
                } else {
                        editor.handleStop();
                }
        }

        public String appendCodeFromAllTabs() {
          return appendCodeFromAllTabs(true);
        }

        public String appendCodeFromAllTabs(boolean interactive) {
                StringBuffer bigCode = new StringBuffer();
                int bigCount = 0;
                for (SketchCode sc : editor.getSketch().getCode()) {
                  if (interactive)
                    sc.setPreprocOffset(bigCount);
                        if (sc == editor.getSketch().getCurrentCode()) {
                                bigCode.append(editor.getText());
                        bigCode.append('\n');
                        bigCount += editor.getLineCount();
                        } else {
                                bigCode.append(sc.getProgram());
                        bigCode.append('\n');
                        bigCount += sc.getLineCount();
                        }
                }

                return bigCode.toString();
        }

        public SketchCode lineToSketchCode(int line) {
                for (SketchCode sc : editor.getSketch().getCode()) {
                        int lineCount;
                        if (sc == editor.getSketch().getCurrentCode()) {
                                lineCount = editor.getLineCount();
                        } else {
                                lineCount = sc.getLineCount();
                        }

                        if (line >= sc.getPreprocOffset() && line < sc.getPreprocOffset() + lineCount) {
                                return sc;
                        }
                }

                return null;
        }

        public void handleHASDRun(boolean present) {
        	final HASDHandler self = this;
            // HelpMeOutLog.getInstance().write(HelpMeOutLog.STARTED_INTERACTIVE_RUN);
       	 if (USEHIGHLIGHT)
                editor.getTextArea().getPainter().addCustomHighlight(new RehearseHighlight());

       	editor.statusEmpty(); //clear the status area

               wasLastRunInteractive = true;
               // clear previous context
               if (canvasFrame != null)
                       canvasFrame.dispose();
               if (applet != null) {
                 // We want to explicitly call the superclass stop() function here
                 // because our overridden stop() handles some exception tracking info,
                 // namely watching to see if the exception was resolved on the last line
                 // of the program.
                       applet.resolveException = false;
                       applet.writeMovie(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub
	                          self.commit_version(applet.savePath("./.hasd/"));
	                          
						}
                       
                       });
                       applet.stop();
               }

               applet = new HASDPApplet(this);

               
               canvasFrame = new JFrame();
               canvasFrame.setLayout(new BorderLayout());
               canvasFrame.setSize(100, 100);
               canvasFrame.setResizable(false);


               //applet.setupFrameResizeListener();
               applet.frame = canvasFrame;
               applet.sketchPath = editor.getSketch().getFolder().getAbsolutePath();
               canvasFrame.add(applet, BorderLayout.CENTER);
               canvasFrame.setVisible(true);
               canvasFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
               canvasFrame.addWindowListener(new WindowAdapter() {
                   public void windowClosing(WindowEvent e) {
                         applet.stop();
                   }
           });

               // NOTE: If this line fails with java.lang.NoSuchMethodError you probably have a the BeanShell bshXXX.jar
               // in the classpath, e.g., /Library/Java/Extensions on OSX
               // to test, run "java bsh.Console" from terminal - if the beanshell console pops up, you'll have this problem
               // @see http://www.beanshell.org/manual/quickstart.html#Download_and_Run_BeanShell
               interpreter = new Interpreter(this, applet);

               String source = appendCodeFromAllTabs();

               // now, add our script to the processing.core package
               try {
                       interpreter.eval("package processing.core;");
               } catch (EvalError e1) {
                       e1.printStackTrace();
               }

               editor.getConsole().clear();
               ensureDocumentExistsForEveryTab();
               clearExecutionInfoForLines();
               /*
               snapshots.clear();
               editor.getTextArea().repaint();
               */
               try {
                       Object obj;
                       try {
                               obj = interpreter.eval(source, true);

                       } catch (ModeException e) {
                               if (e.isJavaMode()) {
                                       throw new RuntimeException("We don't do java mode yet!");
                               } else {
                                       // Code was written in static mode, let's try again.
                                       System.out.println("Code written in static mode, wrapping and restarting.");
                                       // this is kind of gross...
                                       obj = interpreter.eval("setup() {" + source + "}");
                               }
                       }
                       // This actually starts the program.
                       applet.init();

                       if (obj != null)
                       	editor.getConsole().message(obj.toString(), false, false);
               } catch (EvalError e) {
               //  HelpMeOutExceptionTracker.getInstance().processRuntimeException(e, interpreter);
                       editor.getConsole().message(e.toString(), true, false);
                       e.printStackTrace();
               }
               /*
        	this.editor.handleRun(present);
        	
        	String version_text = editor.getText();
        	System.out.println(version_text);
        	*/
               String version_text = this.editor.getText();
         	  this.commitText(applet.savePath("./.hasd/latest_version.txt"), version_text);  
      }
    public void commitText(String path, String text) {
    	try{
    		// Create file 
    		FileWriter fstream = new FileWriter(path);
    		    BufferedWriter out = new BufferedWriter(fstream);
    		out.write(text);
    		//Close the output stream
    		out.close();
    	}
    	catch (Exception e){//Catch exception if any
    		System.err.println("Error: " + e.getMessage());
    	}
    }
        
	public void commit_version(String path) {
		File hasd_directory = new File(path);
		File[] version_files = hasd_directory.listFiles(new FilenameFilter(){
			public boolean accept(File file, String name) {
				return file.isDirectory() && name.matches("v\\d+");
			}
		});
		int max_version = -1;
		
		for(int i = 0; i < version_files.length; i++) {
			File version_file = version_files[i];
			
			String file_name = version_file.getName();
			int version = Integer.parseInt(file_name.substring(1));
			
			if(version > max_version) max_version = version;
		}
		
		int current_version = max_version + 1;
		String directory_name = "v"+current_version;
		
		final File version_directory = new File(path+"/"+directory_name);
		version_directory.mkdir();
		

		
		File code = new File(path+"/latest_version.txt");
		if(code.exists()) {
			code.renameTo(new File(version_directory.getAbsolutePath()+"/code.txt"));
		}
		final File mov = new File(path+"/latest_version.mov");
		if(mov.exists()) {
			mov.renameTo(new File(version_directory.getAbsolutePath()+"/movie.mov"));
		}
	}

	public void error(Object o) {
		getOut().append(o.toString() + "\n");
	}
	
	public PrintStream getErr() {
	        return outputStream;
	}
	
	public Reader getIn() {
	        return new StringReader("");
	}
	
	public PrintStream getOut() {
	        return outputStream;
	}
	
	public void print(Object o) {
	        //getOut().append(o.toString());
	        System.out.print(o.toString());
	}
	
	public void println(Object o) {
	        //getOut().append(o.toString() + "\n");
	        System.out.println(o.toString());
	}
	
	private void ensureDocumentExistsForEveryTab() {
	        SketchCode currentCode = editor.getSketch().getCurrentCode();
	        for (SketchCode sc : editor.getSketch().getCode()) {
	                SyntaxDocument doc = (SyntaxDocument)sc.getDocument();
	                if (doc == null) {
	                        // This code makes the document and associates it with the
	                        // SketchCode object, performing appropriate initialization steps.
	                        // This isn't ideal but now we need each tab to have a valid
	                        // document reference even if that tab hasn't been clicked on yet.
	                	editor.setCode(sc);
	                }
	        }
	        // Set the code back to the one we started.
	        editor.setCode(currentCode);
	}
	
	private void clearExecutionInfoForLines() {
	        for (SketchCode sc : editor.getSketch().getCode()) {
	                SyntaxDocument doc = (SyntaxDocument)sc.getDocument();
	                        for (int line = 0; line < doc.getTokenMarker().getLineCount(); line++) {
	                        	HASDLineModel m =
	                                        (HASDLineModel)doc.getTokenMarker().getLineModelAt(line);
	                                if (m != null) {
	                                        m.executedInLastRun = false;
	                                        m.isMostRecentlyExecuted = false;
	                                }
	                        }
	        }
	}
	
	public void notifyLineExecution(int lineNumber) {
	        linesExecutedCount++;
	
	        if (lastExecutedLineModel != null)
	                lastExecutedLineModel.isMostRecentlyExecuted = false;
	
	        // snapshotPoints is zero-indexed, interpreter is one-indexed.
	        int line = lineNumber - 1;
	
	        SketchCode sc = lineToSketchCode(line);
	        SyntaxDocument doc = (SyntaxDocument)sc.getDocument();
	        
	        HASDLineModel m =
	                (HASDLineModel)doc.getTokenMarker().getLineModelAt(line - sc.getPreprocOffset());
	        if (m == null) {
	                m = new HASDLineModel();
	                doc.getTokenMarker().setLineModelAt(line - sc.getPreprocOffset(), m);
	        }
	
	
	        m.executedInLastRun = true;
	        m.isMostRecentlyExecuted = true;
	        m.countAtLastExec = linesExecutedCount;
	
	        editor.getTextArea().repaint();
	
	        if (m.isPrintPoint) {
	        	snapshots.add(interpreter.makeSnapshotModel());
	        }
	        lastExecutedLineModel = m;
	}
	
	public class TextAreaOutputStream extends OutputStream {
	        public void write( int b ) throws IOException {
	        	editor.getConsole().message( String.valueOf( ( char )b ), false, false);
	        }
	}
	
	private class RehearseHighlight implements TextAreaPainter.Highlight {
	        JEditTextArea textarea;
	        Highlight next;
	
	        public String getToolTipText(MouseEvent evt) {
	                if (next != null) {
	                        return null;
	                }
	                return null;
	        }
	
	        public void init(JEditTextArea textArea, Highlight next) {
	                textarea = textArea;
	                this.next = next;
	        }
	
	        public void paintHighlight(Graphics gfx, int line, int y) {
	                // Interpreter uses one-offset, processing uses zero-offset.
	                Color c = null;
	                HASDLineModel m =
	                        (HASDLineModel)(editor.getTextArea().getTokenMarker().getLineModelAt(line));
	                if (m != null) {
	                        /*
	                        if (m.executedInLastRun)
	                                c = Color.yellow;
	                        if (m.isMostRecentlyExecuted)
	                                c = Color.green;
	                        */
	
	                        int i = Math.min(linesExecutedCount - m.countAtLastExec, 150);
	                        // c = new Color(i,255,i);
	                        c = new Color(78,127,78,200-i);
	                }
	
	                //Color c = lineHighlights.get(line + 1);
	                if (c != null) {
	                        FontMetrics fm = textarea.getPainter().getFontMetrics();
	                        int height = fm.getHeight();
	                        y += fm.getLeading() + fm.getMaxDescent();
	                        gfx.setColor(c);
	                        gfx.fillRect(0,y,editor.getWidth(),height);
	                }
	
	                if (next != null) {
	                        next.paintHighlight(gfx, line, y);
	                }
	        }
	}
}