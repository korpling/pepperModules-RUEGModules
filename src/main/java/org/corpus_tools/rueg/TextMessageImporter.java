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
		addSupportedFormat("timestamped output", "0.0", null);
		getDocumentEndings().add(FILE_ENDING);
	}
	
	/**
	 * Creates a mapper of type {@link TextMessage2SaltMapper}.
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
