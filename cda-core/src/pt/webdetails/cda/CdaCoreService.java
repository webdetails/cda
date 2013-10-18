/*!
* Copyright 2002 - 2013 Webdetails, a Pentaho company.  All rights reserved.
* 
* This software was developed by Webdetails and is provided under the terms
* of the Mozilla Public License, Version 2.0, or any later version. You may not use
* this file except in compliance with the license. If you need a copy of the license,
* please go to  http://mozilla.org/MPL/2.0/. The Initial Developer is Webdetails.
*
* Software distributed under the Mozilla Public License is distributed on an "AS IS"
* basis, WITHOUT WARRANTY OF ANY KIND, either express or  implied. Please refer to
* the license for the specific language governing your rights and limitations.
*/

package pt.webdetails.cda;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import pt.webdetails.cda.dataaccess.AbstractDataAccess;
import pt.webdetails.cda.dataaccess.DataAccessConnectionDescriptor;
import pt.webdetails.cda.discovery.DiscoveryOptions;
import pt.webdetails.cda.exporter.Exporter;
import pt.webdetails.cda.exporter.ExporterEngine;
import pt.webdetails.cda.query.QueryOptions;
import pt.webdetails.cda.settings.CdaSettings;
import pt.webdetails.cda.settings.SettingsManager;
import pt.webdetails.cda.utils.DoQueryParameters;
import pt.webdetails.cpf.PluginEnvironment;
import pt.webdetails.cpf.Util;
import pt.webdetails.cpf.repository.api.IReadAccess;
import pt.webdetails.cpf.utils.CharsetHelper;
import pt.webdetails.cpf.utils.MimeTypes;



public class CdaCoreService
{ 

  private static Log logger = LogFactory.getLog(CdaCoreService.class);

  private static final int DEFAULT_PAGE_SIZE = 20;
  private static final int DEFAULT_START_PAGE = 0;
  private static final String JSONP_CALLBACK = "callback";
  // TODO: purge this bugger
  private IResponseTypeHandler responseHandler;

  public CdaCoreService() { }

  public CdaCoreService(IResponseTypeHandler responseHandler) {
    this.responseHandler = responseHandler;
  }

  public void setResponseHandler(IResponseTypeHandler responseHandler) {
    this.responseHandler = responseHandler;
  }

  //XXX separate this
  public void doQuery(final OutputStream out,DoQueryParameters parameters) throws Exception
  {        
    final CdaEngine engine = CdaEngine.getInstance();
    final QueryOptions queryOptions = new QueryOptions();

    final String path = parameters.getPath();
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(path);
    
    // Handle paging options
    // We assume that any paging options found mean that the user actively wants paging.
    final long pageSize = parameters.getPageSize();
    final long pageStart = parameters.getPageStart();
    final boolean paginate = parameters.isPaginateQuery();
    if (pageSize > 0 || pageStart > 0 || paginate)
    {
      if (pageSize > Integer.MAX_VALUE || pageStart > Integer.MAX_VALUE)
      {
        throw new ArithmeticException("Paging values too large");
      }
      queryOptions.setPaginate(true);
      queryOptions.setPageSize(pageSize > 0 ? (int) pageSize : paginate ? DEFAULT_PAGE_SIZE : 0);
      queryOptions.setPageStart(pageStart > 0 ? (int) pageStart : paginate ? DEFAULT_START_PAGE : 0);
    }
    
    // Support for bypassCache (we'll maintain the name we use in CDE
    /*if(requestParams.hasParameter("bypassCache")){
      queryOptions.setCacheBypass(Boolean.parseBoolean(requestParams.getStringParameter("bypassCache","false")));
    }*/

    queryOptions.setCacheBypass(parameters.isBypassCache());

    queryOptions.setOutputType(parameters.getOutputType());
    queryOptions.setDataAccessId(parameters.getDataAccessId());
    try {
      queryOptions.setOutputIndexId(parameters.getOutputIndexId());
    } catch (NumberFormatException e) {
      logger.error("Illegal outputIndexId '" + parameters.getOutputIndexId() + "'" );
    }
    
    final ArrayList<String> sortBy = new ArrayList<String>();
    for (String sort : parameters.getSortBy())
    {
      if( !StringUtils.isEmpty(sort) )
      {
        sortBy.add(sort);
      }
    }
    queryOptions.setSortBy(sortBy);


    // ... and the query parameters
    // We identify any pathParams starting with "param" as query parameters and extra settings prefixed with "setting"

    for (Map.Entry<String,Object> entry : parameters.getExtraSettings().entrySet())
    {
      final String name = entry.getKey();
      final Object parameter = entry.getValue();
      queryOptions.addSetting(name, (String)parameter); //...
    }

    for ( Map.Entry<String, Object> entry : parameters.getExtraParams().entrySet() ) {
      final String name = entry.getKey();
      final Object parameter = entry.getValue();
      queryOptions.addParameter(name, parameter);

      // if(name.startsWith(PREFIX_PARAMETER)){
      // queryOptions.addParameter(name.substring(PREFIX_PARAMETER.length()),
      // parameter);
      // logger.debug("##########"+name+"##############");

    }
/*
      if (param.startsWith(PREFIX_PARAMETER))
      {
        queryOptions.addParameter(param.substring(PREFIX_PARAMETER.length()), requestParams.getParameter(param));
      }
      else if (param.startsWith(PREFIX_SETTING))
      {
        queryOptions.addSetting(param.substring(PREFIX_SETTING.length()), requestParams.getStringParameter(param, ""));
      }*/
      
    

    if(parameters.isWrapItUp()) {
      String uuid = engine.wrapQuery(out, cdaSettings, queryOptions);
      logger.debug("doQuery: query wrapped as " + uuid);
      writeOut(out, uuid);
      return;
    }
    
    // we'll allow for the special "callback" param to be used, and passed as settingcallback to jsonp exports
    if (!parameters.getJsonCallback().equals("<blank>"))
    {
      queryOptions.addSetting(JSONP_CALLBACK, parameters.getJsonCallback());
    }
    Exporter exporter = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings());
    
