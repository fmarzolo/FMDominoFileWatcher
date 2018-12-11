package fmWatchCompanion;
import fmWatchCompanion.FMBase;
import fmWatchCompanion.FMBase.DebugLevels;

import java.util.Set;

import lotus.domino.NotesException;
import lotus.domino.NotesFactory;
import lotus.domino.NotesThread;
import lotus.domino.Session;

/*
 * this thread wait for a number of second and then run a Domino command
 * the wait time is provided to allow other events at the same time:
 * the command is launched AFTER that all joining event are ended 
 */
//public class FMTimerAMGRunThread implements Runnable {
public class FMTimerAMGRun extends NotesThread {

	Long secondsDelay;
	Set<String> commands;
	Session	xDominoSession = null;
	FMBase base=null;
	//	FMWatcher fmWatcher=null;
	public FMTimerAMGRun(FMBase base, Long secondsWait, Set<String> pcommands) {
		this.base=base;
		this.secondsDelay=secondsWait;
		this.commands=pcommands;
		//		this.fmWatcher=fmWatcher;
		//		fmWatcher.logMessage("in thread command: "+ this.command);
		//		System.out.println("in thread command: "+ this.command);
	}
	
	
	public void runNotes() {
		
		
		
		//waiting time
		try {

			Thread.sleep(secondsDelay * 1000);
			
			base.log("Timer ended: LAUNCH COMMAND: " + commands.toString(), DebugLevels.HIGH);
			//wait ended, launch command
			xDominoSession=NotesFactory.createSession();
			for (String command : commands) {
				xDominoSession.sendConsoleCommand(xDominoSession.getServerName(), command);	
			}

		} catch (InterruptedException e) {
			// this Exception will be triggered for a Thread.interrupt in the code, so is not to be logged normally
			base.log("Thread interrupted:", DebugLevels.HIGH);
		} catch (NotesException e) {
			base.logException("NotesException launching command ", e);
		} finally {
			if ( xDominoSession!=null) {
				try { xDominoSession.recycle();
				} catch (NotesException e) {
					base.logException("NotesException recycling: ", e);
				}
			}
		}
	}
}
