package com.bungleton.coreen.as;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import flash.localization.LocalizationManager;
import flash.localization.ResourceBundleLocalizer;
import flash.localization.XLRLocalizer;
import flex2.compiler.CompilerAPI;
import flex2.compiler.CompilerException;
import flex2.compiler.CompilerSwcContext;
import flex2.compiler.FileSpec;
import flex2.compiler.ResourceContainer;
import flex2.compiler.Source;
import flex2.compiler.SourceList;
import flex2.compiler.SourcePath;
import flex2.compiler.SubCompiler;
import flex2.compiler.as3.As3Compiler;
import flex2.compiler.as3.binding.BindableExtension;
import flex2.compiler.as3.managed.ManagedExtension;
import flex2.compiler.common.CompilerConfiguration;
import flex2.compiler.config.ConfigurationBuffer;
import flex2.compiler.config.ConfigurationException;
import flex2.compiler.io.VirtualFile;
import flex2.compiler.swc.SwcCache;
import flex2.compiler.util.MimeMappings;
import flex2.compiler.util.NameMappings;
import flex2.compiler.util.ThreadLocalToolkit;
import flex2.tools.CompcPreLink;
import flex2.tools.Mxmlc;

public class CoreenASReader {

	public static void main(String[] args) throws Exception {
		CompilerAPI.useAS3();

		ConfigurationBuffer cfgbuf = new ConfigurationBuffer(
				CoreenASReaderConfiguration.class, new HashMap<String, String>());

		args = new String[]{
		  "-load-config+=/Users/charlie/dev/ooo-libs/lib/flex_sdk_4.1.0.16076/frameworks/flex-config.xml",
	      "+flexlib=/Users/charlie/dev/ooo-libs/lib/flex_sdk_4.1.0.16076/frameworks",
	      "-load-config+=/Users/charlie/dev/ooo-libs/aspirin/etc/flex-config.xml"};

		// setup the path resolver
		CompilerAPI.usePathResolver();

		// set up for localizing messages
		LocalizationManager l10n = new LocalizationManager();
		l10n.addLocalizer(new XLRLocalizer());
		l10n.addLocalizer(new ResourceBundleLocalizer());
		ThreadLocalToolkit.setLocalizationManager(l10n);

		// setup a console-based logger...
		CompilerAPI.useConsoleLogger();

		// Setting defaultVar as "no-default-arg" - which is a nonexistent
		// variable.
		// This is to avoid assertion errors when configurations are parsed.
		CoreenASReaderConfiguration configuration = (CoreenASReaderConfiguration) Mxmlc
				.processConfiguration(l10n, "coreenreader", args, cfgbuf,
						CoreenASReaderConfiguration.class, "no-default-arg");

		CompilerAPI.useConsoleLogger(true, true, configuration.getWarnings(),
				true);

		createCoreenXML(configuration, l10n);
	}



	public static void createCoreenXML(CoreenASReaderConfiguration configuration, LocalizationManager l10n)
			throws ConfigurationException, CompilerException
	{
		CompilerAPI.setupHeadless(configuration);

		// set to true so source file takes precedence over source from swc.
		CompilerAPI.setSkipTimestampCheck(true);

		String[] sourceMimeTypes = { MimeMappings.AS };

		CompilerConfiguration compilerConfig = configuration.getCompilerConfiguration();

		// asdoc should always have -doc=true so that it emits doc info. If it is false force it back to true.
		if(!compilerConfig.doc())
		{
		    compilerConfig.cfgDoc(null, true);
		}

		// create a SourcePath...
		SourcePath sourcePath = new SourcePath(sourceMimeTypes, compilerConfig.allowSourcePathOverlap());
		sourcePath.addPathElements( compilerConfig.getSourcePath() );
		System.out.println(configuration.getSourcePath());

		List<VirtualFile>[] files = CompilerAPI.getVirtualFileList(configuration.getSourcePath(),
				Collections.<VirtualFile>emptySet(),
                Sets.newHashSet(sourceMimeTypes),
                sourcePath.getPaths(), Lists.newArrayList());

		NameMappings mappings = CompilerAPI.getNameMappings(configuration);

		//	get standard bundle of compilers, transcoders
		As3Compiler asc = new As3Compiler(compilerConfig);
		SubCompiler[] compilers = { asc };

		// create a FileSpec... can reuse based on appPath, debug settings, etc...
		FileSpec fileSpec = new FileSpec(files[0], sourceMimeTypes, false);
		System.out.println(fileSpec.retrieveSources());

        // create a SourceList...
        SourceList sourceList = new SourceList(files[1], compilerConfig.getSourcePath(), null,
        									   flex2.tools.WebTierAPI.getSourceListMimeTypes(), false);
        System.out.println(sourceList.retrieveSources());

		ResourceContainer resources = new ResourceContainer();

		// set up the compiler extension which writes out toplevel.xml
		CoreenFlexCompilerExtension ext = new CoreenFlexCompilerExtension();
		asc.addCompilerExtension(ext);

		String gendir = (compilerConfig.keepGeneratedActionScript()? compilerConfig.getGeneratedDirectory() : null);
		asc.addCompilerExtension(new BindableExtension(gendir, compilerConfig.getGenerateAbstractSyntaxTree(),true) );
		asc.addCompilerExtension(new ManagedExtension(gendir, compilerConfig.getGenerateAbstractSyntaxTree(),true) );

		if (ThreadLocalToolkit.getBenchmark() != null)
		{
			ThreadLocalToolkit.getBenchmark().benchmark(l10n.getLocalizedTextString(new flex2.tools.Mxmlc.InitialSetup()));
		}

		// load SWCs
		CompilerSwcContext swcContext = new CompilerSwcContext();
		SwcCache cache = new SwcCache();

		// lazy read should only be set by mxmlc/compc/asdoc
		cache.setLazyRead(true);
		// for asdoc the theme and include-libraries values have been purposely not passed in below.
		swcContext.load(compilerConfig.getLibraryPath(),
				compilerConfig.getExternalLibraryPath(), null, null, mappings,
				null, cache);
		configuration.addExterns( swcContext.getExterns() );

		System.out.println("Validating compilation units");
		// validate CompilationUnits in FileSpec and SourcePath
		CompilerAPI.validateCompilationUnits(fileSpec, sourceList, sourcePath, null, resources, swcContext,
													null, false, configuration);

		Map licenseMap = configuration.getLicensesConfiguration().getLicenseMap();

		System.out.println("Compiling");
		// we call compileSwc (and use CompcPreLink, both of which should probably be renamed) to "compile" ASDoc.
		// everything runs through the normal compilation route, but we discard all of the output other than
		// what ASDocExtension creates
		CompilerAPI.compile(fileSpec, sourceList,
				Collections.<Source> emptyList(), sourcePath, resources, null,
				swcContext, mappings, configuration, compilers,
				new CompcPreLink(null, null), licenseMap,
				new ArrayList<Source>());
		System.out.println("Done");

	}
}
