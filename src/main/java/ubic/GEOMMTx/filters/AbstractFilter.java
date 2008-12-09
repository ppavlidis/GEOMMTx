package ubic.GEOMMTx.filters;

import java.io.FileWriter;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ubic.GEOMMTx.ProjectRDFModelTools;
import ubic.GEOMMTx.Vocabulary;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class AbstractFilter {
    protected static Log log = LogFactory.getLog( AbstractFilter.class );

    /**
     * Remove mentions that have a mapped term corresponding to one of the given URL's
     * 
     * @param model the rdf model to remove mentions from
     * @param URLs the urls to remove
     * @return the number of mentions that have been removed from the RDF model
     */
    public int removeMentionsURLs( Model model, Set<String> URLs ) {
        int howMany = 0;
        for ( String URL : URLs ) {
            Resource resource = model.createResource( URL );
            // list all the mentions
            ResIterator mentionIterator = model.listResourcesWithProperty( Vocabulary.mappedTerm, resource );
            Set mentionSet = mentionIterator.toSet();
            ProjectRDFModelTools.removeMentions( model, mentionSet );
            howMany += mentionSet.size();
        }
        return howMany;
    }

    protected int removeMentions( Model model, ResultSet results ) {
        Set<Resource> affectedMentions = new HashSet<Resource>();

        while ( results.hasNext() ) {
            QuerySolution soln = results.nextSolution();
            Resource mention = soln.getResource( "mention" );
            affectedMentions.add( mention );
        }

        ProjectRDFModelTools.removeMentions( model, affectedMentions );

        return affectedMentions.size();
    }

    public abstract int filter( Model model );

    public abstract String getName();


}