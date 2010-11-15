package com.bungleton.coreen.as;

import java.util.LinkedList;
import java.util.List;

import flex2.compiler.config.ConfigurationException;
import flex2.compiler.config.ConfigurationInfo;
import flex2.compiler.config.ConfigurationValue;
import flex2.tools.ToolsConfiguration;

public class CoreenASReaderConfiguration extends ToolsConfiguration {

	@Override
	protected String getTargetFile() {
		return null;
	}

	//
	// 'read-sources' option
	//

	private List sources = new LinkedList();

	public List getSourcePath() {
		return sources;
	}

	public void cfgReadSources(ConfigurationValue cv, List args)
			throws ConfigurationException {
		sources.addAll(args);
	}

	public static ConfigurationInfo getDocSourcesInfo() {
		return new ConfigurationInfo(-1, new String[] { "path-element" }) {
			@Override
			public boolean allowMultiple() {
				return true;
			}

			@Override
			public boolean isPath() {
				return true;
			}
		};
	}

	//
	// 'load-config' p[topm
	//

    // dummy, ignored - pulled out of the buffer
    public void cfgLoadConfig(ConfigurationValue cv, String filename) throws ConfigurationException
    {
    }

    public static ConfigurationInfo getLoadConfigInfo()
    {
        return new ConfigurationInfo( 1, "filename" )
        {
            @Override
			public boolean allowMultiple()
            {
                return true;
            }
        };
    }

}
