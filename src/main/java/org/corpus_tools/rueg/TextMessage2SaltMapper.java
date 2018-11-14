package org.corpus_tools.rueg;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleDataException;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextMessage2SaltMapper extends PepperMapperImpl implements PepperMapper {
	private static final Logger logger = LoggerFactory.getLogger(TextMessage2SaltMapper.class);
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
		Stream<String> data = Files.lines( Paths.get( getResourceURI().toFileString() ));
		data.forEachOrdered(this::extract);
		data.close();
	}
	
	private void extract(String message) {
		SDocumentGraph graph = getDocument().getDocumentGraph();
		String timestamp = message.substring(0, 15);
		String text = message.substring( 18 ).trim();
		STextualDS textualDS = graph.createTextualDS( dropSpeaker(text) );
		List<SToken> tokens = textualDS.tokenize();
		if (((TextMessageImporterProperties) getProperties()).annotateTimeStamps()) {
			graph.createSpan(tokens).createAnnotation(null, "timestamp", timestamp);
		}
	}
	
	/** 
	 * This function removes the messages "SPEAKER_NAME : "-prefix.
	 * @param message
	 * @return
	 */
	private String dropSpeaker(String message) {
		String[] arr = message.split(":", 2);
		return arr.length == 2? arr[1].trim() : arr[0];
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
