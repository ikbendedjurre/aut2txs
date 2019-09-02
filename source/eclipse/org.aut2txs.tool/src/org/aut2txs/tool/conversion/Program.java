package org.aut2txs.tool.conversion;

import java.util.*;

import org.aut2txs.tool.aldebaranLang.File;
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
			
			if (obj instanceof File) {
				Aut2Txs converter = new Aut2Txs(System.out);
				converter.print((File)obj);
			}
		}
	}
}
