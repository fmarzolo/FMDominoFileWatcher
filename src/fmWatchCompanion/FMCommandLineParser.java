package fmWatchCompanion;

//found on https://stackoverflow.com/a/26376532/3021194 
/*
 *use: 
 *-arg1 1 2 --arg2 3 4

System.out.print(params.get("arg1").get(0)); // 1
System.out.print(params.get("arg1").get(1)); // 2
System.out.print(params.get("-arg2").get(0)); // 3
System.out.print(params.get("-arg2").get(1)); // 4 
 * 
 * 
 */
	

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class FMCommandLineParser  {
	//this class provides decoding of parameters in the following form:
	//			-command1 parm1 parm2 -command2 parm3
	// and construct a tree:
	//			command1
	//				parm1
	//				parm2
	//			command2
	//				parm3

	final Map<String, List<String>> params =new TreeMap<String, List<String>>(String.CASE_INSENSITIVE_ORDER);

	List<String> options = null;
	Boolean error=false;
	
	public Boolean isError() {
		return error;
	}
	
	public boolean containsKey(String key) throws Exception {
		if (params.containsKey(key)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public String get(String arg, int pos) throws Exception {
		String ret="";
		if (params.containsKey(arg))
			ret=params.get(arg).get(pos).toString();
		else
			ret="";
		return ret;
	}
	
	public String[] getKeySet() throws Exception {
		String[] strings = params.keySet().toArray(new String[params.size()]);
		return strings;
	}
	
	public FMCommandLineParser(String[] args) throws Exception  {
		init(args);
	}
	
	public FMCommandLineParser(String args) throws Exception {
		String[] parameters = args.split("\\s+");
		init(parameters);
	}
	
	public void init(String[] args) {
	
		for (int i = 0; i < args.length; i++) {
			final String a = args[i];

			if (a.charAt(0) == '-') {				//this is a -command (stored stripping "-")
				if (a.length() < 2) {
					error=true;
					System.out.println("Error at argument " + a);
					return;
				}
				options = new ArrayList<>();
				params.put(a.substring(1), options);
			} else if (options != null) {			//this add an option to 
				options.add(a);
			} else {
				System.out.println("Illegal parameter usage: \"" + a+"\", please specify commands starting with an hyphen");
				error=true;
				return;
			}
		}
	}

	
}
