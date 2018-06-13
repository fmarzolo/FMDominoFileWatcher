package fmWatchCompanion;


import java.util.HashMap;
import java.util.Set;

import lotus.domino.NotesException;
import lotus.domino.Session;

/**********************************************************
 * PathList: allow to search for pairs like these in Notes.INI
 * FMwathcdir_Folder1...50=C:\temp
 * FMwathcdir_Command1...50=tell amgr run .....
 * 
 * 
 */

public class FMPathList {
	private HashMap<String, String> varFolderCommandPairs=new HashMap<String,String>(1,1);
	private Integer maxVarCount=50;
	Session	xDominoSession = null;
	FMBase base=null;

	public Boolean isEmpty() {
		return varFolderCommandPairs.isEmpty();
	}

	public Set<String> getNotesiniFolderNames () {
		return varFolderCommandPairs.keySet();
	}

	public String getNotesiniCommand(String folder) {
		String result="";
		if (varFolderCommandPairs.containsKey(folder)) {
			result=varFolderCommandPairs.get(folder);
		} 
		return result;
	}

	//	FMPathList(Session pDominoSession, Integer pMaxVarCount) {
	//	FMPathList(FMWatcher fmWatcher,Integer pMaxVarCount) {
	public FMPathList(FMBase base, Integer pMaxVarCount) {
		this.base=base;
		xDominoSession=base.getNotesSession();

		if (pMaxVarCount>0) {
			this.maxVarCount=pMaxVarCount;
		}
		init();
	}

	private void init() {
		try {
			String notesIniFolderName, notesIniCommand;
			Integer i;
			String str_i;
			for (i=0;i<=maxVarCount;i++) {
				//remove first "0" string integer to clean Notes.ini var and search it without trailing number
				str_i=(i==0)? "" : Integer.toString(i);
				notesIniFolderName=xDominoSession.getEnvironmentString(FMBase.rootNotesIniFolderName +  str_i, true);
				notesIniCommand=xDominoSession.getEnvironmentString(FMBase.rootNotesIniCommand + str_i , true);

				//				fmWatcher.debug("Getting var:" + rootNotesIniFolderName + str_i);
				if (notesIniFolderName.length()>0 & notesIniCommand.length()>0) {
					varFolderCommandPairs.put(notesIniFolderName, notesIniCommand);
					//					fmWatcher.log("Got Folder:" + notesIniFolderName + " --> "+ notesIniCommand);
				}
			}

		} catch (NotesException ne) {
			//			fmWatcher.logException("got NotesException, unable to continue.", ne);
			System.out.println("got NotesException, unable to continue." + ne);
		} finally {
			try {
				if (xDominoSession!=null) xDominoSession.recycle();
			} catch (Exception e) {
				//								fmWatcher.logException("got Exception in Finally.", e);
				System.out.println("got Exception in Finally "+ e);
			}
		}
	}

	/*
	public HashMap<String, String> getPairs()  {
		return varFolderCommandPairs;
	}
	 */


}
