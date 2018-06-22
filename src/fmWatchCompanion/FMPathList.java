package fmWatchCompanion;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

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
	private Path2CommandListCommander p2cList;
	private HashMap<String, String> varFolderCommandPairs=new HashMap<String,String>(1,1);
	private Integer maxVarCount=50;
	Session	xDominoSession = null;
	FMBase base=null;

	public Boolean isEmpty() {
		return varFolderCommandPairs.isEmpty();
		//trasformare in 
		//return p2cList.isEmpty();
	}

	public Set<String> getNotesiniFolderNames () {
		return varFolderCommandPairs.keySet();
		//trasformare in 
		//return p2cList.getFolderSet();
	}

	public String getNotesiniCommand(String folder) {
		String result="";
		if (varFolderCommandPairs.containsKey(folder)) {
			result=varFolderCommandPairs.get(folder);
		} 
		return result;
		//obsoleto, ne ritorna solo uno, vogliamo possano essere comandi multipli per medesimo folder
	}

	public Set<String> getNotesiniCommandSet(String folder) {
		return p2cList.getCommands4Folder(folder);
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

					//aggiungiamo al nuovo oggetto, poi sarà da togliere varFolderCommandPairs
					p2cList.put(new PathCommandPair(notesIniFolderName, notesIniCommand, false));
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

class Path2CommandListCommander {
	private Vector<PathCommandPair> p2cList=new Vector<PathCommandPair>(5,5);

	public void put(PathCommandPair newElem) {
		p2cList.addElement(newElem);
	}
	public Boolean isEmpty() {
		return p2cList.isEmpty();
	}
	public Set<String> getFolderSet() {
		//ritorna l'elenco del path da monitorare
		Set<String> result=new HashSet<String>(p2cList.size());

		for (int i=0; i < p2cList.size(); i++) {
			result.add( p2cList.get(i).getPath());
		}
		return result;
	}

	public Set<String> getCommands4Folder(String folder) {
		//ritorna un Set di comandi per un dato folder
		Set<String> result=new HashSet<String>(5,5);
		for (int i=0; i < p2cList.size(); i++) {
			if (p2cList.get(i).getPath().equals(folder)) {
				result.add( p2cList.get(i).getDominoCommand());
			}
		}

		return result;
	}
}

class PathCommandPair {
	private String path="";
	private Boolean watchSubfolders=false;
	private String dominoCommand="";
	public PathCommandPair(String pPath, String pCommand,Boolean pWatchSubfolders) {
		setPath(pPath);
		setDominoCommand(pCommand);
		setWatchSubfolders(pWatchSubfolders);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Boolean getWatchSubfolders() {
		return watchSubfolders;
	}
	public void setWatchSubfolders(Boolean watchSubfolders) {
		this.watchSubfolders = watchSubfolders;
	}
	public String getDominoCommand() {
		return dominoCommand;
	}
	public void setDominoCommand(String dominoCommand) {
		this.dominoCommand = dominoCommand;
	}

}