    String attachmentName = exporter.getAttachmentName();
    String mimeType = (attachmentName == null) ? null : getMimeType(attachmentName);
    if(StringUtils.isEmpty(mimeType)){
      mimeType = exporter.getMimeType();
    }
    
    if (parameters != null);//XXX + FIXME ==  if (this.parameterProviders != null)  
    {
      setResponseHeaders(mimeType, attachmentName);
    }
    // Finally, pass the query to the engine
    engine.doQuery(out, cdaSettings, queryOptions);

  }


  public void unwrapQuery(final OutputStream out,
          final String path, final String solution, final String file, final String uuid)  throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
    final String relativePath = getRelativePath(path,solution,file);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(relativePath);
    QueryOptions queryOptions = engine.unwrapQuery(uuid);
    if(queryOptions != null) {
      Exporter exporter = ExporterEngine.getInstance().getExporter(queryOptions.getOutputType(), queryOptions.getExtraSettings());
      
      String attachmentName = exporter.getAttachmentName();
      String mimeType = (attachmentName == null) ? null : getMimeType(attachmentName);
      if(StringUtils.isEmpty(mimeType)){
        mimeType = exporter.getMimeType();
      }
      
      if (relativePath!=null && uuid!= null);//XXX  ==  if (this.parameterProviders != null)  
      {
        setResponseHeaders(mimeType, attachmentName);
      }
      engine.doQuery(out, cdaSettings, queryOptions);
    }
    else {
      logger.error("unwrapQuery: uuid " + uuid + " not found.");
    }
    
  }

  /**
   * List 
   */
  public void listQueries(final OutputStream out,
          final String path,final String solution,final String file, final String outputType) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
    final String relativePath = getRelativePath(path,solution,file);
    if(StringUtils.isEmpty(relativePath)){
      throw new IllegalArgumentException("No path provided");
    }
    logger.debug("Do Query: getRelativePath:" + relativePath);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(relativePath);

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(outputType);

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);
    engine.listQueries(out, cdaSettings, discoveryOptions);
  }

  public void listParameters(final OutputStream out, 
          final String path, final String solution,final String file, final String outputType,final String dataAccessId) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();
   // final ICommonParameterProvider requestParams = requParam;
    final String relativePath = getRelativePath(path,solution,file);
