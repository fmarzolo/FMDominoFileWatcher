package fmWatchCompanion;



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
	String command;
	Session	xDominoSession = null;
	//	FMWatcher fmWatcher=null;
	public FMTimerAMGRun(Long secondsWait, String pcommand) {
		this.secondsDelay=secondsWait;
		this.command=pcommand;
		//		this.fmWatcher=fmWatcher;
		//		fmWatcher.logMessage("in thread command: "+ this.command);
		//		System.out.println("in thread command: "+ this.command);
	}
	

	public void runNotes() {

		//waiting time
		try {
			Thread.sleep(secondsDelay * 1000);
			System.out.println("Timer ended: LAUNCH COMMAND: " + command );
			//wait ended, launch command
			xDominoSession=NotesFactory.createSession();
			xDominoSession.sendConsoleCommand(xDominoSession.getServerName(), command);

		} catch (InterruptedException e) {
			System.out.println("Thread interrupted for command:" + command + " ****************************" );
		}catch (NotesException e) {
			System.out.println("NotesException launching command: "+ command);
		} finally {
			if ( xDominoSession!=null) {
				try { xDominoSession.recycle();
				}catch (NotesException e) {
					System.out.println("NotesException launching command: "+ e.getMessage());
				}
			}

		}



	}

}
