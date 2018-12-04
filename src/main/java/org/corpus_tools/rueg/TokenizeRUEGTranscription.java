package org.corpus_tools.rueg;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.corpus_tools.pepper.common.DOCUMENT_STATUS;
import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperManipulatorImpl;
import org.corpus_tools.pepper.impl.PepperMapperImpl;
import org.corpus_tools.pepper.modules.PepperManipulator;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.pepper.modules.PepperModule;
import org.corpus_tools.pepper.modules.PepperModuleProperties;
import org.corpus_tools.pepper.modules.exceptions.PepperModuleNotReadyException;
import org.corpus_tools.salt.SALT_TYPE;
import org.corpus_tools.salt.SaltFactory;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.common.SDocumentGraph;
import org.corpus_tools.salt.common.SMedialRelation;
import org.corpus_tools.salt.common.SSpan;
import org.corpus_tools.salt.common.STextualDS;
import org.corpus_tools.salt.common.STimeline;
import org.corpus_tools.salt.common.SToken;
import org.corpus_tools.salt.core.SRelation;
import org.corpus_tools.salt.graph.Identifier;
import org.corpus_tools.salt.util.DataSourceSequence;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

/**
 * This is a dummy implementation to show how a {@link PepperManipulator} works.
 * Therefore it just prints out some information about a corpus like the number
 * of nodes, edges and for instance annotation frequencies. <br/>
 * This class can be used as a template for an own implementation of a
 * {@link PepperManipulator} Take a look at the TODO's and adapt the code. If
 * this is the first time, you are implementing a Pepper module, we strongly
 * recommend, to take a look into the 'Developer's Guide for Pepper modules',
 * you will find on
 * <a href="http://corpus-tools.org/pepper/">http://corpus-tools.org/pepper</a>.
 * 
 * @author Thomas Krause
 */
