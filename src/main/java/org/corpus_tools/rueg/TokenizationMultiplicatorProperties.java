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
	/** Provide tokenization names as comma-separated list. The number of names determines also the number of tokenizations, unless a name is mentioned multiple times. The first mentioned name will be used for the existing tokens. */
	private static final String PROP_TOK_NAMES = "tok.names";
	
	public TokenizationMultiplicatorProperties() {
		addProperty(
				PepperModuleProperty.create()
				.withName(PROP_TOK_NAMES)
				.withType(String.class)
				.withDescription("Provide tokenization names as comma-separated list. The number of names determines also the number of tokenizations, unless a name is mentioned multiple times. The first mentioned name will be used for the existing tokens.")
				.isRequired(true)
				.build());
	}
	
	public List<String> getTokenizationNames() {
		String value = StringUtils.strip((String) getProperty(PROP_TOK_NAMES).getValue(), "{}");
		return (new ArrayList<String>(new HashSet<String>(Arrays.asList(value.split("( )*,( )*")))));		
	}
}
