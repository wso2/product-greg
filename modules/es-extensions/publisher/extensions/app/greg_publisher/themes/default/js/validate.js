function validate() {

    var serverName = $('#key').val();
    var password = $('#value').val();
    var passwordVerify = $('#valueVerify').val();

    if(password != passwordVerify){
        alert("Re-entered password does not match");
        return false;
    }
    else {
        return true;
    }
}

function populate(serverName){
    document.getElementById("key").value = serverName;
}