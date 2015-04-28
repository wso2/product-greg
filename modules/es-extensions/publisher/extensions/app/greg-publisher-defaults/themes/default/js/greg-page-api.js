var GregPageAPI = {};
$(function() {
	var gregConfigs = {};
	gregConfigs.NOTIFICATION_API = '';
	gregConfigs.SUBSCRIPTION_API = '';
	gregConfigs.COMMENTS_API = '';

    GregPageAPI.getAssetId = function() {
    	return store.publisher.assetId;
    };
    GregPageAPI.getAssetPath = function() {
    	return store.publisher.assetPath;
    };
    GregPageAPI.getAssetType = function() {
        return store.publisher.type;
    };
}());