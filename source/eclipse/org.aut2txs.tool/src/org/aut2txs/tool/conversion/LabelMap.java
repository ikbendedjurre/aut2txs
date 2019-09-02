package org.aut2txs.tool.conversion;

import java.util.*;

import org.aut2txs.tool.aldebaranLang.File;
import org.aut2txs.tool.aldebaranLang.Transition;

public class LabelMap {
	private Map<String, String> aut2txs;
	private Map<String, String> txs2aut;
	private Set<String> reserved;
	
	public LabelMap(String... reservedWords) {
		clear();
		reserved = new HashSet<String>();
		
		for (String reservedWord : KEYWORDS) {
			reserved.add(reservedWord);
		}
		
		for (String reservedWord : reservedWords) {
			reserved.add(reservedWord);
		}
	}
	
	public void clear() {
		aut2txs = new HashMap<String, String>();
		txs2aut = new HashMap<String, String>();
	}
	
	private final static String[] KEYWORDS = new String[] {
			"EXIT",
			"HIDE",
			"LET",
			"IN",
			"NI",
			"STOP",
			"PROCDEF",
			"CHANDEF",
			"MODELDEF",
			"ENDDEF",
			"IN",
			"OUT",
			"SYNC",
			"CHAN",
			"BEHAVIOUR",
			"IF",
			"THEN",
			"ELSE",
			"ENDIF",
			"STAUTDEF",
			"INIT",
			"VAR",
			"TRANS",
			"CONSTDEF",
			"CNECTDEF",
			"CLIENTSOCK",
			"SERVERSOCK",
			"HOST",
			"PORT",
			"ENCODE",
			"DECODE",
			"Int",
			"String",
			"Bool",
	};
	
	public int size() {
		return txs2aut.size();
	}
	
	public Collection<String> getChannels() {
		return txs2aut.keySet();
	}
	
	private static String makeTxsCompatible(String s) {
		String result = "";
		
		for (int index = 0; index < s.length(); index++) {
			char c = s.charAt(index);
			
			if (Character.isLetterOrDigit(c) || c == '_') {
				result += Character.toUpperCase(c);
			} else {
				if (index < s.length() - 1) {
					if (result.length() > 0 && result.charAt(result.length() - 1) != '_') {
						result += "_";
					}
				}
			}
		}
		
		if (result.length() > 0) {
			char c = result.charAt(0);
			
			if (Character.isLetter(c) || c == '_') {
				return result;
			}
			
			return "_" + result;
		}
		
		return "_";
	}
	
	private String makeUnique(Collection<String> cs, String s) {
		if (cs.contains(s)) {
			int suffix = 0;
			String result;
			
			do {
				suffix++;
				result = s + "_" + suffix;
			} while (cs.contains(result) || reserved.contains(result));
			
			return result;
		}
		
		return s;
	}
	
	public String getOrCreate(String autLabel) {
		String unquoteAutLabel = autLabel.substring(1, autLabel.length() - 1);
		String txsLabel = aut2txs.get(unquoteAutLabel);
		
		if (txsLabel != null) {
			return txsLabel;
		}
		
		String compatibleAutLabel = makeTxsCompatible(unquoteAutLabel);
		String uniqueAutLabel = makeUnique(txs2aut.keySet(), compatibleAutLabel);
		aut2txs.put(unquoteAutLabel, uniqueAutLabel);
		txs2aut.put(uniqueAutLabel, unquoteAutLabel);
		return uniqueAutLabel;
	}
	
	public String addTransition(Transition transition) {
		return getOrCreate(transition.getLabel());
	}
	
	public void addFile(File file) {
		for (Transition transition : file.getTransitions()) {
			addTransition(transition);
		}
	}
	
	public String backwards(String txsLabel) {
		return txs2aut.get(txsLabel);
	}
}
