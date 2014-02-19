$(document).ready(function () {


    $('.app-key-generate-button').click(function () {
        var elem = $(this);

        jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
            action:"generateApplicationKey",
            application:elem.attr("data-application"),
            keytype:elem.attr("data-keytype")
        }, function (result) {
            if (!result.error) {
                $('table .consumerKey', $(elem).parent().parent()).html(result.data.key.consumerKey);
                $('table .accessToken', $(elem).parent().parent()).html(result.data.key.accessToken);
                $('table .consumerSecret', $(elem).parent().parent()).html(result.data.key.consumerSecret);
                $('.app-key-generate-button', $(elem).parent()).hide();
                var inputId;
                var i = elem.attr("iteration");
                var keyType = elem.attr("data-keytype");
                if (keyType == 'PRODUCTION') {
                    $('.key-table-header', $(elem).parent().parent().parent()).html('').html('Production <a onclick="toggleKey(this)" class="show-hide-key pull-right"><i class="icon-arrow-up icon-white"></i> Hide Keys </a>  <div class="pull-right" style="padding:0 5px;"> | </div> <a class="show-hide-key pull-right" onclick="regenerate(\'' + elem.attr("data-application") + '\',\'' + elem.attr("data-keytype") + '\',\'' + elem.attr("iteration") + '\')"><i class="icon-refresh"></i> Re-generate</a>').show();
                    inputId = $('#prodOldAccessToken' + i);
                } else {
                    $('.key-table-header', $(elem).parent().parent().parent()).html('').html('Sandbox <a onclick="toggleKey(this)" class="show-hide-key pull-right"><i class="icon-arrow-up icon-white"></i> Hide Keys </a>  <div class="pull-right" style="padding:0 5px;"> | </div> <a class="show-hide-key pull-right" onclick="regenerate(\'' + elem.attr("data-application") + '\',\'' + elem.attr("data-keytype") + '\',\'' + elem.attr("iteration") + '\')"><i class="icon-refresh"></i> Re-generate</a>').show();
                    inputId = $('#sandOldAccessToken' + i);
                }
                $('.table-striped', $(elem).parent().parent().parent()).show();
                $('.info-msg', $(elem).parent().parent().parent()).html('The keys are available. Click the "Show Key" button to view.').hide();

                inputId.val(result.data.key.accessToken);
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");

        $(this).html('Please wait...');
    });

});

var regenerate=function(appName,keyType,i,btn) {
    //$('.show-hide-key pull-right').attr('disabled');
    $(btn).prev().show();
    $(btn).hide();
    var elem = $(this);
    var divId;
    var oldAccessToken;
    var inputId;
    if (keyType == 'PRODUCTION') {
        inputId=$('#prodOldAccessToken'+i);
        divId = 'prodTable'+i;
    } else {
        inputId= $('#sandOldAccessToken'+i);
        divId = "sandTable"+i;
    }
    oldAccessToken=inputId.val();
    jagg.post("/site/blocks/subscription/subscription-add/ajax/subscription-add.jag", {
        action:"refreshToken",
        application:appName,
        keytype:keyType,
        oldAccessToken:oldAccessToken
    }, function (result) {
        if (!result.error) {

            $('#'+divId+' .accessToken').text(result.data.key.accessToken);
            $('.app-key-generate-button', $(elem).parent()).hide();
            $('.show-hide-key',$(elem).parent().parent().parent()).html('<i class="icon-refresh"></i> Re-generate <div class="pull-right" style="padding:0 5px;"> | </div> <i class="icon-arrow-up icon-white"></i> Hide Keys ').show();
            $('.table-striped', $(elem).parent().parent().parent()).show();
            $('.info-msg', $(elem).parent().parent().parent()).html('The keys are available. Click the "Show Key" button to view.').hide();
            inputId.val(result.data.key.accessToken);

        } else {
            jagg.message({content:result.message,type:"error"});
        }
        $(btn).prev().hide();$(btn).show();


    }, "json");

    $(this).html('Please wait...');

}

function toggleKey(toggleButton){
    var keyTable = $(toggleButton).parent().parent();

    if($('table',keyTable).is(":visible")){
        $('table',keyTable).hide();
        $('.info-msg',keyTable).show();
        $(toggleButton).html('<i class="icon-arrow-down icon-white"></i> Show Keys');
    }else{
        $('table',keyTable).show();
        $('.info-msg',keyTable).hide();
        $(toggleButton).html('<i class="icon-arrow-up icon-white"></i> Hide Keys');
    }
}
function collapseKeys(index,type,link){

    if(type == 'super'){
        if($('#appDetails'+index+'_super').is(":visible")){
            $('i',link).removeClass('icon-minus').addClass('icon-plus');
        }else{
            $('i',link).removeClass('icon-plus').addClass('icon-minus');
        }
        $('#appDetails'+index+'_super').toggle();
    }else{
        if($('#appDetails'+index).is(":visible")){
            $('i',link).removeClass('icon-minus').addClass('icon-plus');
        }else{
            $('i',link).removeClass('icon-plus').addClass('icon-minus');
        }

        $('#appDetails'+index).toggle();
    }
}

function removeSubscription(id, applicationId, delLink) {
    $('#messageModal').html($('#confirmation-data').html());
    $('#messageModal h3.modal-title').html('Confirm Delete');
    $('#messageModal div.modal-body').html('\n\nAre you sure you want to unsubscribe from <b>"' + apiName+'-'+version + '</b>"?');
    $('#messageModal a.btn-primary').html('Yes');
    $('#messageModal a.btn-other').html('No');
    $('#messageModal a.btn-primary').click(function() {
    jagg.post("/site/blocks/subscription/subscription-remove/ajax/subscription-remove.jag", {
        action:"removeSubscription",
        id:id,
        applicationId:applicationId
    }, function (result) {
        if (!result.error) {
            $('#messageModal').modal("hide");
            $(delLink).parent().parent().parent().parent().parent().remove();
            var master = $($(delLink).attr('data-master-id'));
            if($('.keyListPadding',master).length == 0){
                location.reload();
            }
        } else {

            jagg.message({content:result.message,type:"error"});
        }
    }, "json"); });
    $('#messageModal a.btn-other').click(function() {
        return;
    });
    $('#messageModal').modal();

}