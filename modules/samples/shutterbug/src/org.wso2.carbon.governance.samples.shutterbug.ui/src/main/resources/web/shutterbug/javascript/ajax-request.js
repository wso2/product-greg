var AjaxRequestService = AjaxRequestService || {};

AjaxRequestService.deleteImage =
function deleteImage(imageUrl, id) {

    var response = jQuery.ajax({
        type: "POST",
        url: "../resources/delete_ajaxprocessor.jsp",
        data: "pathToDelete=" + imageUrl.split('resource')[1],
        async:   false
    }).responseText;

    //    if(response == "true" || response == "1"){
    $('#td-' + id).remove();
    //    }

    return response == "true" || response == "1";
}

AjaxRequestService.voteForImage =
function voteForImage(imageUrl) {

    var response = jQuery.ajax({
        type: "POST",
        url: "vote-ajaxprocessor.jsp",
        data: "vote=1&imagePath=" + imageUrl,
        async:   false
    }).responseText;

    $('#voteing').html("");
    //    swfobject.embedSWF("http://apps.cooliris.com/embed/cooliris.swf", "wall", "100%", "500", "9.0.0", "", flashvars, params);
    location.reload(true);

    return response == "true" || response == "1";
}

AjaxRequestService.withdrawVoteForImage =
function withdrawVoteForImage(imageUrl) {

    var response = jQuery.ajax({
        type: "POST",
        url: "vote-ajaxprocessor.jsp",
        data: "withdrawVote=1&imagePath=" + imageUrl,
        async:   false
    }).responseText;
    $('#voteing').html("");
    // swfobject.embedSWF("http://apps.cooliris.com/embed/cooliris.swf", "wall", "100%", "500", "9.0.0", "", flashvars, params);
    location.reload(true);
    return response == "true" || response == "1";
}
