/*
 * The GEOMMTx project
 * 
 * Copyright (c) 2007 University of British Columbia
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
package ubic.GEOMMTx.mappers;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import ubic.GEOMMTx.CUIMapper;
import ubic.GEOMMTx.GetUMLSCodes;
import ubic.GEOMMTx.UMLSSourceCode;
import ubic.basecode.util.Configuration;

/**
 * For FMA, we don't find the UMLS mappings in the OWL file; we get those directly from the UMLS data file MRCONSO.RRF.
 * 
 * @author lfrench
 * @version $Id$
 */
public class FMALiteMapper implements CUIMapper {
    private final static String URI_BASE = "http://purl.obolibrary.org/obo/FMA_";

    /*
     * Should probably refactor as this does not use the CUI parameter for anything (non-Javadoc)
     * 
     * @see ubic.GEOMMTx.CUIMapper#convert(java.lang.String, java.util.Collection)
     */
    @Override
    public Set<String> convert( String CUI, Collection<UMLSSourceCode> sourceCodes ) {
        if ( sourceCodes == null ) return null;
        String code = null;
        Set<String> codes = new HashSet<>();
        for ( UMLSSourceCode sourceCode : sourceCodes ) {
            // if FMA is the source
            if ( sourceCode.getSource().startsWith( "UWDA" ) || sourceCode.getSource().startsWith( "FMA" ) ) {
                code = sourceCode.getCode();
                codes.add( URI_BASE + code );
            }
        }

        // doesn't have digital anatomist code
        if ( codes.isEmpty() ) {
            return null;
        }
        // check to see if it exists in the ontology???

        return codes;
    }

    public Set<String> getAllURLs() {
        Set<String> result = new HashSet<>();
        GetUMLSCodes codes = new GetUMLSCodes();
        for ( String concept : codes.getUMLSCodeMap().keySet() ) {
            Set<String> URLs = convert( concept, codes.getUMLSCodeMap().get( concept ) );
            if ( URLs != null ) result.addAll( URLs );
        }
        return result;
    }

    public String getMainURL() {
        return Configuration.getString( "url.fmaOntology" );
    }
}