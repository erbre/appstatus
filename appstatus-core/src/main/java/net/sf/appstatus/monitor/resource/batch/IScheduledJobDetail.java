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
package net.sf.appstatus.monitor.resource.batch;

import java.util.Date;
import java.util.Map;

/**
 * Scheduled job detail.
 * 
 * @author Guillaume Mary
 * 
 */
public interface IScheduledJobDetail {
	/**
	 * Returns the next time at which the Job is scheduled to fire.
	 * 
	 * @return next fire time
	 */
	Date getNextFireTime();

	/**
	 * Return the job parameters map.
	 * 
	 * @return job parameters map
	 */
	Map<String, String> getParameters();

}