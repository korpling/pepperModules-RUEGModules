package org.corpus_tools.rueg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.ByteArrayAssert;
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
	private static final String NEW_MSG_PATTERN = "[0-9][0-9]\\.[0-9][0-9]\\.[0-9][0-9], [0-9][0-9]:[0-9][0-9] - ";
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		if (getDocument() == null) {
			setDocument(SaltFactory.createSDocument());
		}
		if (getDocument().getDocumentGraph() == null) { 
			getDocument().setDocumentGraph( SaltFactory.createSDocumentGraph() );
		}
		try {
			read();
		} catch (IOException e) {
			throw new PepperModuleDataException(this, e.getMessage(), e);
		}
		unify();
		return (DOCUMENT_STATUS.COMPLETED);
	}
	
	private void read() throws IOException {
		byte[] data = Files.readAllBytes( Paths.get( getResourceURI().toFileString() ));
		String text = new String(data);
		SDocumentGraph graph = getDocument().getDocumentGraph();
		List<SToken> messageTokens = new ArrayList<>();
		int m = 1;
		for (String message : text.split(NEW_MSG_PATTERN)) {
			if (hasSpeaker(message)) {
				int l = 1;
				for (String line : dropSpeaker(message).split( "\n|\r" )) {					
					String messageText = line.trim();
					if (!messageText.isEmpty()) {
						System.out.println(messageText);
						List<SToken> tokens = graph.createTextualDS(messageText).tokenize();
						messageTokens.addAll(tokens);
						graph.createSpan(tokens).createAnnotation(null, ANNO_NAME_LINE, Integer.toString(l++));
					}
				}
				if (!messageTokens.isEmpty()) {
					graph.createSpan(messageTokens).createAnnotation(null, ANNO_NAME_MESSAGE, Integer.toString(m++));  //TODO use time stamp instead
					messageTokens.clear();
				}
			}
		}
	}
	
	/** 
	 * This function removes the messages "SPEAKER_NAME : "-prefix.
	 * @param prefixedMessage
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
	
	private void unify() {
		SDocumentGraph graph = getDocument().getDocumentGraph();
		List<STextualDS> datasources = graph.getTextualDSs();	
		String fullText = String.join(" ", datasources.stream().map((STextualDS ds) -> ds.getText()).collect(Collectors.toList()));
		STextualDS targetDS = SaltFactory.createSTextualDS();
		targetDS.setText(fullText);
		int offset = 0;
		Set<STextualDS> removableNodes = new HashSet<>();
		for (STextualDS ds : datasources) {
			final int o = offset;
			graph.getTokensBySequence(new DataSourceSequence<Integer>(ds, 0, ds.getEnd())).stream().forEach((SToken t) -> reassignToken(t, targetDS, o));
			offset += 1 + ds.getText().length();
			removableNodes.add(ds);
		}
		removableNodes.stream().forEach(graph::removeNode);
		graph.addNode(targetDS);
	}
	
	private void reassignToken(SToken sToken, STextualDS targetDS, int offset) {
		STextualRelation textRel = (STextualRelation) sToken.getOutRelations().stream().filter((SRelation r) -> r instanceof STextualRelation).findFirst().get();
		textRel.setTarget(targetDS);
		textRel.setStart(textRel.getStart() + offset);
		textRel.setEnd(textRel.getEnd() + offset);
	}
}
