/*
 * The GEOMMTx project
 * 
 * Copyright (c) 2009 University of British Columbia
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
 */
package ubic.GEOMMTx;

import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import ubic.GEOMMTx.util.SetupParameters;

/**
 * Test of options setting.
 * 
 * @author paul
 * @version $Id$
 */
public class TestSetupParameters {

    @Test
    public void test() {
        Iterator<String> i = SetupParameters.getKeys( "geommtx.annotator" );

        assertTrue( i.hasNext() );

        assertTrue( StringUtils.isNotBlank( SetupParameters.getStringArray( "geommtx.annotator.mmtxOptions" )[0] ) );

        assertTrue( StringUtils.isNotBlank( SetupParameters.getStringArray( "geommtx.annotator.mmtxOptions" )[1] ) );
    }

}
