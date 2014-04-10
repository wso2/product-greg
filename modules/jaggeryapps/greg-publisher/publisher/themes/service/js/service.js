$(function(){

    $('#collapseBasic').collapse('show');

    $("#basic_name").keyup(function(e) {onFieldContentChange('basic_name');});
    $("#basic_name").change(function(e) {onFieldContentChange('basic_name');});

    $("#basic_url").keyup(function(e) {onFieldContentChange('basic_url');});
    $("#basic_url").change(function(e) {onFieldContentChange('basic_url');});

    $("#basic_filepath").keyup(function(e) {onFieldContentChange('basic_filepath');});
    $("#basic_filepath").change(function(e) {onFieldContentChange('basic_filepath');});


    function onFieldContentChange(field){
        //alert(field);
        if(field == 'basic_name'){
                //alert($("#overview_name").val() );
                if($("#basic_name").val().length > 0){
                    handleOtherFields(1);
                }else {
                    handleOtherFields(4);
                }
        }   else if(field == 'basic_url'){
                //alert($("#interface_wsdlUrl").val().length);
                if($("#basic_url").val().length > 0){
                    handleOtherFields(2);
                }else {
                    handleOtherFields(4);
                }
        }   else if(field == 'basic_filepath'){
                if($("#basic_filepath").val().length > 0){
                    handleOtherFields(3);
                }else {
                    handleOtherFields(4);
                }
        }   else{
                alert(4);
        }
    }

    function handleOtherFields(point){
        switch(point)
        {
            case 1:
                $("#basic_url").val('');
                $("#basic_filepath").val('');
                $("#basic_filepath").attr("disabled", "disabled");
                $("#basic_url").attr("disabled", "disabled");
                break;
            case 2:
                $("#basic_name").val('');
                $("#basic_filepath").val('');
                $("#basic_filepath").attr("disabled", "disabled");
                $("#basic_name").attr("disabled", "disabled");
                $("#advance_namespace").attr("disabled", "disabled");
                break;
            case 3:
                $("#basic_name").val('');
                $("#basic_url").val('');
                $("#basic_name").attr("disabled", "disabled");
                $("#basic_url").attr("disabled", "disabled");
                $("#advance_namespace").attr("disabled", "disabled");
                break;
            default:
                $("#basic_name").removeAttr("disabled");
                $("#basic_url").removeAttr("disabled");
                $("#basic_filepath").removeAttr("disabled");
                $("#advance_namespace").removeAttr("disabled");

        }
    }

});
