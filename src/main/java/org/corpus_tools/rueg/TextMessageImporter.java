package org.corpus_tools.rueg;

import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperImporterImpl;
import org.corpus_tools.pepper.modules.PepperImporter;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

@Component(name = "TextMessageImporterJavaComponent", factory = "PepperImporterComponentFactory")
public class TextMessageImporter extends PepperImporterImpl implements PepperImporter {
	public static final String FILE_ENDING = "txt";
	public TextMessageImporter() {
		super();
		setName("TextMessageImporter");
		setProperties(new TextMessageImporterProperties());
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-RUEGModules"));
		setDesc("This importer transforms text messenger exports (such as messages exported with and from WhatsApp) to a Salt model.");
		// set list of formats supported by this module
		addSupportedFormat("timestamped output", null, null);
		getDocumentEndings().add(FILE_ENDING);
	}
	
	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}.
	 * {@inheritDoc PepperModule#createPepperMapper(Identifier)}
	 */
	@Override
	public PepperMapper createPepperMapper(Identifier sElementId) {
		PepperMapper mapper = new TextMessage2SaltMapper();
		URI resourcePath = getIdentifier2ResourceTable().get(sElementId);
		if (sElementId.getIdentifiableElement() instanceof SDocument) {
			mapper.setResourceURI(resourcePath);
		}
		return (mapper);
	}
}