@Component(name = "TokenizeRUEGTranscriptionComponent", factory = "PepperManipulatorComponentFactory")
public class TokenizeRUEGTranscription extends PepperManipulatorImpl {
	// =================================================== mandatory
	// ===================================================
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * A constructor for your module. Set the coordinates, with which your module
	 * shall be registered. The coordinates (modules name, version and supported
	 * formats) are a kind of a fingerprint, which should make your module unique.
	 */
	public TokenizeRUEGTranscription() {
		super();
		setName("TokenizeRUEGTranscription");
		// TODO change suppliers e-mail address
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		// TODO change suppliers homepage
		setSupplierHomepage(URI.createURI(PepperConfiguration.HOMEPAGE));
		// TODO add a description of what your module is supposed to do
		setDesc("The manipulator, traverses over the document-structure and prints out some information about it, like the frequencies of annotations, the number of nodes and edges and so on. ");
	}

	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * This method creates a customized {@link PepperMapper} object and returns it.
	 * You can here do some additional initialisations. Thinks like setting the
	 * {@link Identifier} of the {@link SDocument} or {@link SCorpus} object and the
	 * {@link URI} resource is done by the framework (or more in detail in method
	 * {@link #start()}). The parameter <code>Identifier</code>, if a
	 * {@link PepperMapper} object should be created in case of the object to map is
	 * either an {@link SDocument} object or an {@link SCorpus} object of the mapper
	 * should be initialized differently. <br/>
	 * 
	 * @param Identifier {@link Identifier} of the {@link SCorpus} or
	 *                   {@link SDocument} to be processed.
	 * @return {@link PepperMapper} object to do the mapping task for object
	 *         connected to given {@link Identifier}
	 */
	public PepperMapper createPepperMapper(Identifier Identifier) {
		Mapper mapper = new Mapper();
		return (mapper);
	}

	/**
	 * This class is a dummy implementation for a mapper, to show how it works.
	 * Pepper or more specific this dummy implementation of a Pepper module creates
	 * one mapper object per {@link SDocument} object and {@link SCorpus} object
	 * each. This ensures, that each of those objects is run independently from
	 * another and runs parallelized. <br/>
	 * The method {@link #mapSCorpus()} is supposed to handle all {@link SCorpus}
	 * object and the method {@link #mapSDocument()} is supposed to handle all
	 * {@link SDocument} objects. <br/>
	 * In our dummy implementation, we just print out some information about a
	 * corpus to system.out. This is not very useful, but might be a good starting
	 * point to explain how access the several objects in Salt model.
	 */
	public static class Mapper extends PepperMapperImpl {
		/**
		 * Creates meta annotations, if not already exists
		 */
		@Override
		public DOCUMENT_STATUS mapSCorpus() {
			return (DOCUMENT_STATUS.COMPLETED);
		}

		/**
		 * prints out some information about document-structure
		 */
		@Override
		public DOCUMENT_STATUS mapSDocument() {

			SDocumentGraph g = getDocument().getDocumentGraph();

			// rename the speaker to "dipl"
			if (!g.getTextualDSs().isEmpty()) {
				g.getTextualDSs().get(0).setName("dipl");
			}

			List<SToken> originalToken = new LinkedList<>(g.getTokens());
			for (SToken utteranceToken : originalToken) {

				List<DataSourceSequence> utteranceSeqList = g.getOverlappedDataSourceSequence(utteranceToken,
						SALT_TYPE.STEXT_OVERLAPPING_RELATION);

				if (!utteranceSeqList.isEmpty()) {
					// tokenize the token value (by getting new indexes for the token based on
					// the same base text)
					List<DataSourceSequence<Integer>> tokenizedUtterance = this.tokenize(utteranceSeqList.get(0));

					// add token and connect them with a newly created span
					TreeMap<Integer, SToken> sortedTokenForUtterance = new TreeMap<>();
					for (DataSourceSequence tokenSeq : tokenizedUtterance) {
						SToken newToken = g.createToken(tokenSeq);
						sortedTokenForUtterance.put(tokenSeq.getStart().intValue(), newToken);
					}
					List<SToken> tokenForUtterance = new ArrayList<>(sortedTokenForUtterance.values());

					SSpan utteranceSpan = g.createSpan(tokenForUtterance);
					if (utteranceSpan != null) {
						// add original token value as annotation value
						utteranceSpan.createAnnotation("rueg", "cu", g.getText(utteranceToken));

						// align the new tokens with the audio file (using interpolation)
						for (SRelation out : utteranceToken.getOutRelations()) {
							if (out instanceof SMedialRelation) {
								SMedialRelation mediaRel = (SMedialRelation) out;
								// Don't copy the media relation, but use an interpolation based on the text
								// length to add media
								// relations to the new tokens. Overlapped tokens should be sorted by their text
								// order.
								final double oldMediaLength = mediaRel.getEnd() - mediaRel.getStart();

								double currentStart = mediaRel.getStart();

								// get text length of all new nodes: tokenization can remove characters
								double newTextLength = 0.0;
								for (SToken tok : tokenForUtterance) {
									newTextLength += (double) g.getText(tok).length();
								}
								if (newTextLength == 0.0) {
									// avoid division by zero
									newTextLength = 0.00001;
								}

								for (int i = 0; i < tokenForUtterance.size(); i++) {
									SToken tok = tokenForUtterance.get(i);
									double newTokTextLength = (double) g.getText(tok).length();
									double newTokMediaLength = (newTokTextLength / newTextLength) * oldMediaLength;

									SMedialRelation newTokMediaRel = SaltFactory.createSMedialRelation();
									newTokMediaRel.setSource(tok);
									newTokMediaRel.setTarget(mediaRel.getTarget());
									newTokMediaRel.setStart(currentStart);
									if (i < tokenForUtterance.size() - 1) {
										newTokMediaRel.setEnd(currentStart + newTokMediaLength);
									} else {
										// assign all remaining time to the lasts token to avoid any missing time due to
										// fractions
										newTokMediaRel.setEnd(mediaRel.getEnd());
									}
									g.addRelation(newTokMediaRel);

									currentStart = currentStart + newTokMediaLength;
								}
							}
						}
					}

					// remove the old token
					g.removeNode(utteranceToken);
				}
			}

			// The timeline is invalid after removing all original token
			STimeline existingTimeline = g.getTimeline();
			if (existingTimeline != null) {
				g.removeNode(existingTimeline);
			}

			return (DOCUMENT_STATUS.COMPLETED);
		}

		private final Pattern nonWhiteSpacePattern = Pattern.compile("^(?<token>(\\S+)).*");

		private boolean checkLookahead(String wholeText, int rangeStart, int rangeEnd, String searchText) {

			if (rangeStart + searchText.length() > rangeEnd) {
				// Cannot match the text if the the search does not fit in the rest of the whole
				// text
				return false;
			}

			// check if all characters match
			for (int i = 0; i < searchText.length() && rangeStart + i < wholeText.length()
					&& rangeStart + i < rangeEnd; i++) {
				if (wholeText.charAt(rangeStart + i) != searchText.charAt(i)) {
					// mismatch, return false
					return false;
				}
			}
			// no mismatch found, the prefix can be matched
			return true;
		}

		private List<DataSourceSequence<Integer>> tokenize(DataSourceSequence utteranceSeq) {
			List<DataSourceSequence<Integer>> result = new LinkedList<>();

			if (utteranceSeq.getDataSource() instanceof STextualDS) {
				STextualDS ds = (STextualDS) utteranceSeq.getDataSource();

				String wholeText = ds.getText();

				for (int i = utteranceSeq.getStart().intValue(); i < utteranceSeq.getEnd().intValue(); i++) {

					char c = wholeText.charAt(i);
					if (c == '(' || c == '[' || c == '{') {
						// Special treatment for parenthesis:
						// a parenthesis always start a new continuous token from this position to the
						// closing parenthesis.

						char startCharacter = c;
						char endCharacter = c;
						switch (startCharacter) {
						case '(':
							endCharacter = ')';
							break;
						case '[':
							endCharacter = ']';
							break;
						case '{':
							endCharacter = '}';
							break;
						}

						int counter = 1;
						int tokenStart = i;
						while (counter > 0 && i < utteranceSeq.getEnd().intValue()) {
							i++;
							if (wholeText.charAt(i) == startCharacter) {
								// more opening parenthesis
								counter++;
							} else if (wholeText.charAt(i) == endCharacter) {
								counter--;
							}
						}

						// don't add empty token
						if (tokenStart != i) {
							DataSourceSequence<Integer> newTokenSeq = new DataSourceSequence<>();
							newTokenSeq.setDataSource(ds);
							newTokenSeq.setStart(tokenStart);
							newTokenSeq.setEnd(i + 1);
							result.add(newTokenSeq);
						}
						// go to next character
						i++;

					} else if(checkLookahead(wholeText, i, utteranceSeq.getEnd().intValue(), "<Q>")) {
						// find closing tag and make the whole range a single token
						int tokenStart = i;
						i += 3;
						while(i < utteranceSeq.getEnd().intValue()) {
							if(checkLookahead(wholeText, i, utteranceSeq.getEnd().intValue(), "</Q>")) {
								i += 4;
								break;
							} else {
								i++;
							}							
						}
						// don't add empty token
						if (tokenStart != i) {
							DataSourceSequence<Integer> newTokenSeq = new DataSourceSequence<>();
							newTokenSeq.setDataSource(ds);
							newTokenSeq.setStart(tokenStart);
							newTokenSeq.setEnd(i + 1);
							result.add(newTokenSeq);
						}
						// go to next character
						i++;
					} else {

						// check if token pattern matches from this start position
						Matcher nonWhitespace = nonWhiteSpacePattern.matcher(wholeText.substring(i));
						if (nonWhitespace.matches()) {
							int tokenEndExclusive = i + nonWhitespace.end("token");
							DataSourceSequence<Integer> newTokenSeq = new DataSourceSequence<>();
							newTokenSeq.setDataSource(ds);
							newTokenSeq.setStart(i);
							newTokenSeq.setEnd(tokenEndExclusive);
							result.add(newTokenSeq);
							i = tokenEndExclusive;
						}
					}
				}

			}

			return result;
		}

	}

	// =================================================== optional
	// ===================================================
	/**
	 * <strong>OVERRIDE THIS METHOD FOR CUSTOMIZATION</strong> <br/>
	 * This method is called by the pepper framework after initializing this object
	 * and directly before start processing. Initializing means setting properties
	 * {@link PepperModuleProperties}, setting temporary files, resources etc. .
	 * returns false or throws an exception in case of {@link PepperModule} instance
	 * is not ready for any reason.
	 * 
	 * @return false, {@link PepperModule} instance is not ready for any reason,
	 *         true, else.
	 */
	@Override
	public boolean isReadyToStart() throws PepperModuleNotReadyException {
		// TODO make some initializations if necessary
		return (super.isReadyToStart());
	}
}
