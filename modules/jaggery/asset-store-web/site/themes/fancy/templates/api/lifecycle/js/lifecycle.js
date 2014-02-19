function invokeAspect(id, action) {
    var items = "", i = 0;
    var checkItemPrefix = '#checkItem';
    if (action === "voteClick") {
        checkItemPrefix = '#voteItem';
    }
    while($(checkItemPrefix + i).val()) {
        items += ($(checkItemPrefix + i).attr("checked") == "checked") + ";";
        i++;
    }
    if (items.length > 0) {
        items = items.substring(0, items.length - 1);
    }
    jagg.post("/site/blocks/api/lifecycle/ajax/invoke.jag",
            { action:action, id:id, items:items},
            function (result) {
                if (result.error) {
                    if (result.message == "AuthenticateError") {
                        jagg.showLogin();
                    } else {
                        jagg.message({content:result.message, type:"error"});
                    }
                } else {
                    var index = window.location.href.indexOf("&");
                    window.location = (index > 0 ? window.location.href.substring(0, index) : window.location.href) + "&tab=2";
                }
            }, "json");
}