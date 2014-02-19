function changeAppNameMode(linkObj){
    var theTr = $(linkObj).parent().parent();
    var appName = $(theTr).attr('data-value');
    $('td:first',theTr).html('<div class="row-fluid"><div class="span6"> <input class="app_name_new" maxlength="70" value="'+theTr.attr('data-value')+'" type="text" /> </div><div class="span6"><button class="btn btn-primary" onclick="updateApplication(this)">Save</button> <button class="btn" onclick="updateApplication_reset(this)">Cancel</button></div></div> ');
    $('input.app_name_new',theTr).focus();
    $('input.app_name_new',theTr).keyup(function(){
        var error = "";
        if($(this).val() == ""){
            error = "This field is required.";
        }else if($(this).val().length>70){
            error = "Name exceeds character limit (70)";
        }else if(/(["\'])/g.test($(this).val())){
            error = 'Name contains one or more illegal characters ( " \' )';
        }
        if(error != ""){
            $(this).addClass('error');
            if(!$(this).next().hasClass('error')){
                $(this).parent().append('<label class="error">'+error+'</label>');
            }else{
                $(this).next().show().html(error);
            }
        }else{
            $(this).removeClass('error');
            $(this).next().hide();
        }
    });
}
function updateApplication_reset(linkObj){
    var theTr = $(linkObj).parent().parent().parent().parent();
    var appName = $(theTr).attr('data-value');
    $('td:first',theTr).html(appName);
}
function updateApplication(linkObj){
    var theTr = $(linkObj).parent().parent().parent().parent();
    var applicationOld = $(theTr).attr('data-value');
    var applicationNew = $('input.app_name_new',theTr).val();
    var error = "";
    if (applicationNew == "") {
        error = "This field is required.";
    } else if (applicationNew.length > 70) {
        error = "Name exceeds character limit (70)";
    } else if (/(["\'])/g.test(applicationNew)) {
        error = 'Name contains one or more illegal characters ( " \' )';
    }
    if(error != ""){
        return;
    }
        jagg.post("/site/blocks/application/application-update/ajax/application-update.jag", {
            action:"updateApplication",
            applicationOld:applicationOld,
            applicationNew:applicationNew
        }, function (result) {
            if (result.error == false) {
                window.location.reload();
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
}

function deleteApp(linkObj) {
    var theTr = $(linkObj).parent().parent();
    var appName = $(theTr).attr('data-value');
    $('#messageModal').html($('#confirmation-data').html());
    $('#messageModal h3.modal-title').html('Confirm Delete');
    $('#messageModal div.modal-body').html('\n\nAre you sure you want to remove the application "' + appName + '"? This will cancel all the existing subscriptions and keys associated with the application.');
    $('#messageModal a.btn-primary').html('Yes');
    $('#messageModal a.btn-other').html('No');
    $('#messageModal a.btn-primary').click(function() {
        jagg.post("/site/blocks/application/application-remove/ajax/application-remove.jag", {
            action:"removeApplication",
            application:appName
        }, function (result) {
            if (!result.error) {
                window.location.reload();
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
    });
    $('#messageModal a.btn-other').click(function() {
        window.location.reload();
    });
    $('#messageModal').modal();

}

function hideMsg() {
    $('#applicationTable tr:last').css("background-color", "");
    $('#appAddMessage').hide("fast");
}
$(document).ready(function() {
    if ($.cookie('highlight') != null && $.cookie('highlight') == "true") {
        $.cookie('highlight', "false");

        $('#applicationTable tr:last').css("background-color", "#d1dce3");
        $('#appAddMessage').show();
        $('#applicationShowName').text($.cookie('lastAppName'));
        var t = setTimeout("hideMsg()", 3000);
    }
});
