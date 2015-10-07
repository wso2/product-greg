var SELECT_CONTAINER = '.select-resource',
    SELECT_CONTAINER_CSS = 'select-resource';

$(document).ready(function(){
    $(SELECT_CONTAINER).select2({
        dropdownCssClass: SELECT_CONTAINER_CSS,
        containerCssClass: SELECT_CONTAINER_CSS
    });
});

$(function() {
	
	var SELECT_ENTRY_TEMPLATE = "" +
		"<div class='item' data-uuid='{{uuid}}' data-type='{{shortName}}'>" +
		"<div class='text'>" +
		"   <div class='resource-name' data-resource-name='{{text}}'>{{text}}</div>" +
		"   <div class='resource-type' data-resource-type='{{type}}'>{{type}}</div>" +
		"</div>" +
		" <div class='icon'>" +
		"     <i class='fw fw-{{iconClass}}'></i>" +
		" </div>" +
		"</div>" +
		"";
	var ADD_ASSOCIATION_BUTTON_ID = '#addAssociation';

	var associatableURL = function(assetType, associationType, id) {
		return caramel.context + '/apis/association/' + assetType + "/" + associationType + "/" + id;
	};
	var associateURL = function(){
		return caramel.context+'/apis/association';
	};

	var removeAssociationURL = function(){
		return caramel.context+'/apis/association/remove';
	};
	var getAssetType = function() {
		return store.publisher.type;
	};
	var getCurrentAssetId = function(){
		return store.publisher.assetId;
	};
	var loadAssociationTargets = function(assetType, associationType, id) {
		var promise = $.ajax({
			url: associatableURL(assetType, associationType, id)
		});
		promise.done(function(data) {
			renderSelect2Box(data);
            if($(SELECT_CONTAINER + ' option').length !== 0){
                $(ADD_ASSOCIATION_BUTTON_ID).css('display', 'inline-block');
            }
            else {
                $(ADD_ASSOCIATION_BUTTON_ID).hide();
            }
		});
		promise.fail(function() {
			//Do the error handling here
		});
	};
	var associationData = function(element) {
		return $(element).data();
	};
	var template = function(data) {
		var ptr = Handlebars.compile(SELECT_ENTRY_TEMPLATE);
		return ptr(data);
	};
	var getTargetAssociation= function(){
		return $('.select2-selection__rendered .item').data();
	};
	var invokeAssociationAPI = function(data){
        $.ajax({
            url: associateURL(),
            data: JSON.stringify(data),
            type: 'POST',
            contentType: 'application/json',
            success: function () {
                BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_SUCCESS,
                    title: 'Success!',
                    message: '<div><i class="fa fa-check"></i> Association added successfully</div>',
                    buttons: [{
                        label: 'OK',
                        action: function (dialogItself) {
                            dialogItself.close();
                            location.reload(true);
                        }
                    }]

                });
            },
            error: function () {
                BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_DANGER,
                    title: 'Error!',
                    message: '<div><i class="fa fa-warning"></i> Error occurred while adding association</div>',
                    buttons: [{
                        label: 'Close',
                        action: function (dialogItself) {
                            dialogItself.close();
                        }

                    }]

                });
            }
        });

	};

    var invokeRemoveAssociationAPI = function (data) {
        $.ajax({
            url: removeAssociationURL(),
            data: JSON.stringify(data),
            type: 'DELETE',
            contentType: 'application/json',
            success: function () {
                BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_SUCCESS,
                    title: 'Success!',
                    message: '<div><i class="fa fa-check"></i> Association removed successfully</div>',
                    buttons: [{
                        label: 'OK',
                        action: function (dialogItself) {
                            dialogItself.close();
                            location.reload(true);
                        }
                    }]

                });
            },
            error: function () {
                BootstrapDialog.show({
                    type: BootstrapDialog.TYPE_DANGER,
                    title: 'Error!',
                    message: '<div><i class="fa fa-warning"></i> Error occurred while removing association</div>',
                    buttons: [{
                        label: 'Close',
                        action: function (dialogItself) {
                            dialogItself.close();
                        }

                    }]

                });
            }
        });
    };

	var initAddAssociationLogic = function(){
		var fromAssetId = getCurrentAssetId();
		var toAssetId;
		var data = {};
		var targetDetails = {};
		$(ADD_ASSOCIATION_BUTTON_ID).on('click',function(){
			targetDetails = getTargetAssociation();
			data.sourceUUID = fromAssetId;
			data.destUUID = targetDetails.uuid;
			data.sourceType = getAssetType();
			data.destType = targetDetails.type;
			data.associationType = $('#association-type-container > li > a.selected').closest('li').data('association-type');
			invokeAssociationAPI(data);
		});
	};

	var initRemoveAssociationLogic = function(){
		var REMOVE_ASSOCIATION_BUTTON_ID = '.wr-association-operations [data-operation=delete]';
		var fromAssetId = getCurrentAssetId();
		var toAssetId;
		var data = {};
		var targetDetails = {};
		$(REMOVE_ASSOCIATION_BUTTON_ID).on('click',function(){
			data.sourceUUID = fromAssetId;
			data.destUUID = $(this).data('uuid');
			data.sourceType = getAssetType();
			data.destType = $(this).data('resource-shortname');
			data.associationType = $(this).data('resource-associationtype');
			invokeRemoveAssociationAPI(data);
		});
	};
	var init = function() {
		$('#association-type-container > li').each(function() {
			$(this).on('click', function() {
                
                $('#step2').removeClass('disabled-area');
                $('#step2 select').attr('disabled', false);

				$('#association-type-container > li > a').removeClass('selected, disabled');

				var meta = associationData(this);
				var assetType = getAssetType();
				var id = getCurrentAssetId();
				if (!meta.associationType) {
					throw 'Unable to locate the association type for the selected association';
				}
				//Make the API call here
				loadAssociationTargets(assetType, meta.associationType, id);
                
				$('a', this).addClass('selected');
				$(this).siblings('li').find('a').addClass('disabled');

			});
		});

	};
	/**
	 * Formats the provided data into a format that can be used
	 * with the select2 plugin.The select2 plugin excepts the index
	 * and a text value to be provided in the data set (which must be an array)
	 * @param  {[type]} data [description]
	 * @return {[type]}      [description]
	 */
	var formatSelect2Data = function(data) {
		var output = data.results;
		for (var index = 0; index < output.length; index++) {
			output[index].id = index;
		}
		return data;
	};
	var renderSelect2Box = function(data) {
		data = formatSelect2Data(data);
		$(SELECT_CONTAINER).html('');
		$(SELECT_CONTAINER).select2({
			placeholder: 'NO ASSET FOUND',
			data: data.results,
			multiple: false,
			width: "100%",
			templateResult: function(result) {
				return template(result)
			},
			templateSelection: function(result) {
				return template(result)
			},
            dropdownCssClass: SELECT_CONTAINER_CSS,
            containerCssClass: SELECT_CONTAINER_CSS,
			escapeMarkup: function(m) {
				return m;
			}
		});

	};

	init();
	initAddAssociationLogic();
	initRemoveAssociationLogic();
});