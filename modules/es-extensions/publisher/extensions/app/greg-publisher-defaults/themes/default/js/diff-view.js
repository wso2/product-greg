$("#diff-view-version").on('change',function(){
    var diff_view_url = "/publisher/pages/diff?type=wsdl&path=" + $("#diff-view-version").val() + ',' +
        $("#diff-view-version").find(':selected').data('base_path');
    $("#diff-view-button").attr("href", diff_view_url);
});

$('select.select2').select2({
    dropdownCssClass: 'version-select-drop',
    containerCssClass: 'version-select'
});