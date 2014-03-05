package pt.webdetails.cda.services;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import net.htmlparser.jericho.Attributes;
import net.htmlparser.jericho.HTMLElementName;
import net.htmlparser.jericho.OutputDocument;
import net.htmlparser.jericho.Source;
import net.htmlparser.jericho.StartTag;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cpf.context.api.IUrlProvider;
import pt.webdetails.cpf.packager.origin.PathOrigin;
import pt.webdetails.cpf.repository.api.IContentAccessFactory;
import pt.webdetails.cpf.utils.MimeTypes;
import pt.webdetails.cpf.utils.Pair;

/**
 * Serves a pre-processed webpage.
 * Updates URLs so a page can be serviced outside its location.
 * Also allows to inject simple javascript assignments.
 */
public abstract class ProcessedHtmlPage extends BaseService {

  private static Log log = LogFactory.getLog(ProcessedHtmlPage.class);
  protected static final Pattern URL_PROTOCOL = Pattern.compile("^\\w*\\:");

  private static final String CODE_SNIPPET_START_TAG = String.format( "<script type=\"%s\">\n", MimeTypes.JAVASCRIPT) ;
  private static final String CODE_SNIPPET_END_TAG = "\n</script>\n";

  private IUrlProvider urlProvider;
  private IContentAccessFactory access;

  protected ProcessedHtmlPage( IUrlProvider urlProvider, IContentAccessFactory access ) {
    this.urlProvider = urlProvider;
    this.access = access;
  }

  private IUrlProvider getUrlProvider() {
    return urlProvider;
  }
  private IContentAccessFactory getRepo() {
    return access;
  }

  protected String processPage(PathOrigin baseDir, String pagePath) throws IOException {

    long start = System.currentTimeMillis();
    InputStream file = null;
    try {
      file = baseDir.getReader( getRepo() ).getFileInputStream( pagePath );
      Source html = new Source( file ); 
      OutputDocument outDoc = new OutputDocument( html );
      // transform
      modifyDocument( html, baseDir, outDoc );
      return outDoc.toString();
    } finally {
      IOUtils.closeQuietly( file );
      if ( log.isDebugEnabled() ) {
        log.debug( String.format("processPage for %s took %dms", pagePath, System.currentTimeMillis() - start ) );
      }
    }

  }

  /**
   * Updates relative source attributes to externally accessible abs paths
   * @param html the document
   * @param baseDir html location
   * @param out processed document
   */
  protected void modifyDocument( Source html, PathOrigin baseDir, OutputDocument out ) {
    replaceUrlAttribute( html.getAllStartTags( HTMLElementName.LINK ), "href", baseDir, out);
    replaceUrlAttribute( html.getAllStartTags( HTMLElementName.SCRIPT ), "src", baseDir, out);
    replaceUrlAttribute( html.getAllStartTags( HTMLElementName.IMG ), "src", baseDir, out);
    int insertPos = html.getFirstElement( HTMLElementName.HEAD ).getEndTag().getBegin() ;
    out.insert( insertPos, getCodeSnippet( getBackendAssignments( getUrlProvider() ) ) );
  }

  /**
   * Will be added in a code snippet at the end of the HEAD element.
   */ 
  protected abstract Iterable<Pair<String, String>> getBackendAssignments( IUrlProvider urlProvider );

  protected String getCodeSnippet(Iterable<Pair<String,String>> assignments) {
    StringBuilder element =  new StringBuilder( CODE_SNIPPET_START_TAG );
    for ( Pair<String, String> assignment : assignments ) {
      element.append( assignment.first ).append( " = " ).append( assignment.second ).append( ";\n" );
    }
    element.append( CODE_SNIPPET_END_TAG );
    return element.toString();
  }

  protected boolean shouldProcessPath( String path ) {
    // if it is a relative path
    return 
        !StringUtils.isEmpty( path ) &&
        !path.startsWith( "/" ) &&
        !URL_PROTOCOL.matcher( path ).find();
  }

  protected String processPath(PathOrigin origin, String path, IUrlProvider urlProvider ) {
    return normalizeUri( origin.getUrl( path, urlProvider ) );
  }

  protected int replaceUrlAttribute ( Iterable<StartTag> tags, final String pathAttribute, PathOrigin baseDir, OutputDocument doc ) {
    int count = 0;
    for ( StartTag tag : tags ) {
      Attributes attr = tag.parseAttributes();
      String path = attr.getValue( pathAttribute );
      if ( shouldProcessPath( path ) ) {
        String newPath = processPath( baseDir, path, getUrlProvider() );
        if ( log.isTraceEnabled() ) { //TODO: trace
          log.trace( String.format( "replaced: in %s@%s \"%s\" --> \"%s\"", tag.getName(), pathAttribute, path, newPath ) );
        }
        doc.replace( attr, true ).put( pathAttribute, newPath );
        count++;
      }
    }
    return count;
  }

  private static String normalizeUri ( String path ) {
    try {
      URI uri = new URI( path );
      return uri.normalize().getPath();
    } catch ( URISyntaxException e ) {
      log.error("normalizeUri: cannot process path " + path, e);
      return path;
    }
  }

}
