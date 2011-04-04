package edu.cmu.hcii.browser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTError;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import edu.cmu.hcii.browser.actions.HASDAction;
import edu.cmu.hcii.browser.actions.LineClickedAction;
import edu.cmu.hcii.browser.version_history.HASDVersionHistory;


public class HASDServer {
	private Display display;
	private Shell shell;
	private Browser browser;
	
    public static void main(String[] args) throws IOException {
    	HASDServer server = new HASDServer();
    	server.initialize();
    }
    
    public void initialize() {
    	final HASDServer self = this;
    	Runnable r = new Runnable() {
    		public void run() {
    			try {
    				self.setupServer();
				} catch (IOException e) {
					e.printStackTrace();
				}
    		}
    	};
    	Thread t = new Thread(r);
    	t.start();
    	this.setupBrowser();
    }
    
    public void setupServer() throws IOException {
    	 ServerSocket serverSocket = null;
         try {
             serverSocket = new ServerSocket(4444);
         } catch (IOException e) {
             System.err.println("Could not listen on port: 4444.");
             System.exit(1);
         }

         Socket clientSocket = null;
         try {
             clientSocket = serverSocket.accept();
         } catch (IOException e) {
             System.err.println("Accept failed.");
             System.exit(1);
         }

         PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
         BufferedReader in = new BufferedReader(
 				new InputStreamReader(
 				clientSocket.getInputStream()));
         String inputLine;
         HASDAction outputAction;
         HASDProtocol kkp = new HASDProtocol();

         outputAction = kkp.processInput(null);

         while ((inputLine = in.readLine()) != null) {
              outputAction = kkp.processInput(inputLine);
              if(outputAction!=null) {
            	  if(outputAction instanceof LineClickedAction) {
            		  final LineClickedAction lca = (LineClickedAction) outputAction;
		              this.display.syncExec(
		            		  new Runnable() {
		            		    public void run() {
		            		    	shell.setVisible(true);
		            		    	shell.setLocation(new Point(lca.point.x, lca.point.y));
		            		    	shell.setSize(400, 300);
		            		    	shell.setFocus();
		            		    	HASDVersionHistory version_history = HASDVersionHistory.generate(lca.path);
		            		    	//String browserText = HASDWebpage.GenerateWebpage();
		            		    	//browser.setText(browserText);
		            		    }
		            		  });
            	  }
              }
         }
         out.close();
         in.close();
         clientSocket.close();
         serverSocket.close();
         System.exit(0);
    }
    
 
    
    
    public void setupBrowser() {
    	this.display = new Display();
		this.shell = new Shell(display, SWT.ON_TOP);
		GridLayout gridLayout = new GridLayout();
		shell.setLayout(gridLayout);
		
		GridData data = new GridData();

		//final Browser browser;
		try {
			browser = new Browser(shell, SWT.NONE);
		} catch (SWTError e) {
			System.out.println("Could not instantiate Browser: " + e.getMessage());
			display.dispose();
			return;
		}
		
		data = new GridData();
		data.horizontalAlignment = GridData.FILL;
		data.verticalAlignment = GridData.FILL;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		browser.setLayoutData(data);

		shell.open();
		shell.setAlpha(220);
		shell.setVisible(false);

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}