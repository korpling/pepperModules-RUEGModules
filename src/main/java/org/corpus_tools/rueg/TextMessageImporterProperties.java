package org.corpus_tools.rueg;

import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;

public class TextMessageImporterProperties extends PepperModuleProperties {
	/** Assign time stamp annotation to tokens contained in the respective message. */
	private static final String PROP_KEEP_TIME_STAMPS = "keep.timestamp";
	
	public TextMessageImporterProperties() {
		addProperty(PepperModuleProperty.create()
				.withName(PROP_KEEP_TIME_STAMPS)
				.withType(Boolean.class)
				.withDescription("Assign time stamp annotation to tokens contained in the respective message.")
				.withDefaultValue(true)
				.isRequired(false)
				.build());
	}
	
	/** 
	 * If true, the user desires the messages' time stamps to be kept as annotations.
	 * @return
	 */
	public boolean annotateTimeStamps() {
		return (Boolean) getProperty(PROP_KEEP_TIME_STAMPS).getValue();
	}
}
