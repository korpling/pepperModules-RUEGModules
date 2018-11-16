package org.corpus_tools.rueg;

import org.corpus_tools.pepper.common.PepperConfiguration;
import org.corpus_tools.pepper.impl.PepperManipulatorImpl;
import org.corpus_tools.pepper.modules.PepperManipulator;
import org.corpus_tools.pepper.modules.PepperMapper;
import org.corpus_tools.salt.common.SDocument;
import org.corpus_tools.salt.graph.Identifier;
import org.eclipse.emf.common.util.URI;
import org.osgi.service.component.annotations.Component;

@Component(name = "TokenizationMultiplicatorJavaComponent", factory = "PepperManipulatorComponentFactory")
public class TokenizationMultiplicator extends PepperManipulatorImpl implements PepperManipulator{
	public TokenizationMultiplicator() {
		super();
		setName("TokenizationMultiplicator");
		setProperties(new TokenizationMultiplicatorProperties());
		setSupplierContact(URI.createURI(PepperConfiguration.EMAIL));
		setSupplierHomepage(URI.createURI("https://github.com/korpling/pepperModules-RUEGModules"));
		setDesc("This importer transforms text messenger exports (such as messages exported with and from WhatsApp) to a Salt model.");
	}
	
	/**
	 * Creates a mapper of type {@link EXMARaLDA2SaltMapper}.
	 * {@inheritDoc PepperModule#createPepperMapper(Identifier)}
	 */
	@Override
	public PepperMapper createPepperMapper(Identifier sElementId) {
		return (new TokenizationMultiplicatorMapper());
	}
}