//    IRepositoryAccess repAccess = CdaEngine.getEnvironment().getRepositoryAccess();
    logger.debug("Do Query: getRelativePath:" + relativePath);
    final CdaSettings cdaSettings = SettingsManager.getInstance().parseSettingsFile(relativePath);

    // Handle the query itself and its output format...
    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(outputType);
    discoveryOptions.setDataAccessId(dataAccessId);

    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);

    engine.listParameters(out, cdaSettings, discoveryOptions);
  }

  public void getCdaList(final OutputStream out,final String outputType) throws Exception
  {
    final CdaEngine engine = CdaEngine.getInstance();

    final DiscoveryOptions discoveryOptions = new DiscoveryOptions();
    discoveryOptions.setOutputType(outputType);
    String mimeType = ExporterEngine.getInstance().getExporter(discoveryOptions.getOutputType()).getMimeType();
    setResponseHeaders(mimeType);
    engine.getCdaList(out, discoveryOptions);
  }

 // @Exposed(accessLevel = AccessLevel.ADMIN, outputType = MimeType.PLAIN_TEXT)
  public void clearCache(final OutputStream out) throws Exception
  {
    SettingsManager.getInstance().clearCache();
    AbstractDataAccess.clearCache();

    out.write("Cache cleared".getBytes());
  }
  

  private String getRelativePath( final String originalPath, final String solution, final String file )
    throws UnsupportedEncodingException
  {
    final String encoding = CharsetHelper.getEncoding();
    String joined = "";
    joined += ( StringUtils.isEmpty( solution ) ? "" : URLDecoder.decode( solution, encoding ) + "/" );
    joined += ( StringUtils.isEmpty( originalPath ) ? "" : URLDecoder.decode( originalPath, encoding ) );
    joined += ( StringUtils.isEmpty( file ) ? "" : "/" + URLDecoder.decode( file, encoding ) );
    joined = joined.replaceAll( "//", "/" );
    return joined;
  }


  public String getResourceAsString(final String path, final HashMap<String, String> tokens) throws IOException
  {
    // Read file
    IReadAccess repository = getRepositoryAccess();
    String resourceContents = StringUtils.EMPTY;
    
    if (repository.fileExists(path))
    {
      resourceContents = Util.toString( repository.getFileInputStream(path) );
    }
    else {
      return null;
    }
    
    // Make replacement of tokens
    if (tokens != null)
    {
      for (final String key : tokens.keySet())
      { 
        resourceContents = StringUtils.replace(resourceContents, key, tokens.get(key));
      }
    }
    return resourceContents;
  }

  public void getCssResource(final OutputStream out, final String resource) throws Exception
  {
    getResource(out, resource);
  }

  public void getJsResource(final OutputStream out, final String resource) throws Exception
  {
    getResource(out, resource);
  }

  public void getResource(final OutputStream out, String resource) throws Exception
  {
    IReadAccess repo = getRepositoryAccess();
    if ( repo.fileExists( resource ) ) {
      InputStream in = null;
      try {
        in = repo.getFileInputStream( resource );
        IOUtils.copy( in, out );
      }
      finally {
        IOUtils.closeQuietly( in );
      }
      
    }

  }

  //TODO: temp, only getResource needs repository access
  private IReadAccess getRepositoryAccess() {
    return PluginEnvironment.env().getContentAccessFactory().getUserContentAccess("/");
  }

  public void listDataAccessTypes(final OutputStream out,final boolean refreshCache) throws Exception
  {
    //boolean refreshCache = Boolean.parseBoolean(requParam.getStringParameter("refreshCache", "false"));
    
    DataAccessConnectionDescriptor[] data = SettingsManager.getInstance().
            getDataAccessDescriptors(refreshCache);

    StringBuilder output = new StringBuilder("");
    if (data != null)
    {
      output.append("{\n");
      for (DataAccessConnectionDescriptor datum : data)
      {
                output.append(datum.toJSON()).append(",\n");
      }
      writeOut(out, output.toString().replaceAll(",\n\\z", "\n}"));
    }
  }

  public String getPluginName() {
    return "cda";
  }

  private void writeOut(OutputStream out,String uuid){
    try {
      out.write( uuid.getBytes() );
    } catch ( IOException e ) {
      logger.error( "Failed to write to stream", e );
    }
  }

  private String getMimeType(String attachmentName){
      return MimeTypes.getMimeType(attachmentName);
  }

  private void setResponseHeaders(String mimeType, String attachmentName){
       setResponseHeaders(mimeType, 0, attachmentName);
  }

  private void setResponseHeaders(String mimeType){
      setResponseHeaders(mimeType, 0, null);
  }
  /**
   * @deprecated no headers in core service
   */
  private void setResponseHeaders(final String mimeType, final int cacheDuration, final String attachmentName)
  {
    if (responseHandler.hasResponse()) {
        responseHandler.setResponseHeaders(mimeType, cacheDuration, attachmentName);
    }
  }
  
  
}
