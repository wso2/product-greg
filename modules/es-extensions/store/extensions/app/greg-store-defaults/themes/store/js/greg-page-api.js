var GregPageAPI = {};
$(function() {
	var gregConfigs = {};
	gregConfigs.NOTIFICATION_API = '';
	gregConfigs.SUBSCRIPTION_API = '';
	gregConfigs.COMMENTS_API = '';

    GregPageAPI.getAssetId = function() {
    	return store.store.assetId;
    };
    GregPageAPI.getAssetPath = function() {
    	return store.store.assetPath;
    };
    GregPageAPI.getAssetType = function() {
        return store.store.type;
    };
}());