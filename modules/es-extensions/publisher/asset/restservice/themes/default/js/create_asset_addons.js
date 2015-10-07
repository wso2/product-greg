$(function(){
	$('h2.field-title').each(function(){
		if($(this).next().attr('id') != "collapseoverview"){
			$(this).next().removeClass('in');
		}
	})
});
$(function(){
	$('#tag-ui-container').hide();
});