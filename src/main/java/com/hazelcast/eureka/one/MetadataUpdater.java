/*
 * Copyright 2020 Hazelcast Inc.
 *
 * Licensed under the Hazelcast Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at
 *
 * http://hazelcast.com/hazelcast-community-license
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package com.hazelcast.eureka.one;

import java.util.Map;

import com.google.common.base.Preconditions;
import com.hazelcast.spi.discovery.DiscoveryNode;
import com.netflix.appinfo.ApplicationInfoManager;
import com.netflix.appinfo.InstanceInfo;

class MetadataUpdater implements StatusChangeStrategy {

    private DiscoveryNode discoveryNode;
    private boolean selfRegistration;
    private String groupName;

    MetadataUpdater(DiscoveryNode discoveryNode, boolean selfRegistration, String groupName) {
        Preconditions.checkNotNull(discoveryNode);
        Preconditions.checkNotNull(groupName);

        this.discoveryNode = discoveryNode;
        this.selfRegistration = selfRegistration;
        this.groupName = groupName;
    }

    @Override
    public void update(ApplicationInfoManager manager, InstanceInfo.InstanceStatus status) {
        Preconditions.checkNotNull(manager);
        Preconditions.checkNotNull(status);

        int port = discoveryNode.getPrivateAddress().getPort();
        String host = discoveryNode.getPrivateAddress().getHost();

        // provide Hazelcast info in Eureka metadata
        Map<String, String> map = manager.getInfo().getMetadata();
        map.put(EurekaHazelcastMetadata.HAZELCAST_PORT, Integer.toString(port));
        map.put(EurekaHazelcastMetadata.HAZELCAST_HOST, host);
        map.put(EurekaHazelcastMetadata.HAZELCAST_GROUP_NAME, groupName);

        if (shouldRegister()) {
            manager.setInstanceStatus(status);
        }
    }

    @Override
    public boolean shouldRegister() {
        return this.selfRegistration;
    }
}
