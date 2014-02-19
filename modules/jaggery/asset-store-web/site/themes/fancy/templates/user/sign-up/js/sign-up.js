$(document).ready(function() {
    $.validator.addMethod("matchPasswords", function(value) {
		return value == $("#newPassword").val();
	}, "The passwords you entered do not match.");

    $.validator.addMethod('noSpace', function(value, element) {
            return !/\s/g.test(value);
    }, 'Username contains white spaces.');


    $("#sign-up").validate({
     submitHandler: function(form) {
       jagg.post("/site/blocks/user/sign-up/ajax/user-add.jag", {
            action:"addUser",
            username:$('#newUsername').val(),
            password:$('#newPassword').val()
        }, function (result) {
            if (result.error == false) {
                jagg.message({content:"User added successfully. You can now sign into the API store using the new user account.",type:"info",
                    cbk:function() {
                        $('#signUpRedirectForm').submit();
                    }
                });
            } else {
                jagg.message({content:result.message,type:"error"});
            }
        }, "json");
     }
    });
    $("#newPassword").keyup(function() {
        $(this).valid();
    });
    $('#newPassword').focus(function(){
        $('#password-help').show();
        $('.password-meter').show();
    });
    $('#newPassword').blur(function(){
        $('#password-help').hide();
        $('.password-meter').hide();
    });
});