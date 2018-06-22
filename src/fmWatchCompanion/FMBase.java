package fmWatchCompanion;

import lotus.domino.Session;

public class FMBase {
	static Session notesSession;
	public static final String rootNotesIniFolderName="FMwathcdir_Folder";
	public static final String rootNotesIniCommand="FMwathcdir_Command";
	public static final String rootNotesIniSubfolders="FMwathcdir_Subfolders";
	private Boolean showDebugMessages=false;

	public Session getNotesSession() {
		return notesSession;
	}

	public FMBase(Boolean pShowDebugMessages) {
		showDebugMessages=pShowDebugMessages;
	}
	
	public FMBase() {
	}

	public void setNotesSession(Session notesSession) {
		FMBase.notesSession = notesSession;
	}
	public void setDebug(Boolean pShowDebugMessages) {
		showDebugMessages=pShowDebugMessages;
	}

	public void log(String message) {
		System.out.println(message);
	}

	public void debug(String message) {
		if (showDebugMessages) {
			System.out.println(message);
		}
	}

	public void logException(String message, Exception e) {
		log(message + " In: " +e.getStackTrace()[0].getClassName() +
				" method: "+ e.getStackTrace()[0].getMethodName()+
				", row: " + e.getStackTrace()[0].getLineNumber() + ": " + e.toString());

	}

}
