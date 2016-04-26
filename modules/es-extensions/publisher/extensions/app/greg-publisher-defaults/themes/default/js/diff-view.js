$("#diff-view-version li").on('mouseover',function(event){

    var id = $(this).parents('.select').attr('id');
    var version = $(this).children().text();
    var path = $(this).children().attr("id");
    var base_path = $(this).children().data("base_path");
    var selected_option = $('#'+id+' .selected');
    selected_option.text(version);
    selected_option.attr('data-selected_base_path',path);

    var assetType = store.publisher.type;
    var diff_view_url = "/publisher/pages/diff?type=" + assetType + "&path=" + path + ',' + base_path;
    $("#diff-view-button").attr("href", diff_view_url);
});


$('select.select2').select2({
    dropdownCssClass: 'version-select-drop',
    containerCssClass: 'version-select'
});