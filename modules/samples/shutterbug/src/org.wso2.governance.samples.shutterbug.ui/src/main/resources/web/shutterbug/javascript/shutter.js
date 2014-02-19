function fillResourceUploadDetails() {
    var filepath = document.getElementById('uResourceFile').value;

    var filename = "";
    if (filepath.indexOf("\\") != -1) {
        filename = filepath.substring(filepath.lastIndexOf('\\') + 1, filepath.length);
    } else {
        filename = filepath.substring(filepath.lastIndexOf('/') + 1, filepath.length);
    }

    document.getElementById('uResourceName').value = filename;
}

function validateDesc() {
    if (!document.getElementById('uResourceFile').value) {
        $("#dialog").html("<p>You need to select an image</p>");
        showMsgDialog();
        return false;
    } else if (!document.getElementById('uResourceName').value) {
        $("#dialog").html("<p>You need to enter a name for the image</p>");
        showMsgDialog();
        return false;
    } else if (!document.getElementById('description').value) {
        $("#dialog").html("<p>You need to enter a description for the image</p>");
        showMsgDialog();
        return false;
    }
    return true;
}

function showMsgDialog() {
    $("#dialog").dialog({
        close:function() {
            window.location = "shutterbug-ajaxprocessor.jsp";
        },
        buttons:{
            "OK":function() {
                window.location = "shutterbug-ajaxprocessor.jsp";
            }
        },
        height:160,
        width:450,
        minHeight:160,
        minWidth:330,
        modal:true
    });
}
