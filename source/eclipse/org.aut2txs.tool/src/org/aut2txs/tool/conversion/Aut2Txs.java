package org.aut2txs.tool.conversion;

import java.io.PrintStream;
import java.util.*;

import org.aut2txs.tool.aldebaranLang.*;

public class Aut2Txs {
	private PrintStream out;
	private LabelMap labelMap;
	
	public Aut2Txs(PrintStream out) {
		this.out = out;
		
		labelMap = new LabelMap("p");
	}
	
	private void print(String format, Object... args) {
		out.println(String.format(format, args));
	}
	
	public void print(File file) {
		labelMap.clear();
		labelMap.addFile(file);
		
		String chanList;
		
		if (labelMap.size() > 0) {
			List<String> chanNames = new ArrayList<String>(labelMap.getChannels());
			print("CHANDEF Chans ::=");
			print("\t  %s", getChanNameText(chanNames.get(0)));
			chanList = chanNames.get(0);
			
			for (int index = 1; index < chanNames.size(); index++) {
				print("\t, %s", getChanNameText(chanNames.get(index)));
				chanList += "," + chanNames.get(index);
			}
			
			print("ENDDEF");
			print("");
		} else {
			chanList = "";
		}
		
		print("PROCDEF p [%s] (state :: Int) ::=", chanList);
		
		if (file.getTransitions().size() > 0) {
			print("\t   %s", getTransitionText(file.getTransitions().get(0), chanList));
			
			for (int index = 1; index < file.getTransitions().size(); index++) {
				print("\t## %s", getTransitionText(file.getTransitions().get(index), chanList));
				
			}
		} else {
			print("\tSTOP");
		}
		
		print("ENDDEF");
		print("");
		
		print("MODELDEF Model");
		print("CHAN IN");
		print("CHAN OUT %s", chanList);
		print("BEHAVIOR p [%s] (0)", chanList);
		print("ENDDEF");
	}
	
	private String getChanNameText(String chanName) {
		return String.format("%s -- Generated from \"%s\"", chanName, labelMap.backwards(chanName));
	}
	
	private String getTransitionText(Transition transition, String chanList) {
		return String.format("%s [state == %d] >-> p [%s] (%d)", labelMap.getOrCreate(transition.getLabel()), transition.getStartState(), chanList, transition.getEndState());
	}
}
