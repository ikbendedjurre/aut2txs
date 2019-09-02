package org.aut2txs.tool.log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.xtext.validation.Issue;

public class Log {
	public enum Verbosity {
		DETAIL("Detailed information", "Detail", 0, IMarker.SEVERITY_INFO),
		INFO("Most information", "Info", 1, IMarker.SEVERITY_INFO),
		WARNING("Errors and warnings", "Warning", 2, IMarker.SEVERITY_WARNING),
		ERROR("Errors only", "Error", 3, IMarker.SEVERITY_ERROR),
		
		;
		
		public final String description;
		public final String shortName;
		public final int value;
		public final int severityIMarker;
		
		private Verbosity(String description, String shortName, int value, int severityIMarker) {
			this.description = description;
			this.shortName = shortName;
			this.value = value;
			this.severityIMarker = severityIMarker;
		}
		
		public static Verbosity parse(String text) {
			for (Verbosity v : values()) {
				if (v.name().equals(text)) {
					return v;
				}
			}
			
			return null;
		}
	}
	
	private static Verbosity verbosity = Verbosity.WARNING;
	private static Set<OutputStream> outputStreams = getDefaultOutputStreams();
	private static OutputStream fileOutputStream = null;
	
	private static OutputStream getFileOutputStream() {
		if (fileOutputStream == null) {
			try {
				fileOutputStream = new FileOutputStream(new File("latest.log"));
			} catch (FileNotFoundException e) {
				fileOutputStream = System.out;
			}
		}
		
		return fileOutputStream;
	}
	
	private static Set<OutputStream> getDefaultOutputStreams() {
		Set<OutputStream> outputStreams = new HashSet<OutputStream>();
		outputStreams.add(getFileOutputStream());
		outputStreams.add(System.out);
		return outputStreams;
	}
	
	private static IResource currentProject = null;
	private static IResource currentMainFile = null;
	
	public static Verbosity getVerbosity() {
		return verbosity;
	}
	
	public static void setVerbosity(Verbosity verbosity) {
		Log.verbosity = verbosity;
	}
	
	public static Set<OutputStream> getOutputStreams() {
		return Log.outputStreams;
	}
	
	public static void setCurrentResources(IResource project, IResource mainFile) {
		currentProject = project;
		currentMainFile = mainFile;
	}
	
	public static void print(String format, Object... args) {
		byte[] output = String.format(format + "\n", args).getBytes(Charset.forName("UTF-8"));
		
		for (OutputStream outputStream : outputStreams) {
			try {
				outputStream.write(output);
			} catch (IOException e) {
				//Do nothing.
			}
		}
	}
	
	private static boolean checkVerbosity(Verbosity verbosity) {
		return verbosity.value >= Log.verbosity.value;
	}
	
	public static void print(Verbosity verbosity, Object source, String format, Object... args) {
		if (checkVerbosity(verbosity)) {
			String prefix = "";
			
			if (source instanceof Issue) {
				Issue issue = (Issue)source;
				prefix = String.format("%s at line %d, column %d: ", verbosity.shortName, issue.getLineNumber(), issue.getColumn());
			} else if (source instanceof Diagnostic) {
				Diagnostic diagnostic = (Diagnostic)source;
				String diagnosticLocation = diagnostic.getLocation() != null ? " in " + diagnostic.getLocation().toString() : "";
				prefix = String.format("%s%s at line %d, column %d: ", verbosity.shortName, diagnosticLocation, diagnostic.getLine(), diagnostic.getColumn());
			} else if (source instanceof Integer) {
				prefix = String.format("%s at line %d: ", verbosity.shortName, (Integer)source);
			}
			
			print(prefix + format, args);
		}
	}
	
	private static IMarker createMarker(IResource resource, Verbosity verbosity, String format, Object... args) {
		try {
			IMarker marker = resource.createMarker(IMarker.PROBLEM);
			marker.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_NORMAL);
			marker.setAttribute(IMarker.SEVERITY, verbosity.severityIMarker);
			marker.setAttribute(IMarker.MESSAGE, String.format(format, args));
			marker.setAttribute(IMarker.TRANSIENT, true);
			return marker;
		} catch (CoreException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private static void setMarkerPosition(IMarker marker, Object source) {
		try {
			if (source instanceof Issue) {
				Issue issue = (Issue)source;
				marker.setAttribute(IMarker.CHAR_START, issue.getOffset());
				marker.setAttribute(IMarker.CHAR_END, issue.getOffset() + issue.getLength());
			} else if (source instanceof Diagnostic) {
				marker.setAttribute(IMarker.LINE_NUMBER, ((Diagnostic)source).getLine());
			} else if (source instanceof Integer) {
				marker.setAttribute(IMarker.LINE_NUMBER, (Integer)source);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}
	
	public static IMarker createProjectMarker(Verbosity verbosity, String format, Object... args) {
		if (checkVerbosity(verbosity) && currentProject != null) {
			return createMarker(currentProject, verbosity, format, args);
		}
		return null;
	}
	
	public static IMarker createMainFileMarker(Verbosity verbosity, Object source, String format, Object... args) {
		if (checkVerbosity(verbosity) && currentMainFile != null) {
			setMarkerPosition(createMarker(currentMainFile, verbosity, format, args), source);
		}
		return null;
	}
	
	private static void projectMsg(Verbosity verbosity, String format, Object... args) {
		print(verbosity, null, format, args);
		createProjectMarker(verbosity, format, args);
	}
	
	private static void mainFileMsg(Verbosity verbosity, Object source, String format, Object... args) {
		print(verbosity, source, format, args);
		createMainFileMarker(verbosity, source, format, args);
	}
	
	public static void projectError(String format, Object... args) {
		projectMsg(Verbosity.ERROR, format, args);
	}
	
	public static void projectWarning(String format, Object... args) {
		projectMsg(Verbosity.WARNING, format, args);
	}
	
	public static void projectInfo(String format, Object... args) {
		projectMsg(Verbosity.INFO, format, args);
	}
	
	public static void projectDetail(String format, Object... args) {
		projectMsg(Verbosity.DETAIL, format, args);
	}
	
	public static void mainFileError(String format, Object source, Object... args) {
		mainFileMsg(Verbosity.ERROR, source, format, args);
	}
	
	public static void mainFileWarning(String format, Object source, Object... args) {
		mainFileMsg(Verbosity.WARNING, source, format, args);
	}
	
	public static void mainFileInfo(String format, Object source, Object... args) {
		mainFileMsg(Verbosity.INFO, source, format, args);
	}
	
	public static void mainFileDetail(String format, Object source, Object... args) {
		mainFileMsg(Verbosity.DETAIL, source, format, args);
	}
}
