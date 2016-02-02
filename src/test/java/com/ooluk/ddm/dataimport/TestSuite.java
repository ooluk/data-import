/*
 *  Copyright 2015 Ooluk Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.ooluk.ddm.dataimport;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * @author Siddhesh Prabhu
 * @since 1.0
 *
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
    com.ooluk.ddm.dataimport.rule.RuleMapTest.class,
    com.ooluk.ddm.dataimport.rule.RuleStoreTest.class,
    com.ooluk.ddm.dataimport.rule.RulesEngineTest.class,
    com.ooluk.ddm.dataimport.workers.cobol.COBOLDataObjectReaderTest.class,
    com.ooluk.ddm.dataimport.workers.cobol.COBOLSyntaxTest.class,
    com.ooluk.ddm.dataimport.workers.cobol.COBOLDataObjectReaderTest.class,
    com.ooluk.ddm.dataimport.workers.jdbc.JDBCDataObjectReaderTest.class,
    com.ooluk.ddm.dataimport.workers.xml.XMLDataObjectReaderTest.class
})

public class TestSuite {

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }    
}