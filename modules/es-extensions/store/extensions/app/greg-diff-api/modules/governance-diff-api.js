var api = {};
(function (api) {
    api.getDetailsDiff = function (base_path, new_path, mediaType) {

    };

    api.getTextDiff = function (base_path, new_path) {
        var ComparatorUtils = Packages.org.wso2.carbon.governance.comparator.utils.ComparatorUtils;
        var comparatorUtils = new ComparatorUtils();
        var comparison = comparatorUtils.getArtifactTextDiff(base_path, new_path);

        var Gson = Packages.com.google.gson.Gson;
        var gson = new Gson();
        var result = gson.toJson(comparison);
        try {
            result = parse(String(result));
        } catch (e) {
            log.error(e);
        }
        print(result);
    }
}(api));