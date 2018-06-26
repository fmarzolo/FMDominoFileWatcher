//wonderful example for watching directory in thread:
//https://www.thecoderscorner.com/team-blog/java-and-jvm/java-nio/36-watching-files-in-java-7-with-watchservice/

/*
 * This class needs JAddin from http://abdata.ch
 */

import java.io.IOException;
import java.nio.file.*;

import fmWatchCompanion.FMBase;
import fmWatchCompanion.FMCommandLineParser;
import fmWatchCompanion.FMDirChangeEventReceiver;
import fmWatchCompanion.FMPathList;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;


public class FMWatcher extends JAddinThread {
	private Boolean doQuit=false;
	WatchService myWatcher ;
	FMDirChangeEventReceiver dirChangeEventReceiver;
	Thread thread4DirChangeEventReceiver ;
	static final String FMWATCHERVERSION="0.4.1";
	private int varsMax=10;

	private FMBase base=null;

	
	void debug(String pMessage) {
		logDebug(pMessage);
	}


	// This method runs in a separate thread to avoid any processing delays of the main Domino message queue.
	public void addinStart() {
		
		base=new FMBase();
		base.setNotesSession(getDominoSession());
		

		if (getAddinParameters()!=null) { 
			base.log(getName() + " started with the parameters <" + getAddinParameters() + '>');
			FMCommandLineParser options=new FMCommandLineParser( getAddinParameters());

			if (options.isError()) {
				usage();	
			} else {
				if (options.get("varsMax",0).length()>0) {
					varsMax=Integer.parseInt(options.get("varsMax",0));
					logMessage("Will read " + varsMax + " variables from notes.ini");
				} else if (options.get("debug",0).length()>0) {
					base.setDebug(true);
					logMessage("debug messages will be shown");
				} else {
					logMessage("You can add parameters:");
					logMessage("\"-varsMax n\" to load as many notes.ini vars (default:"+ varsMax+")");
					logMessage("\"-debug\" to show debug messages (in add to the debug! JAddin parameter)");
				}
			}
		}

		try {
			base.log(getName() + " version " + FMWATCHERVERSION +  " Running on " + getDominoSession().getNotesVersion());
		} catch (Exception e) {
			base.logException("Unable to get version: " , e);
		}

		//discover pairs of folder to monitor and command to execute
		FMPathList pathList=new FMPathList(base,varsMax);

		if (pathList.isEmpty()) {
			base.log("Unable to get any configuration.\n\n Please configure and restart");
		} else {
			base.log("Got configurations");
			for (String folderName : pathList.getNotesiniFolderNames()) {
				base.log("folderName:" + folderName + " commands:"+String.join(", ", pathList.getNotesiniCommandSet(folderName)));
			}
			try {
				if  (! setMonitorFolders(pathList)) {
					base.log("setMonitor Error");
					doQuit=true;
				};
			} catch (Exception e){
				base.logException("Unknown Exception " , e);
				doQuit=true;
			}
		}

		// Loop to see that the user thread is running ...
		while (! doQuit) {
			if (thread4DirChangeEventReceiver.isAlive()) {
				base.log("Thread waiting events... do nothing.... and sleep...");
			} else {
				base.log("Thread terminated, restart program");
			}

			waitSeconds(15);
		}
		base.log(getName() +" Quitting!");
	}

	public void addinNextHour() {
		super.addinNextHour();

		// here we can reconfigure restarting watching

	}
	void usage() {
		logMessage(getName() + " set any number of Watchable dirs so that every change trigger Domino commands.");
		logMessage("NOTES.ini must contains as many pairs you like in the form:");
		logMessage("FMwathcdir_Folder=c:\\temp");
		logMessage("FMwathcdir_Command=tell amgr run \"path\\db.nsf\" 'agentname'");
		logMessage("FMwathcdir_Folder1=\\\\nas4\\public\\Software");
		logMessage("FMwathcdir_Command1=tell amgr run \"path\\db.nsf\" 'agentname'");
		logMessage("You can launch program with the parameter -varsMax n to have as many folder watched you need");
	}

	//set monitor registering every folder
	Boolean setMonitorFolders  ( FMPathList pathList) {


		// make a new watch service that we can register interest in
		// directories and files with.
		String storeFolderName="";
		Boolean result=false;		

		try {
			myWatcher = FileSystems.getDefault().newWatchService();
			dirChangeEventReceiver = new FMDirChangeEventReceiver(base,myWatcher, pathList);
			thread4DirChangeEventReceiver = new Thread(dirChangeEventReceiver, "FileWatcher");
			thread4DirChangeEventReceiver.start();
			int i=0;
			for (String folderName : pathList.getNotesiniFolderNames()) {
				i++;
				storeFolderName=folderName;
				debug("folderName:" + folderName + " commands:"+pathList.getNotesiniCommandSet(folderName));
				Path toWatch = Paths.get(folderName);
				if(toWatch == null) {
					base.log("Directory not found:" + folderName);
				} else {
					debug("registering folder ("+ i + "):" + folderName);
					// il metodo register consentirebbe di ricevere una WatchKey univoca per ogni folder
					// non la usiamo, useremo 
					toWatch.register(myWatcher, ENTRY_CREATE, ENTRY_MODIFY);
					
							
				}

			}
			result=true;
		} catch (AccessDeniedException e) {
			base.logException("Exception: Access denied in folder: "+storeFolderName , e);
		} catch (IOException e) {
			base.logException("Exception: " ,e);
		}
		return result;
	}

	// This method is called asynchronously by the JAddin framework when the command 'Quit' or 'Exit' is entered or at Domino
	// server shutdown. It should be executed as quickly as possible to avoid any main Domino message queue delays. After
	// returning, the JAddin framework issues Thread.interrupt() to signal termination to the addInStart() code.
	public void addinStop() {
		doQuit=true;
		base.log(getName() + ": Termination in progress");
	}

	// This method is called asynchronously by the JAddin framework for any console command entered. It should be executed as quickly
	// as possible to avoid any main Domino message queue delays.
	public void addinCommand(String command) {
		base.log(getName() + ": You have entered the command <" + command + '>');

		if (command.equalsIgnoreCase("SendMail")) {
			try {
				sendMessage("FromAddress@acme.com", "RecipientAddress@acme.com", "Test message", "Some email text content");
			} catch (Exception e) {
				base.log("Unable to send message: " + e.getMessage());
			}
		}
	}



}




