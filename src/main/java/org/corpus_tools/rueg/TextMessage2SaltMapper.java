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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextMessage2SaltMapper extends PepperMapperImpl implements PepperMapper {
	private static final Logger logger = LoggerFactory.getLogger(TextMessage2SaltMapper.class);
	private static final String ANNO_NAME_MESSAGE = "message";
	private static final String ANNO_NAME_LINE = "line";
	private static final String NEW_MSG_PATTERN = "(\\[)?[0-9][0-9]?(\\.|/)[0-9][0-9]?(\\.|/)(20)?[0-9][0-9](,| ÖS) [0-9]?[0-9]:[0-9][0-9](:[0-9][0-9])?(\\])? (- )?";
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		if (getDocument() == null) {
			setDocument(SaltFactory.createSDocument());
		}
		if (getDocument().getDocumentGraph() == null) { 
			getDocument().setDocumentGraph( SaltFactory.createSDocumentGraph() );
		}
		try {
			unify( read() );
		} catch (IOException e) {
			throw new PepperModuleDataException(this, e.getMessage(), e);
		}
		return (DOCUMENT_STATUS.COMPLETED);
	}
	
	private List<STextualDS> read() throws IOException {
		byte[] data = Files.readAllBytes( Paths.get( getResourceURI().toFileString() ));
		String text = new String(data);
				
		SDocumentGraph graph = getDocument().getDocumentGraph();
		List<SToken> messageTokens = new ArrayList<>();
		List<STextualDS> orderedDataSources = new ArrayList<>();
		int m = 1;
		for (String message : text.split(NEW_MSG_PATTERN)) {
			if (hasSpeaker(message)) {
				for (String line : dropSpeaker(message).split( "\n|\r" )) {					
					String messageText = line.trim();
					if (!messageText.isEmpty()) {
						String rawText = messageText;
						STextualDS ds = graph.createTextualDS( freeEmojis(messageText) );
						ds.setName("dipl");
						orderedDataSources.add(ds);
						List<SToken> tokens = ds.tokenize();
						messageTokens.addAll(tokens);
						SSpan span = graph.createSpan(tokens);
						span.createAnnotation(null, ANNO_NAME_LINE, rawText);
					}
				}
				if (!messageTokens.isEmpty()) {
					graph.createSpan(messageTokens).createAnnotation(null, ANNO_NAME_MESSAGE, Integer.toString(m++));  //TODO use time stamp instead
					messageTokens.clear();
				}
			}
		}
		return orderedDataSources;
	}
	
	private static final String EMOJI_RANGE = "[\\x{1F600}-\\x{1F64F}]";
	
	/** 
	 * Separates emojis from other text with a space. 
	 * @param text
	 * @return
	 */
	private String freeEmojis(String text) {
		return text
				.replaceAll("([^ ])(" + EMOJI_RANGE + ")", "$1 $2")
				.replaceAll("(" + EMOJI_RANGE + ")([^ ])", "$1 $2");
	}
	
	/** 
	 * This function removes the messages "SPEAKER_NAME : "-prefix.
	 * @param prefixedMessage[\u1F600-\u1F64F]
	 * @return
	 */
	private String dropSpeaker(String prefixedMessage) {
		String[] arr = prefixedMessage.split(": ", 2);
		return arr.length == 2? arr[1] : arr[0];
	}
	
	/**
	 * Determine whether the provided message is an internal message or has been authored by a user.
	 * @param messageLine
	 * @return
	 */
	private boolean hasSpeaker(String messageLine) {
		return messageLine.contains(": ");
	}
	
	private void unify(List<STextualDS> orderedDataSources) {
		SDocumentGraph graph = getDocument().getDocumentGraph();
		List<STextualDS> datasources = graph.getTextualDSs();	
		String fullText = String.join(" ", datasources.stream().map((STextualDS ds) -> ds.getText()).collect(Collectors.toList()));
		STextualDS targetDS = SaltFactory.createSTextualDS();
		targetDS.setName("dipl");
		targetDS.setText(fullText);
		int offset = 0;
		for (STextualDS ds : orderedDataSources) {
			final int o = offset;
			graph.getTokensBySequence(new DataSourceSequence<Integer>(ds, 0, ds.getEnd())).stream().forEach((SToken t) -> reassignToken(t, targetDS, o));
			offset += 1 + ds.getText().length();
		}
		orderedDataSources.stream().forEach(graph::removeNode);
		graph.addNode(targetDS);
	}
	
	private void reassignToken(SToken sToken, STextualDS targetDS, int offset) {
		STextualRelation textRel = (STextualRelation) sToken.getOutRelations().stream().filter((SRelation r) -> r instanceof STextualRelation).findFirst().get();
		textRel.setTarget(targetDS);
		textRel.setStart(textRel.getStart() + offset);
		textRel.setEnd(textRel.getEnd() + offset);
	}
}
