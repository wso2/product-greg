$(document).ready(function () {
    var application = $("#application-name").val("");

     $.validator.addMethod('validateSpecialChars', function(value, element) {
        return !/(["\'])/g.test(value);
    }, 'Name contains one or more illegal characters ( &nbsp;&nbsp; " &nbsp;&nbsp; \' &nbsp;&nbsp; )');

    $("#appAddForm").validate({
        submitHandler: function(form) {
            applicationAdd();
        }
    });
    var applicationAdd = function(){
        var application = $("#application-name").val();
        var apiPath = $("#apiPath").val();
        var goBack = $("#goBack").val();
        jagg.post("/site/blocks/application/application-add/ajax/application-add.jag", {
            action:"addApplication",
            application:application
        }, function (result) {
            if (result.error == false) {
                var date = new Date();
                date.setTime(date.getTime() + (3 * 1000));
                $.cookie('highlight','true',{ expires: date});
                $.cookie('lastAppName',application,{ expires: date});
                if(goBack == "yes"){
                    jagg.message({content:'Return back to API detail page?',type:'confirm',okCallback:function(){
                         window.location.href = apiViewUrl + "?" +  apiPath;
                    },cancelCallback:function(){
                        window.location.href= appAddUrl;
                    }});
                } else{
                    window.location.reload();
                }

            } else {
                jagg.message({content:result.error,type:"error"});
            }
        }, "json");
    };


    $("#application-name").charCount({
			allowed: 70,
			warning: 50,
			counterText: 'Characters left: '
		});
    $("#application-name").val('');

    /*$('#application-name').keydown(function(event) {
         if (event.which == 13) {
               applicationAdd();
            }
        });*/
});

