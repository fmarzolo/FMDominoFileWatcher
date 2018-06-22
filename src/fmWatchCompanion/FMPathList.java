package fmWatchCompanion;


import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import lotus.domino.NotesException;
import lotus.domino.Session;

/**********************************************************
 * PathList: allow to search for pairs like these in Notes.INI
 * FMwathcdir_Folder...50=C:\temp
 * FMwathcdir_Command...50=tell amgr run .....
 * FMwathcdir_Subfolders...50=true (or 1)
 * FMwathcdir_Folder1...50=C:\temp
 * FMwathcdir_Command1...50=tell amgr run .....
 * FMwathcdir_Subfolders...50=false (or 0)
 * FMwathcdir_Folder2...50=C:\temp
 * FMwathcdir_Command2...50=tell amgr run .....
 * 
 * 
 */

public class FMPathList {
	private Path2CommandListCommander p2cList=new Path2CommandListCommander();
	private Integer maxVarCount=50;
	Session	xDominoSession = null;
	FMBase base=null;

	public Boolean isEmpty() {
		//		return varFolderCommandPairs.isEmpty();
		//trasformare in 
		return p2cList.isEmpty();
	}

	public Set<String> getNotesiniFolderNames () {
		//		return varFolderCommandPairs.keySet();
		//trasformare in 
		return p2cList.getFolderSet();
	}


	public Set<String> getNotesiniCommandSet(String folder) {
		return p2cList.getCommands4Folder(folder);
	}

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
			String notesIniFolderName, notesIniCommand, notesIniSubfolders;
			Integer i;
			String str_i;
			for (i=0;i<=maxVarCount;i++) {
				//remove first "0" string integer to clean Notes.ini var and search it without trailing number
				str_i=(i==0)? "" : Integer.toString(i);
				notesIniFolderName=xDominoSession.getEnvironmentString(FMBase.rootNotesIniFolderName +  str_i, true);
				notesIniCommand=xDominoSession.getEnvironmentString(FMBase.rootNotesIniCommand + str_i , true);
				notesIniSubfolders=xDominoSession.getEnvironmentString(FMBase.rootNotesIniSubfolders + str_i , true);

				//				fmWatcher.debug("Getting var:" + rootNotesIniFolderName + str_i);
				if (notesIniFolderName.length()>0 & notesIniCommand.length()>0) {
					//					varFolderCommandPairs.put(notesIniFolderName, notesIniCommand);
					base.log("Got Folder:" + notesIniFolderName + " --> "+ notesIniCommand);

					PathCommandPair pathCommandPair=new PathCommandPair(notesIniFolderName, notesIniCommand, notesIniSubfolders);
					if (pathCommandPair.getWatchSubfolders()) {
						base.log("Sorry! Subfolder watching not implemented currently! (Folder "+ pathCommandPair.getPath() + ")");
					}
					p2cList.put(pathCommandPair);
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

/*
 * this class mantains a Vector for each folder to be watched. 
 * Can have siblings (same folder multiple times), so you can register multiple command for the same folder.
 * 
 */

class Path2CommandListCommander {
	private Vector<PathCommandPair> p2cList=new Vector<PathCommandPair>(5,5);

	public void put(PathCommandPair newElem) {
		try {
			p2cList.addElement(newElem);
		} catch (Exception ne) {
			System.out.println("Exception: " + ne.getStackTrace()[0].getClassName() +
					" method: "+ ne.getStackTrace()[0].getMethodName()+
					", row: " + ne.getStackTrace()[0].getLineNumber() + ": " + ne.toString());
		}
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

/*
 * this class allow keep for each folder to be watched: 
 * path
 * boolean for register on subfolders
 * domino command for this folder
 * 
 */
 
 
class PathCommandPair {
	private String path="";
	private Boolean watchSubfolders=false;
	private String dominoCommand="";
	public PathCommandPair(String pPath, String pCommand,Boolean pWatchSubfolders) {
		setPath(pPath);
		setDominoCommand(pCommand);
		setWatchSubfolders(pWatchSubfolders);
	}

	public PathCommandPair(String pPath, String pCommand,String pWatchSubfolders) {
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
	
	//overload to permits zero length value or "1" or "true"
	public void setWatchSubfolders(String watchSubfolders) {
		if (watchSubfolders.length()==0)
			this.watchSubfolders =false;
		else
			this.watchSubfolders = watchSubfolders.equalsIgnoreCase("true") || watchSubfolders.equalsIgnoreCase("1") ? true : false;
	}

	public String getDominoCommand() {
		return dominoCommand;
	}
	public void setDominoCommand(String dominoCommand) {
		this.dominoCommand = dominoCommand;
	}

}