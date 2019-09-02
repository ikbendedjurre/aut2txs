package org.aut2txs.tool.conversion;

import java.io.*;
import java.util.*;

import org.eclipse.emf.ecore.EObject;

public class Program {
	public static void main(String[] args) {
		if (args.length != 1) {
			throw new Error("Provide exactly 1 filename please!");
		}
		
		StandaloneParser p = new StandaloneParser();
		List<EObject> r = p.parse(args[0]);
		
		System.out.println("Found " + r.size() + " objects:");
		
		for (EObject obj : r) {
			System.out.println("\tFound: " + obj.getClass().getName());
			
			if (obj instanceof org.aut2txs.tool.aldebaranLang.File) {
				try {
					File destFile = new File(args[0] + ".txs");
					PrintStream ps = new PrintStream(destFile);
					
					Aut2Txs converter = new Aut2Txs(System.out, ps);
					converter.print((org.aut2txs.tool.aldebaranLang.File)obj);
					
					ps.flush();
					ps.close();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					
				}
			}
		}
	}
}
