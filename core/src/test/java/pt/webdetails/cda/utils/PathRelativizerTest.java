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

import org.junit.Assert;
import org.junit.Test;


public class PathRelativizerTest {

  private final String NORMAL_BASE_PATH = "/path/to/file.txt";
  private final String ROOT_BASE_PATH = "/";
  private final String FOLDER_BASE_PATH = "/folder";


  @Test
  public void testRelativizePath() {

    String sameFolder = PathRelativizer.relativizePath( NORMAL_BASE_PATH, "/path/to/otherFile.txt" );
    String higherUp = PathRelativizer.relativizePath( NORMAL_BASE_PATH, "/path/to/otherFolder/otherFile.java" );
    String inRoot = PathRelativizer.relativizePath( NORMAL_BASE_PATH, "/root.css" );
    String diferentPathFromRoot = PathRelativizer.relativizePath( NORMAL_BASE_PATH, "/other/path/to/a/file.html" );
    String huge = PathRelativizer.relativizePath( NORMAL_BASE_PATH, "/path/huge/to/some/file/one/two/three/a/b/c.css" );
    String toFolder = PathRelativizer.relativizePath( NORMAL_BASE_PATH, "/path/to/folder" );
    String sameFile = PathRelativizer.relativizePath( NORMAL_BASE_PATH, NORMAL_BASE_PATH );

    Assert.assertEquals( "otherFile.txt", sameFolder );
    Assert.assertEquals( "otherFolder/otherFile.java", higherUp );
    Assert.assertEquals( "../../root.css", inRoot );
    Assert.assertEquals( "../../other/path/to/a/file.html", diferentPathFromRoot );
    Assert.assertEquals( "../huge/to/some/file/one/two/three/a/b/c.css", huge );
    Assert.assertEquals( "folder", toFolder );
    Assert.assertEquals( "", sameFile );

    String rootSameFolder = PathRelativizer.relativizePath( ROOT_BASE_PATH, "/file.txt" );
    String rootHigherUpFolder = PathRelativizer.relativizePath( ROOT_BASE_PATH, "/path/to/a/file.css" );
    String rootToFolder = PathRelativizer.relativizePath( ROOT_BASE_PATH, "/path/to/a/folder" );
    String rootToRoot = PathRelativizer.relativizePath( ROOT_BASE_PATH, ROOT_BASE_PATH );

    Assert.assertEquals( "file.txt", rootSameFolder );
    Assert.assertEquals( "path/to/a/file.css", rootHigherUpFolder );
    Assert.assertEquals( "path/to/a/folder", rootToFolder );
    Assert.assertEquals( "", rootToRoot );

    String folderSameFolder = PathRelativizer.relativizePath( FOLDER_BASE_PATH, "/folder/file.css" );
    String folderHigherUp = PathRelativizer.relativizePath( FOLDER_BASE_PATH, "/folder/anotherFolder/file.html" );
    String folderInRoot = PathRelativizer.relativizePath( FOLDER_BASE_PATH, "/file.sh" );
    String folderDiferentPath = PathRelativizer.relativizePath( FOLDER_BASE_PATH, "/anotherFolder/file.bat" );
    String folderToFolder = PathRelativizer.relativizePath( FOLDER_BASE_PATH, "/folder/anotherFolder" );
    String folderToSameFolder = PathRelativizer.relativizePath( FOLDER_BASE_PATH, FOLDER_BASE_PATH );

    Assert.assertEquals( "file.css", folderSameFolder );
    Assert.assertEquals( "anotherFolder/file.html", folderHigherUp );
    Assert.assertEquals( "../file.sh", folderInRoot );
    Assert.assertEquals( "../anotherFolder/file.bat", folderDiferentPath );
    Assert.assertEquals( "anotherFolder", folderToFolder );
    Assert.assertEquals( "", folderToSameFolder );

  }
}
