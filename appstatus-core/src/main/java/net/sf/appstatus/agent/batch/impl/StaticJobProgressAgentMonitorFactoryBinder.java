/*
 * Copyright 2010 Capgemini
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0 
 * 
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, 
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 * See the License for the specific language governing permissions and 
 * limitations under the License. 
 * 
 */
package net.sf.appstatus.agent.batch.impl;

import net.sf.appstatus.agent.batch.IJobProgressAgentMonitorFactory;
import net.sf.appstatus.agent.batch.spi.IJobProgressAgentMonitorFactoryBinder;

/**
 * Dummy binder use to compile. Should be excluded.
 * 
 * @author Guillaume Mary
 * 
 */
public class StaticJobProgressAgentMonitorFactoryBinder implements
		IJobProgressAgentMonitorFactoryBinder {

	/**
	 * The unique instance of this class.
	 * 
	 */
	private static final StaticJobProgressAgentMonitorFactoryBinder SINGLETON = new StaticJobProgressAgentMonitorFactoryBinder();

	/**
	 * Return the singleton of this class.
	 * 
	 * @return the StaticJobProgressAgentMonitorFactoryBinder singleton
	 */
	public static final StaticJobProgressAgentMonitorFactoryBinder getSingleton() {
		return SINGLETON;
	}

	private StaticJobProgressAgentMonitorFactoryBinder() {
		throw new UnsupportedOperationException(
				"This code should have never made it into the jar");
	}

	/**
	 * {@inheritDoc}
	 */
	public IJobProgressAgentMonitorFactory getJobProgressAgentMonitorFactory() {
		throw new UnsupportedOperationException(
				"This code should never make it into the jar");
	}

	/**
	 * {@inheritDoc}
	 */
	public String getJobProgressAgentMonitorFactoryStr() {
		throw new UnsupportedOperationException(
				"This code should never make it into the jar");
	}

}