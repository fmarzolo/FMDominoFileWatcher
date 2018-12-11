//wonderful example for watching directory in thread:
//https://www.thecoderscorner.com/team-blog/java-and-jvm/java-nio/36-watching-files-in-java-7-with-watchservice/

/*
 * This class needs JAddin from http://abdata.ch, version 1.3
 */

import java.io.IOException;
import java.nio.file.*;

import fmWatchCompanion.FMBase;
import fmWatchCompanion.FMBase.DebugLevels;
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
	static final String FMWATCHERVERSION="0.4.2";
	private int varsMax=10;

	private FMBase base=null;

	// This method runs in a separate thread to avoid any processing delays of the main Domino message queue.
	public void addinStart() {

		base=new FMBase(getDominoSession());

		try {
			base.log(getName() + " version " + FMWATCHERVERSION +  " Running on " + getDominoSession().getNotesVersion());
		} catch (Exception e) {
			base.logException("Unable to get version: " , e);
		}

		//decoding parameters
		try {
			if (getAddinParameters()!=null) {
				base.log(getName() + " started with parameters <" + getAddinParameters() + '>');
				FMCommandLineParser options=new FMCommandLineParser( getAddinParameters());
				//				base.log("letti options da notes.ini");
				//				base.log("keyset:" + String.join(", ", options.getKeySet()));

				if (options.isError()) {
					usageParms();	
				} else {
					base.log("reading parms");
					if (options.get("varsMax",0).length()>0) {
						varsMax=Integer.parseInt(options.get("varsMax",0));
						base.log("Will read " + varsMax + " variables from notes.ini", DebugLevels.HIGH);
					} 
					if (options.get("loglevel",0).length()>0) {
						//						base.log("reading loglevel");
						int loglev=Integer.parseInt(options.get("loglevel",0));
						//						logMessage("Setting loglevel to " + loglev);
						switch (loglev) {
						case 0:	base.setDebugLevel(DebugLevels.NONE); break;
						case 1:	base.setDebugLevel(DebugLevels.LOW);break;
						case 5:	base.setDebugLevel(DebugLevels.NORM);break;
						case 7:	base.setDebugLevel(DebugLevels.HIGH);break;
						case 9:	base.setDebugLevel(DebugLevels.TRACE);break;
						default:usageVars();
						}
					} 
					if (options.get("debug",0).length()>0) {
						//						base.log("reading debug");
						logMessage("debug messages will be shown");
						base.setDebugLevel(DebugLevels.TRACE);
					} 
				}
			}
		} catch (Exception e) {
			base.logException("Exception decoding parameters", e);
			doQuit=true;
		}


		if (! doQuit) {
			//discover pairs of folder to monitor and command to execute
			FMPathList pathList=new FMPathList(base,varsMax);

			if (pathList.isEmpty()) {
				base.log("Unable to get any configuration.\n\n Please configure and restart");
			} else {
				base.log("Got configurations", DebugLevels.HIGH);
				for (String folderName : pathList.getNotesiniFolderNames()) {
					base.log("folderName:" + folderName + " commands:"+String.join(", ", pathList.getNotesiniCommandSet(folderName)), DebugLevels.HIGH);
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
					base.log("Thread waiting events... do nothing.... and sleep...", DebugLevels.HIGH);
				} else {
					base.log("Thread terminated unexpectedly, restart program");
				}
				waitSeconds(15);
			}
		}
		base.log(getName() +" Quitting!");
	}

	public void addinNextHour() {
		super.addinNextHour();
		// here we can reconfigure restarting watching
	}
	void usageVars() {
		logMessage(getName() + " set any number of Watchable dirs so that every change trigger Domino commands.");
		logMessage("NOTES.ini must contains as many pairs you like in the form:");
		logMessage("FMwatchdir_Folder=c:\\temp");
		logMessage("FMwatchdir_Command=tell amgr run \"path\\db.nsf\" 'agentname'");
		logMessage("FMwatchdir_Folder1=\\\\nas4\\public\\Software");
		logMessage("FMwatchdir_Command1=tell amgr run \"path\\db.nsf\" 'agentname'");		
	}

	void usageParms() {
		logMessage("You can launch program with the parameter -varsMax n to have as many folder watched you need");
		logMessage("You can add parameters:");
		logMessage("\"-varsMax n\" to load as many notes.ini vars (default:"+ varsMax+")");
		logMessage("\"-loglevel n\" to set log level (0-1-5-7-9, default:"+ 0+")");
		logMessage("\"debug\" to show debug messages (same as -loglevel 9, in add to the debug! JAddin parameter)");
		logMessage("");
		logMessage("\"-loglevel n\" is available as command, use: tell " + getName() + "-loglevel 9");
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
				base.log("folderName:" + folderName + " commands:"+pathList.getNotesiniCommandSet(folderName), DebugLevels.HIGH);
				Path toWatch = Paths.get(folderName);
				if(toWatch == null) {
					base.log("Directory not found:" + folderName);
				} else {
					base.log("registering folder ("+ i + "):" + folderName, DebugLevels.HIGH);
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
		base.log(getName() + ": Quit requested",DebugLevels.HIGH);
	}

	// This method is called asynchronously by the JAddin framework for any console command entered. It should be executed as quickly
	// as possible to avoid any main Domino message queue delays.
	public void addinCommand(String command) {
		base.log(getName() + ": You have entered the command <" + command + '>');
		try {
			FMCommandLineParser options=new FMCommandLineParser( command);

			//complete command case:
			if (command.equalsIgnoreCase("SendMail")) {
				try {
					sendMessage("FromAddress@acme.com", "RecipientAddress@acme.com", "Test message", "Some email text content");
				} catch (Exception e) {
					base.logException("Unable to send message", e);
				}
			}

			//parameterized command case:
			if (options.get("loglevel",0).length()>0) {
				int loglev=Integer.parseInt(options.get("loglevel",0));
				base.log("setting loglevel to " +loglev);
				switch (loglev) {
				case 0:	base.setDebugLevel(DebugLevels.NONE); break;
				case 1:	base.setDebugLevel(DebugLevels.LOW);break;
				case 5:	base.setDebugLevel(DebugLevels.NORM);break;
				case 7:	base.setDebugLevel(DebugLevels.HIGH);break;
				case 9:	base.setDebugLevel(DebugLevels.TRACE);break;
				default:usageParms();
				}
			}

		} catch (Exception e) {
			base.logException("Unable to understand command", e);
		}


	}

}
