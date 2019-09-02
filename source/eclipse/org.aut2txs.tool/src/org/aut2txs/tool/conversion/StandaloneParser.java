package org.aut2txs.tool.conversion;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.aut2txs.tool.AldebaranLangStandaloneSetup;
import org.aut2txs.tool.aldebaranLang.AldebaranLangPackage;
import org.aut2txs.tool.log.Log;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.*;
import org.eclipse.emf.ecore.resource.*;
import org.eclipse.emf.ecore.resource.Resource.Diagnostic;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ocl.xtext.oclinecore.OCLinEcoreStandaloneSetup;
import org.eclipse.xtext.resource.*;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.*;

import com.google.inject.Injector;

public class StandaloneParser {
	private XtextResourceSet resourceSet;
	private IResourceValidator validator;
	
	private final static Map<String, Object> LOAD_OPTIONS = createLoadOptions();
	
	private static Map<String, Object> createLoadOptions() {
		Map<String, Object> result = new HashMap<String, Object>();
		result.put(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		return result;
	}
	
	public StandaloneParser() {
		OCLinEcoreStandaloneSetup.doSetup();
		
		Injector injector = new AldebaranLangStandaloneSetup().createInjectorAndDoEMFRegistration();
		validator = injector.getInstance(IResourceValidator.class);
		resourceSet = injector.getInstance(XtextResourceSet.class);
		
		EPackage p = AldebaranLangPackage.eINSTANCE;
		resourceSet.getPackageRegistry().put(p.getNsURI(), p);
	}
	
	public XtextResourceSet getResourceSet() {
		return resourceSet;
	}
	
	private Resource registerResource(String filename) {
		File file = new File(filename);
		
		if (!file.exists() || !file.isFile()) {
			Log.projectError("Could not locate model \"%s\"!", file.getAbsolutePath());
			return null;
		}
		
		URI fileURI = URI.createFileURI(file.getAbsolutePath());
		Resource result = resourceSet.getResource(fileURI, false);
		
		if (result == null) {
			Log.projectInfo("Loading model \"%s\"...", filename);
			result = resourceSet.createResource(fileURI);
		}
		
		return result;
	}
	
	public List<EObject> parse(String filename) {
		Resource resource = registerResource(filename);
		List<EObject> emptyResult = new ArrayList<EObject>();
		
		if (resource == null) {
			return emptyResult;
		}
		
		try {
			resource.load(LOAD_OPTIONS);
			EcoreUtil.resolveAll(resource);
			Log.projectInfo("Model \"%s\" loaded successfully!", filename);
		} catch (IOException e) {
			Log.projectInfo("Failed to load model \"%s\" (\"%s\")!", filename, e.getMessage());
			return emptyResult;
		}
		
		if (resource.isLoaded() && resource.getErrors().size() > 0) {
			for (Diagnostic d : resource.getErrors()) {
				Log.mainFileError(d.getMessage(), d);
			}
			
			for (Diagnostic d : resource.getWarnings()) {
				Log.mainFileWarning(d.getMessage(), d);
			}
			
			return emptyResult;
		}
		
		List<Issue> issues = validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
		
		if (issues.size() > 0) {
			for (Issue issue : issues) {
				Log.mainFileError(issue.getMessage(), issue);
			}
			
			return emptyResult;
		}
		
		Log.projectInfo("Model \"%s\" validated successfully!", filename);
		return resource.getContents();
	}
}
