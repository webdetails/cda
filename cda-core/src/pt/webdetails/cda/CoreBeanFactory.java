/* This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/. */

package pt.webdetails.cda;

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


public class CoreBeanFactory implements ICdaBeanFactory {


	private static final Log logger = LogFactory.getLog(CoreBeanFactory.class);  

	protected static ConfigurableApplicationContext context;

	public CoreBeanFactory() {
		context = getSpringBeanFactory("cda.spring.xml");
	}

	public CoreBeanFactory(String config) {
		context = getSpringBeanFactory(config);
	}

	protected ConfigurableApplicationContext getSpringBeanFactory(String config) {
		try {
			final ClassLoader cl = this.getClass().getClassLoader();
			URL url = cl.getResource(config);
			if (url != null) {
				logger.debug("Found spring file @ " + url); //$NON-NLS-1$
				ConfigurableApplicationContext context = new ClassPathXmlApplicationContext(config) {
					@Override
					protected void initBeanDefinitionReader(
							XmlBeanDefinitionReader beanDefinitionReader) {

						beanDefinitionReader.setBeanClassLoader(cl);
					}

					@Override
					protected void prepareBeanFactory(
							ConfigurableListableBeanFactory clBeanFactory) {
						super.prepareBeanFactory(clBeanFactory);
						clBeanFactory.setBeanClassLoader(cl);
					}

					/**
					 * Critically important to override this and return the desired
					 * CL
					 **/
					@Override
					public ClassLoader getClassLoader() {
						return cl;
					}
				};
				return context;
			}
		} catch (Exception e) {
			logger.fatal("Error loading cda.spring.xml", e);
		}
		logger.fatal("Spring definition file does not exist. There should be a cda.spring.xml file on the classpath ");
		return null;

	}

	@Override
	public Object getBean(String id) {
		if (context.containsBean(id)) {
			return context.getBean(id);
		}
		return null;
	}

	public String[] getBeanNamesForType(Class clazz) {
		return context.getBeanNamesForType(clazz);
	}

	@Override
	public boolean containsBean(String id) {
		if (context != null)
			return context.containsBean(id);
		return false;
	}



}
