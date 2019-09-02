package org.aut2txs.tool.conversion;

import java.io.PrintStream;
import java.util.*;

import org.aut2txs.tool.aldebaranLang.*;

public class Aut2Txs {
	private List<PrintStream> outs;
	private LabelMap labelMap;
	
	public Aut2Txs(PrintStream... out) {
		outs = new ArrayList<PrintStream>();
		
		for (PrintStream o : out) {
			outs.add(o);
		}
		
		labelMap = new LabelMap("p", "A", "state");
	}
	
	private void print(String format, Object... args) {
		String s = String.format(format, args);
		
		for (PrintStream o : outs) {
			o.println(s);
		}
	}
	
	public void print(File file) {
		labelMap.clear();
		labelMap.addFile(file);
		
		for (String chanName : labelMap.getChannels()) {
			print("-- \"%s\" originates from \"%s\"", chanName, labelMap.backwards(chanName));
		}
		
		print("");
		
		print("CHANDEF Chans ::=");
		print("\tA :: String");
		print("ENDDEF");
		print("");
		
		print("PROCDEF p [A :: String] (state :: Int) ::=");
		
		if (file.getTransitions().size() > 0) {
			print("\t   %s", getTransitionText(file.getTransitions().get(0)));
			
			for (int index = 1; index < file.getTransitions().size(); index++) {
				print("\t## %s", getTransitionText(file.getTransitions().get(index)));
				
			}
		} else {
			print("\tSTOP");
		}
		
		print("ENDDEF");
		print("");
		
		print("MODELDEF Model ::=");
		print("CHAN IN");
		print("CHAN OUT A");
		print("BEHAVIOUR p [A] (0)");
		print("ENDDEF");
	}
	
	private String getTransitionText(Transition transition) {
		return String.format("A ! \"%s\" [[ state == %d ]] >-> p [A] (%d)", labelMap.getOrCreate(transition.getLabel()), transition.getStartState(), transition.getEndState());
	}
}
