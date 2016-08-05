var SELECT_CONTAINER = '.select-resource',
    SELECT_CONTAINER_CSS = 'select-resource';

$(document).ready(function () {
    $(SELECT_CONTAINER).select2({
        dropdownCssClass: SELECT_CONTAINER_CSS,
        containerCssClass: SELECT_CONTAINER_CSS
    });
});

$(function () {

    // Template of the select options.
    var SELECT_ENTRY_TEMPLATE = "" +
        "<div class='item' data-uuid='{{uuid}}' data-type='{{shortName}}'>" +
        "<div class='text'>" +
        "   <div class='resource-name' data-resource-name='{{text}}'>{{text}}</div>" +
        "   <div class='resource-version' data-resource-version='{{version}}'>{{version}}</div>" +
        "   <div class='resource-type' data-resource-type='{{type}}'>{{shortName}}</div>" +
        "</div>" +
        " <div class='icon'>" +
        "     <i class='fw fw-{{iconClass}}'></i>" +
        " </div>" +
        "</div>" +
        "";
    var ADD_ASSOCIATION_BUTTON_ID = '#addAssociation';

    var associatableURL = function (assetType, associationType, id) {
        return caramel.context + '/apis/association/' + assetType + "/" + associationType + "/" + id;
    };
    var associateURL = function () {
        return caramel.context + '/apis/association';
    };

    var removeAssociationURL = function () {
        return caramel.context + '/apis/association/remove';
    };
    var getAssetType = function () {
        return store.publisher.type;
    };
    var getCurrentAssetId = function () {
        return store.publisher.assetId;
    };

    /**
     *  This is the method that load data to select option
     *
     * @param assetType         G-Reg artifact type.
     * @param associationType   Association type.
     * @param id                UUID of the origin asset.
     */
    var initSelect2 = function (assetType, associationType, id) {
        $(SELECT_CONTAINER).html('');
        $(SELECT_CONTAINER).select2({
            placeholder: "Please select an asset...",
            multiple: false,
            width: "100%",
            ajax: {
                url: associatableURL(assetType, associationType, id),
                dataType: "json",
                delay: 1000,
                data: function (params) {
                    var query = '"name":"' + params.term + '"';
                    return {
                        q: query
                    };
                },
                processResults: function (data) {
                    data.results.forEach(function (entry) {
                        entry.id = entry.uuid;
                    });
                    // Show/Hide Add button
                    if (data.results.length !== 0) {
                        $(ADD_ASSOCIATION_BUTTON_ID).css('display', 'inline-block');
                    } else {
                        $(ADD_ASSOCIATION_BUTTON_ID).hide();
                    }

                    return data;
                },
                cache: true
            },
            minimumInputLength: 2,
            templateResult: function (data) {
                return template(data);
            },
            templateSelection: function (data) {
                return template(data);
            },
            dropdownCssClass: SELECT_CONTAINER_CSS,
            containerCssClass: SELECT_CONTAINER_CSS,
            escapeMarkup: function (m) {
                return m;
            }
        });
    };

    var associationData = function (element) {
        return $(element).data();
    };
    var template = function (data) {
        var ptr = Handlebars.compile(SELECT_ENTRY_TEMPLATE);
        return ptr(data);
    };
    var getTargetAssociation = function () {
        return $('.select2-selection__rendered .item').data();
    };
    var invokeAssociationAPI = function (data) {
        $.ajax({
            url: associateURL(),
            data: JSON.stringify(data),
            type: 'POST',
            contentType: 'application/json',
            success: function (response) {
                if (response.code == 400) {
                    messages.alertInfo(response.message);
                } else {
                    messages.alertSuccess(response.message);
                    setTimeout(function () {
                        location.reload(true);
                    }, 2000);
                }
            },
            error: function () {
                messages.alertError('Error occurred while adding association');
            }
        });

    };

    var invokeRemoveAssociationAPI = function (data) {
        BootstrapDialog.show({
            type: BootstrapDialog.TYPE_WARNING,
            title: 'Warning!',
            message: '<div><i class="fa fa-check"></i> Are you sure you want to delete the association?</div>',
            buttons: [{
                label: 'Yes',
                action: function (dialogItself) {
                    dialogItself.close();
                    $.ajax({
                        url: removeAssociationURL(),
                        data: JSON.stringify(data),
                        type: 'DELETE',
                        contentType: 'application/json',
                        success: function () {
                            messages.alertSuccess('Association removed successfully');
                            setTimeout(function () {
                                location.reload(true);
                            }, 2000);
                        },
                        error: function () {
                            messages.alertError('Error occurred while removing association');
                        }
                    });
                }
            }, {
                label: 'No',
                action: function (dialogItself) {
                    dialogItself.close();
                }
            }]
        });
    };

    var initAddAssociationLogic = function () {
        var fromAssetId = getCurrentAssetId();
        var toAssetId;
        var data = {};
        var targetDetails = {};
        $(ADD_ASSOCIATION_BUTTON_ID).on('click', function () {
            targetDetails = getTargetAssociation();
            data.sourceUUID = fromAssetId;
            data.destUUID = targetDetails.uuid;
            data.sourceType = getAssetType();
            data.destType = targetDetails.type;
            data.associationType = $('#association-type-container > li > a.selected').closest('li').data('association-type');
            invokeAssociationAPI(data);
        });
    };

    var initRemoveAssociationLogic = function () {
        var REMOVE_ASSOCIATION_BUTTON_ID = '.wr-association-operations [data-operation=delete]';
        var fromAssetId = getCurrentAssetId();
        var toAssetId;
        var data = {};
        var targetDetails = {};
        $(REMOVE_ASSOCIATION_BUTTON_ID).on('click', function () {
            data.sourceUUID = fromAssetId;
            data.destUUID = $(this).data('uuid');
            data.sourceType = getAssetType();
            data.destType = $(this).data('resource-shortname');
            data.associationType = $(this).data('resource-associationtype');
            invokeRemoveAssociationAPI(data);
        });
    };
    var init = function () {
        $('#association-type-container > li').each(function () {
            $(this).on('click', function () {

                $('#step2').removeClass('disabled-area');
                $('#step2 select').attr('disabled', false);

                $('#association-type-container > li > a').removeClass('selected, disabled');

                var meta = associationData(this);
                var assetType = getAssetType();
                var id = getCurrentAssetId();
                if (!meta.associationType) {
                    throw 'Unable to locate the association type for the selected association';
                }
                // Init Select2 and make the API call here
                initSelect2(assetType, meta.associationType, id);

                $('a', this).addClass('selected');
                $(this).siblings('li').find('a').addClass('disabled');

            });
        });

    };

    init();
    initAddAssociationLogic();
    initRemoveAssociationLogic();
});