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

package pt.webdetails.cda.cache;

import java.util.concurrent.Callable;

/**
 * Boilerplate to run a method in a different ClassLoader.
 */
public class ClassLoaderAwareCaller {
  private ClassLoader classLoader;

  public ClassLoaderAwareCaller() {
    this(Thread.currentThread().getContextClassLoader());
  }

  public ClassLoaderAwareCaller(ClassLoader classLoader) {
   this.classLoader = classLoader; 
  }

  protected <T> T callInClassLoader(Callable<T> callable) throws Exception{
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try
    {
      if(this.classLoader != null)
      {
        Thread.currentThread().setContextClassLoader(this.classLoader);
      }
      
      return callable.call();
      
    }
    finally{
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }

  protected void runInClassLoader(Runnable runnable)
  {
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    try
    {
      if(this.classLoader != null)
      {
        Thread.currentThread().setContextClassLoader(this.classLoader);
      }
      
      runnable.run();
      
    }
    finally{
      Thread.currentThread().setContextClassLoader(contextClassLoader);
    }
  }
}
