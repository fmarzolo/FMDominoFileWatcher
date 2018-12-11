package fmWatchCompanion;

import lotus.domino.Session;

public class FMBase {
	public enum DebugLevels {
		NONE(0), 
		LOW(1), 
		NORM(5), 
		HIGH(7), 
		TRACE(9);

		private Integer severity;
		
		DebugLevels(int severity) {
	        this.severity = severity;
	    }	
	
		public boolean isHigherThan(DebugLevels other) {
	        return this.severity >= other.severity;
	    }
		
		public boolean isLowerThan(DebugLevels other) {
	        return this.severity <= other.severity;
	    }
		
	};
	static Session notesSession;
	public static final String rootNotesIniFolderName="FMwatchdir_Folder";
	public static final String rootNotesIniCommand="FMwatchdir_Command";
	public static final String rootNotesIniSubfolders="FMwatchdir_Subfolders";
	private DebugLevels debugLevel=DebugLevels.NONE;

	public Session getNotesSession() {
		return notesSession;
	}

	public FMBase(DebugLevels debugLevel) {
		setDebugLevel(debugLevel);
	}
	
	public FMBase(Session notesSession) {
		FMBase.notesSession = notesSession;
	}

//
//	public void setDebug(Boolean pShowDebugMessages) {
//		showDebugMessages=pShowDebugMessages;
//	}
	
	public void setDebugLevel(DebugLevels debugLevel) {
		this.debugLevel=debugLevel;
	}

	public void log(String message) {
		try {
			System.out.println(message);	
		} catch (Exception e) {
			System.out.println("Exception in log:" + e);
		}
		
	}

	public void log(String message, DebugLevels showifDebugHigherThan) {
	
		if (showifDebugHigherThan.isLowerThan(debugLevel)) {
			System.out.println(message);
		}
	}
	public void debug(String message, DebugLevels showifDebugHigherThan) {
		if (showifDebugHigherThan.isLowerThan(debugLevel)) {
			System.out.println(message);
		}
	}

	public void logException(String message, Exception e) {
		log(message + " In: " +e.getStackTrace()[0].getClassName() +
				" method: "+ e.getStackTrace()[0].getMethodName()+
				", row: " + e.getStackTrace()[0].getLineNumber() + ": " + e.toString());

	}

}
