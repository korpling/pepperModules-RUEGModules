/**
 * Copyright 2016 Humboldt-Universität zu Berlin.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
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
