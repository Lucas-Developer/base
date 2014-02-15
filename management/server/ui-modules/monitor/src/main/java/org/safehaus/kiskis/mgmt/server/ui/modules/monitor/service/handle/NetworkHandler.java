package org.safehaus.kiskis.mgmt.server.ui.modules.monitor.service.handle;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.elasticsearch.index.query.QueryBuilders.termQuery;

public class NetworkHandler extends Handler {

    public NetworkHandler () {
        super("if_packets", "rx + tx");
    }

    protected void setQueryBuilder(BoolQueryBuilder queryBuilder) {
        queryBuilder
                .must(termQuery("host", "node1"))
                .must(termQuery("collectd_type", "if_packets"));
    }

    protected Map<String, Double> parseHits(SearchHit hits[]) {
        LinkedHashMap<String, Double> map = new LinkedHashMap<String, Double>();

        for (int i = hits.length-1; i >= 0; i--) {
            Map<String, Object> json = hits[i].getSource();
            Integer rx = (Integer) json.get("rx");
            Integer tx = (Integer) json.get("tx");
            map.put(
                    json.get("@timestamp").toString(),
                    (double) (rx + tx)
            );
        }

        return map;
    }

}
