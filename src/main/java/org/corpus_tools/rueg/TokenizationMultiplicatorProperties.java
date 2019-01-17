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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.PepperModuleProperty;

import com.ctc.wstx.util.StringUtil;

public class TokenizationMultiplicatorProperties extends PepperModuleProperties {
	/**
	 * Provide tokenization names as comma-separated list. The number of names
	 * determines also the number of tokenizations, unless a name is mentioned
	 * multiple times. The first mentioned name will be used for the existing
	 * tokens.
	 */
	private static final String PROP_TOK_NAMES = "tok.names";

	private static final String PROP_ADD_LANGUAGE = "add.language";

	public TokenizationMultiplicatorProperties() {
		addProperty(PepperModuleProperty.create().withName(PROP_TOK_NAMES).withType(String.class).withDescription(
				"Provide tokenization names as comma-separated list. The number of names determines also the number of tokenizations, unless a name is mentioned multiple times. The first mentioned name will be used for the existing tokens.")
				.isRequired(true).build());

		addProperty(PepperModuleProperty.create().withName(PROP_ADD_LANGUAGE).withType(Boolean.class)
				.withDescription("If true add the \"language\" token annotation (derived from document name).")
				.withDefaultValue(true).build());
	}

	public List<String> getTokenizationNames() {
		String value = StringUtils.strip((String) getProperty(PROP_TOK_NAMES).getValue(), "{}");
		return (new ArrayList<String>(new HashSet<String>(Arrays.asList(value.split("( )*,( )*")))));
	}
	
	public boolean isAddLanguage() {
		return (Boolean) getProperty(PROP_ADD_LANGUAGE).getValue();
	}
}
