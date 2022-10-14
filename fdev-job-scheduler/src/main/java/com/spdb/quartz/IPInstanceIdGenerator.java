/*
 * All content copyright Terracotta, Inc., unless otherwise indicated. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 */
package com.spdb.quartz;

import com.spdb.common.util.IPUtils;
import org.quartz.SchedulerException;
import org.quartz.simpl.HostnameInstanceIdGenerator;
import org.quartz.simpl.SimpleInstanceIdGenerator;
import org.quartz.spi.InstanceIdGenerator;


/**
 * <p>
 * <code>InstanceIdGenerator</code> that names the scheduler instance using
 * just the machine hostname.
 * </p>
 *
 * <p>
 * This class is useful when you know that your scheduler instance will be the
 * only one running on a particular machine.  Each time the scheduler is
 * restarted, it will get the same instance id as long as the machine is not
 * renamed.
 * </p>
 *
 * @author lizz
 * @see InstanceIdGenerator
 * @see SimpleInstanceIdGenerator
 * @see HostnameInstanceIdGenerator
 */

public class IPInstanceIdGenerator implements InstanceIdGenerator {

    @Override
    public String generateInstanceId() throws SchedulerException {
        return IPUtils.getLocalIP() + "_" + System.currentTimeMillis();
    }
}