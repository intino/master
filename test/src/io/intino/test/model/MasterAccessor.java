package io.intino.test.model;

import com.hazelcast.client.HazelcastClient;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import io.intino.test.core.TriplesRecord;

class MasterAccessor {

    private static final HazelcastInstance HazelCast = HazelcastClient.newHazelcastClient();

    static TriplesRecord get(String type, String id) {
        return getMap(type).get(id);
    }

    static IMap<String, TriplesRecord> getMap(String type) {
        return HazelCast.getMap(type);
    }
}
