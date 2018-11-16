package org.corpus_tools.rueg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SRelation;

/** 
 * This mapper simply multiplicates a tokenization to please the EXMARaLDA exporter. It is thus not a generally applicable tool (yet).
 * @author klotzmaz
 *
 */
public class TokenizationMultiplicatorMapper extends PepperMapperImpl implements PepperMapper {
	@Override
	public DOCUMENT_STATUS mapSDocument() {
		List<String> names = ((TokenizationMultiplicatorProperties) getProperties()).getTokenizationNames();
		SDocumentGraph graph = getDocument().getDocumentGraph();
		Map<String, SToken> previousTokenByName = new HashMap<>();
		Map<String, STextualDS> datasourceByName = new HashMap<>();
		String modelName = names.get(0);
		names.remove(modelName);
		graph.createTimeline();
		{
			datasourceByName.put(modelName, graph.getTextualDSs().get(0));
			String text = datasourceByName.get(modelName).getText();
			for (String name : names) {
				datasourceByName.put(name, graph.createTextualDS( text ));
			}
			for (String name : datasourceByName.keySet()) {
				datasourceByName.get(name).createFeature("exmaralda", "speaker", name);
				getDocument().createMetaAnnotation(name, "abbreviation", name);
				getDocument().createMetaAnnotation(name, "sex", "u");
			}
		}
		for (STextualRelation modelTokenRelation : getSortedTextualRelations()) {
			for (String name : names) {
				SToken namedToken = graph.createToken(datasourceByName.get(name), modelTokenRelation.getStart(), modelTokenRelation.getEnd());
				{ //represent
					SToken previousToken = previousTokenByName.get(name);
					if (previousToken != null) {
						graph.createRelation(previousToken, namedToken, SALT_TYPE.SORDER_RELATION, null).setType(name);
					}
					previousTokenByName.put(name, namedToken);
				}
				{ //align
					STimelineRelation modelTimeRel = getTimelineRelationByToken( modelTokenRelation.getSource() );
					STimelineRelation timeRel = graph.createTimelineRelation(namedToken, 1);
					timeRel.setStart( modelTimeRel.getStart() );
					timeRel.setEnd( modelTimeRel.getEnd() );
				}
			}
		}
		return DOCUMENT_STATUS.COMPLETED;
	}
	
	private List<STextualRelation> getSortedTextualRelations() {
		return getDocument().getDocumentGraph().getSortedTokenByText().stream().map(TokenizationMultiplicatorMapper::getTextualRelationByToken).collect(Collectors.toList());
	}

	public static STextualRelation getTextualRelationByToken(SToken tok) {
		return (STextualRelation) tok.getOutRelations().stream().filter((SRelation r) -> r instanceof STextualRelation).findFirst().get();
	}
	
	public static STimelineRelation getTimelineRelationByToken(SToken tok) {
		return (STimelineRelation) tok.getOutRelations().stream().filter((SRelation r) -> r instanceof STimelineRelation).findFirst().get();
	}
}
