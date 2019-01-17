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

import org.corpus_tools.rueg.TokenizeRUEGTranscription;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.corpus_tools.pepper.testFramework.PepperManipulatorTest;
import org.corpus_tools.salt.common.SCorpus;
import org.corpus_tools.salt.samples.SampleGenerator;
import org.junit.Before;
import org.junit.Test;

/**
 * This is a dummy implementation of a JUnit test for testing the
 * {@link TokenizeRUEGTranscription} class. Feel free to adapt and enhance this test
 * class for real tests to check the work of your manipulator. If you are not
 * confirm with JUnit, please have a look at <a
 * href="http://www.vogella.com/tutorials/JUnit/article.html">
 * http://www.vogella.com/tutorials/JUnit/article.html</a>. <br/>
 * Please note, that the test class is derived from
 * {@link PepperManipulatorTest}. The usage of this class should simplfy your
 * work and allows you to test only your single manipulator in the Pepper
 * environment.
 * 
 * @author Thomas Krause
 */
public class TokenizeRUEGTranscriptionTest extends PepperManipulatorTest {
	/**
	 * This method is called by the JUnit environment each time before a test
	 * case starts. So each time a method annotated with @Test is called. This
	 * enables, that each method could run in its own environment being not
	 * influenced by before or after running test cases.
	 */
	@Before
	public void setUp() {
		setFixture(new TokenizeRUEGTranscription());
	}

	// TODO add further tests for any test cases you can think of and which are
	// necessary
}
