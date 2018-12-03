package org.corpus_tools.rueg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SMedialRelation;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STextualRelation;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.STimelineRelation;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SRelation;

/**
 * This mapper simply multiplicates a tokenization to please the EXMARaLDA
 * exporter. It is thus not a generally applicable tool (yet).
 * 
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
		createTimelineFromText(graph);
		{
			datasourceByName.put(modelName, graph.getTextualDSs().get(0));
			String text = datasourceByName.get(modelName).getText();
			for (String name : names) {
				STextualDS ds = graph.createTextualDS(text);
				ds.setName(name);
				datasourceByName.put(name, ds);
			}
			for (String name : datasourceByName.keySet()) {
				STextualDS ds = datasourceByName.get(name);
				if(ds.containsLabel("exmaralda::speaker")) {
					ds.removeLabel("exmaralda::speaker");
				}
				ds.createFeature("exmaralda", "speaker", name);
				getDocument().createMetaAnnotation(name, "abbreviation", name);
				getDocument().createMetaAnnotation(name, "sex", "u");
			}
		}
		for (STextualRelation modelTokenRelation : getSortedTextualRelations()) {
			for (String name : names) {
				SToken namedToken = graph.createToken(datasourceByName.get(name), modelTokenRelation.getStart(),
						modelTokenRelation.getEnd());
				{ // represent
					SToken previousToken = previousTokenByName.get(name);
					if (previousToken != null) {
						graph.createRelation(previousToken, namedToken, SALT_TYPE.SORDER_RELATION, null).setType(name);
					}
					previousTokenByName.put(name, namedToken);
				}
				{ // align
					STimelineRelation modelTimeRel = getTimelineRelationByToken(modelTokenRelation.getSource());
					STimelineRelation timeRel = graph.createTimelineRelation(namedToken, 1);
					timeRel.setStart(modelTimeRel.getStart());
					timeRel.setEnd(modelTimeRel.getEnd());
					
					SMedialRelation modelMediaRel = getMedialRelation(modelTokenRelation.getSource());
					if(modelMediaRel != null) {
						SMedialRelation mediaRel = SaltFactory.createSMedialRelation();
						mediaRel.setSource(namedToken);
						mediaRel.setTarget(modelMediaRel.getTarget());
						mediaRel.setStart(modelMediaRel.getStart());
						mediaRel.setEnd(modelMediaRel.getEnd());
						graph.addRelation(mediaRel);
					}
				}
			}
		}
		return DOCUMENT_STATUS.COMPLETED;
	}

	private STimeline createTimelineFromText(SDocumentGraph g) {
		STimeline retVal = null;
		if ((g.getTimeline() == null) || (g.getTimeline().getEnd() == 0)) {
			STimeline sTimeline = SaltFactory.createSTimeline();
			g.addNode(sTimeline);
			List<STimelineRelation> sTimeRelList = new ArrayList<>();
			Map<STextualDS, List<STimelineRelation>> sTimeRelTable = new Hashtable<>();
			for (STextualRelation sTextRel : g.getTextualRelations()) {
				// for each token create a STimeline object
				STimelineRelation sTimeRel = SaltFactory.createSTimelineRelation();
				sTimeRel.setTarget(sTimeline);
				sTimeRel.setSource(sTextRel.getSource());

				// start: put STimelineRelation into sTimeRelTable
				if (sTimeRelTable.get(sTextRel.getTarget()) == null) {
					sTimeRelTable.put(sTextRel.getTarget(), new ArrayList<STimelineRelation>());
				}
				// TODO not only adding the timeRel, sorting for left and right
				// textual position
				sTimeRelTable.get(sTextRel.getTarget()).add(sTimeRel);
				// end: put STimelineRelation into sTimeRelTable
			} // for each token create a STimeline object
			for (STextualDS sTextualDS : g.getTextualDSs()) {
				List<STimelineRelation> rels = sTimeRelTable.get(sTextualDS);
				if (rels != null) {
					sTimeRelList.addAll(sTimeRelTable.get(sTextualDS));
				}
			}
			Integer pot = 0;
			for (STimelineRelation sTimeRelation : sTimeRelList) {
				sTimeRelation.setStart(pot);
				pot++;
				sTimeline.increasePointOfTime();
				sTimeRelation.setEnd(pot);
				g.addRelation(sTimeRelation);
			}
			retVal = sTimeline;
		} else {
			retVal = g.getTimeline();
		}

		return (retVal);
	}

	private List<STextualRelation> getSortedTextualRelations() {
		return getDocument().getDocumentGraph().getSortedTokenByText().stream()
				.map(TokenizationMultiplicatorMapper::getTextualRelationByToken).collect(Collectors.toList());
	}

	public static STextualRelation getTextualRelationByToken(SToken tok) {
		return (STextualRelation) tok.getOutRelations().stream().filter((SRelation r) -> r instanceof STextualRelation)
				.findFirst().get();
	}

	public static STimelineRelation getTimelineRelationByToken(SToken tok) {
		return (STimelineRelation) tok.getOutRelations().stream()
				.filter((SRelation r) -> r instanceof STimelineRelation).findFirst().orElse(null);
	}
	
	public static SMedialRelation getMedialRelation(SToken tok) {
		return (SMedialRelation) tok.getOutRelations().stream()
				.filter((SRelation r) -> r instanceof SMedialRelation).findFirst().get();
	}
}
