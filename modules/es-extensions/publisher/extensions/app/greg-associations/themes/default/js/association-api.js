$(function() {

	var SELECT_CONTAINER = '.select-resource';
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

	var associatableURL = function(assetType, associationType) {
		return caramel.context + '/apis/association/' + assetType + "/" + associationType;
	};
	var associateURL = function(){
		return caramel.context+'/apis/association';
	};
	var getAssetType = function() {
		return store.publisher.type;
	};
	var getCurrentAssetId = function(){
		return store.publisher.assetId;
	};
	var loadAssociationTargets = function(assetType, associationType) {
		var promise = $.ajax({
			url: associatableURL(assetType, associationType)
		});
		promise.done(function(data) {
			//console.log(data);
			renderSelect2Box(data);
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
			url:associateURL(),
			data:JSON.stringify(data),
			type:'POST',
			contentType:'application/json',
			success:function(){
				alert('association added successfully');
			},
			error:function(){
				alert('Error')
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
	var init = function() {
		$('#association-type-container > li').each(function() {
			$(this).on('click', function() {

				$('#association-type-container > li > a').removeClass('selected, disabled');

				var meta = associationData(this);
				var assetType = getAssetType();
				if (!meta.associationType) {
					throw 'Unable to locate the association type for the selected association';
				}
				//Make the API call here
				loadAssociationTargets(assetType, meta.associationType);

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
			placeholder: 'Select Asset',
			data: data.results,
			multiple: false,
			width: "100%",
			templateResult: function(result) {
				return template(result)
			},
			templateSelection: function(result) {
				return template(result)
			},
			escapeMarkup: function(m) {
				return m;
			}
		});

	};

	init();
	initAddAssociationLogic();
});