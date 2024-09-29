/*! ******************************************************************************
 *
 * Pentaho
 *
 * Copyright (C) 2024 by Hitachi Vantara, LLC : http://www.pentaho.com
 *
 * Use of this software is governed by the Business Source License included
 * in the LICENSE.TXT file.
 *
 * Change Date: 2028-08-13
 ******************************************************************************/


package pt.webdetails.cda.utils;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

public class PathRelativizer {


  private static final String MOCK_FILE = "mockFile.mockExtension";

  /**
   * Given two absolute paths, this method returns the targetPath relative to the basePath. It won't work with Windows
   * paths.
   *
   * @param basePath   Path to relativize against
   * @param targetPath Path to relativize
   * @return targetPath relative to basePath
   */
  public static String relativizePath( String basePath, String targetPath ) {
    basePath = FilenameUtils.separatorsToUnix( FilenameUtils.normalizeNoEndSeparator( basePath ) );
    targetPath = FilenameUtils.separatorsToUnix( FilenameUtils.normalizeNoEndSeparator( targetPath ) );

    String[] basePathArray = basePath.length() > 1 ? basePath.split( "/" ) : new String[] { "/" };
    String[] targetPathArray = targetPath.length() > 1 ? targetPath.split( "/" ) : new String[] { "/" };

    //if we're dealing with folders, add a mock file
    if ( basePathArray.length > 1 && !basePathArray[ basePathArray.length - 1 ].contains( "." ) ) {
      basePath += "/" + MOCK_FILE;
      basePathArray = basePath.split( "/" );
    }
    if ( targetPathArray.length > 1 && !targetPathArray[ targetPathArray.length - 1 ].contains( "." ) ) {
      targetPath += "/" + MOCK_FILE;
      targetPathArray = targetPath.split( "/" );
    }

    int differCount = -1;
    for ( int i = 0; ( i < basePathArray.length ) && ( i < targetPathArray.length ); i++ ) {
      if ( basePathArray[ i ].equals( targetPathArray[ i ] ) ) {
        continue;
      }
      differCount = i;
      break;
    }
    if ( differCount == -1 ) {
      targetPathArray = cleanSamePath( basePathArray, targetPathArray );
      return StringUtils.join( targetPathArray, "/" );
    } else {
      if ( differCount == basePathArray.length - 1 ) { //case same folder or some folders ahead
        targetPathArray = cleanSamePath( basePathArray, targetPathArray );
        return StringUtils.join( targetPathArray, "/" );
      } else { // case some folders back
        String backUp = "";
        for ( int i = differCount; i < basePathArray.length - 1; i++ ) {
          backUp += "../";
        }
        targetPathArray = cleanSamePath( basePathArray, targetPathArray );
        return backUp + StringUtils.join( targetPathArray, "/" );
      }

    }

  }

  private static String[] cleanSamePath( String[] model, String[] dirty ) {
    for ( int i = 0; i < model.length; i++ ) {
      if ( !dirty[ i ].equals( model[ i ] ) ) {
        break;
      }
      dirty[ i ] = "";
    }
    return removeTrash( dirty );
  }

  private static String[] removeTrash( String[] dirty ) {
    String clean = "";
    for ( int i = 0; i < dirty.length; i++ ) {
      if ( !StringUtils.isEmpty( dirty[ i ] ) && !dirty[ i ].equals( MOCK_FILE ) ) {
        clean += dirty[ i ] + "/";
      }
    }
    return clean.split( "/" );
  }
}
