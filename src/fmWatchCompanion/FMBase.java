package fmWatchCompanion;

import lotus.domino.Session;


public class FMBase {
	static Session notesSession;
	public static final String rootNotesIniFolderName="FMwathcdir_Folder";
	public static final String rootNotesIniCommand="FMwathcdir_Command";

	public Session getNotesSession() {
		return notesSession;
	}

	public void setNotesSession(Session notesSession) {
		FMBase.notesSession = notesSession;
	}
	
	public void log(String message) {
		System.out.println(message);
	}
	
	public void logException(String message, Exception e) {
		log(message + " In: " +e.getStackTrace()[0].getClassName() +
				" method: "+ e.getStackTrace()[0].getMethodName()+
				", row: " + e.getStackTrace()[0].getLineNumber() + ": " + e.toString());

	}

}
