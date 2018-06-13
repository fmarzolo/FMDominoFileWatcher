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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FMCommandLineParser {
	final Map<String, List<String>> params = new HashMap<>();

	List<String> options = null;
	Boolean error=false;
	
	public Boolean isError() {
		return error;
	}
	
	public String get(String arg, int pos) {
		return params.get(arg).get(pos).toString();
	}
	
	public FMCommandLineParser(String[] args) {
		init(args);
	}
	
	public FMCommandLineParser(String args) {
		String[] parameters = args.split("\\s+");
		init(parameters);
	}
	
	public void init(String[] args) {
	
		for (int i = 0; i < args.length; i++) {
			final String a = args[i];

			if (a.charAt(0) == '-') {
				if (a.length() < 2) {
					error=true;
					System.err.println("Error at argument " + a);
					return;
				}

				options = new ArrayList<>();
				params.put(a.substring(1), options);
			}
			else if (options != null) {
				options.add(a);
			}
			else {
				System.out.println("Illegal parameter usage");
				error=true;
				return;
			}
		}
	}

	
}