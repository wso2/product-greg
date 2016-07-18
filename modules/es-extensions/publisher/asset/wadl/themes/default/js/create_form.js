/*
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
/**
 * [contains actions, for creating a wadl name when the wadl url is selected,
 * to show drop down menu for import & upload of wadls,
 * to call custom api
 * ]
 */
$(function() {
    var uploadUI,importUI;

    //function to set the wadl name from the wadl url.
    $('input[name="overview_url"]').change(function() {
        var wadlUrl = $('input[name="overview_url"]').val();
        var wadlFileName = "";
        //This check seems unwanted, as registry does not support file
        //upload here.
        if (wadlUrl.indexOf("\\") != -1) {
            wadlFileName = wadlUrl.substring(wadlUrl.lastIndexOf('\\') + 1, wadlUrl.length);
        } else {
            wadlFileName = wadlUrl.substring(wadlUrl.lastIndexOf('/') + 1, wadlUrl.length);
        }
        if (wadlFileName.search(/\.[^?]*$/i) < 0) {
            wadlFileName = wadlFileName.replace("?", ".");
            var suffix = ".wadl";
            if (wadlFileName.indexOf(".") > 0) {
                wadlFileName = wadlFileName.substring(0, wadlFileName.lastIndexOf(".")) + suffix;
            } else {
                wadlFileName = wadlFileName + suffix;
            }
        }
        var notAllowedChars = "!@#;%^*+={}|<>";
        for (i = 0; i < notAllowedChars.length; i ++) {
            var c = notAllowedChars.charAt(i);
            wadlFileName = wadlFileName.replace(c, "_");
        }
        $('input[name="overview_name"]').val(wadlFileName);
        var ajaxURL = caramel.context + '/apis/assets?type=wadl&q="name":"' + wadlFileName + '"';
        $.ajax({
            url: ajaxURL,
            type: 'GET',
            success: function (data) {
                if (data.count > 0) {
                    messages.alertInfo("Wadls exist with the same name ");
                }
            }
        });
    });

    $('#form-asset-create').ajaxForm({
        beforeSubmit:function(){
            var addSelector = $('#addMethodSelector');
            var selectedValue = addSelector.val();
            var version;
            var name;
            if (selectedValue == "upload") {
                name = encodeURIComponent($('#wadl_file_name').val());
                version = $('#file_version').val();
                if (!validator.isValidForm(uploadUI)) {
                    messages.alertError("All required fields must be provided");
                    return false;
                }
            } else {
                name = encodeURIComponent($('input[name="overview_name"]').val());
                version = $('input[name="overview_version"]').val();
                if (!validator.isValidForm(importUI)) {
                    messages.alertError("All required fields must be provided");
                    return false;
                }
            }
            var ajaxURL = caramel.context + '/apis/assets?type=wadl&q="name":"' + name +
                '","version":"' + version + '"';
            var resourceExist = false;
            $.ajax({
                async: false,
                url: ajaxURL,
                type: 'GET',
                success: function (data) {
                    if (data.count > 0) {
                        messages.alertError("Wadl exist with same name and version");
                        resourceExist = true;
                    }

                }
            });
            if (resourceExist) {
                return false;
            }
            var action = "";
            if ($('#importUI').is(":visible")) {
                action = "addNewAssetButton";
            } else if ($('#uploadUI').is(":visible")) {
                action = "addNewWadlFileAssetButton";
            }
            if (action === 'addNewWadlFileAssetButton') {//upload via file browser
                //call the custom endpoint for processing wadl upload via file browser.
                var $wadlFileInput = $('input[name="wadl_file"]');
                var wadlFileInputValue = $wadlFileInput.val();
                var wadlFilePath = wadlFileInputValue;

                if(!validator.isValidForm(uploadUI)) {
                    messages.alertInfo("All required fields must be provided");
                    return false;
                }
                var fileName = wadlFilePath.split('\\').reverse()[0];
                //set the zip file name, to the hidden attribute.
                $('#wadl_file_name').val(fileName);
            } else if (action === 'addNewAssetButton') {//upload via url.
                if(!validator.isValidForm(importUI)) {
                    messages.alertInfo("All required fields must be provided");
                    return false;
                }
            }

            var createButton = $('#form-asset-create input[type="submit"]');
            createButton.attr('disabled', 'disabled');
            createButton.next().attr('disabled', 'disabled');
            caramel.render('loading', 'Creating asset. Please wait..', function (info, content) {
                var $content = $(content).removeClass('loading-animation-big').addClass('loading-animation');
                createButton.parent().append($content);
            });
        },
        success:function(data){
            //$.cookie("new-asset-" + data.type, data.id + ":" + data.type + ":" + data.name);
            window.location=$('#form-asset-create').attr('data-redirect-url');
            messages.alertSuccess("Successfully created the wadl");
        },
        error:function(){
            messages.alertError("Error occurred while adding the wadl");
            var createButton = $('#form-asset-create input[type="submit"]');
            createButton.removeAttr('disabled');
            $('.fa-spinner').parent().remove();
        }
    });
    var initUI = function() {
        //function to display upload or import uis.
        uploadUI = $('#uploadUI');
        importUI = $('#importUI');
        importUI.show();
        validator.initValidationEvents('importUI',function(){});
        $('#form-asset-create').attr('action', caramel.context + '/apis/assets?type=wadl');

        $('#addMethodSelector').val('import');
        $('select').on('change', function() {
            var addSelector = $('#addMethodSelector');
            var selectedValue = addSelector.val();
            if (selectedValue == "upload") {
                uploadUI.show();
                importUI.hide();
                validator.initValidationEvents(uploadUI,function(){});
                validator.removeValidationEvents(importUI);
                $('#form-asset-create').attr('action', caramel.context + '/assets/wadl/apis/wadls');

            } else if (selectedValue == "import") {
                importUI.show();
                uploadUI.hide();
                validator.initValidationEvents(importUI,function(){});
                validator.removeValidationEvents(uploadUI);
                $('#form-asset-create').attr('action', caramel.context + '/apis/assets?type=wadl');
            }
        });
        var fileInput = $('input[name="wadl_file"]');
        if(fileInput.val().length > 0){
            var fileName = fileInput.val().split('\\').reverse()[0];
            $('#wadl_file_name').val(fileName);
        }
        $('input[name="wadl_file"]').change(function(){
            var fileName = fileInput.val().split('\\').reverse()[0];
            $('#wadl_file_name').val(fileName);
        })
    };
    initUI();
});
