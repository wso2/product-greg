$(function() {
    var obtainFormMetaData = function(formId) {
        return $(formId).data();
    };
    $(document).ready(function() {
        $('#form-asset-update').ajaxForm({
            success: function() {
                alert('Updated the asset successfully');
            },
            error: function() {
               var options=obtainFormMetaData('#form-asset-update');
               var string = JSON.stringify(options);
               if(!hasParserError(string)){
                    alert('Updated the asset successfully');
               } 
               else{
                    alert('Unable to update the asset');
               }
            }
        });
    });
    //check whether the update form content can be parsed.
    var hasParserError = function(string){
         try {
             JSON.parse(string);
        } catch (exception) {
            return true;
        }
        return false;
    };
});