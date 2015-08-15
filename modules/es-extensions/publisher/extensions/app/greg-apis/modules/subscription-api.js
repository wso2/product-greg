var api = {};
(function(api) {
    var carbon = require('carbon');
    var service = function() {
        var eventingService = carbon.server.osgiService('org.wso2.carbon.registry.eventing.services.EventingService');
        return eventingService;
    };
    api.getSubscriptions = function() {
        var listOfEvent = service().getAllSubscriptions();
        var iter = listOfEvent.listIterator();
        var item;
        var entry;
        /*for(var key in iter){
        	log.info('key: '+key);
        }*/
        while(iter.hasNext()){
        	item = iter.next();
        	/*log.info(item.getId());
        	log.info(item.getTopicName());
        	log.info(item.getEventSinkURL());
        	log.info(item.getExpires());
        	log.info(item.getEventDispatcherName());
        	log.info(item.getOwner());
        	log.info(item.getTenantDomain());
        	log.info(item.getTenantId());
        	log.info(item.getMode());
        	log.info(item.getCreatedTime().toString());*/

        }
        return listOfEvent;
    };
}(api));