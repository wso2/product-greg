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
 * [contains actions, for creating a schema name when the schema url is selected,
 * to show drop down menu for import & upload of schemas,
 * to call custom api
 * ]
 */
$(function() {
    var uploadUI,importUI;

    //function to set the schema name from the schema url.
    $('input[name="overview_url"]').change(function() {
        var schemaUrl = $('input[name="overview_url"]').val();
        var schemaFileName = "";
        //This check seems unwanted, as registry does not support file
        //upload here.
        if (schemaUrl.indexOf("\\") != -1) {
            schemaFileName = schemaUrl.substring(schemaUrl.lastIndexOf('\\') + 1, schemaUrl.length);
        } else {
            schemaFileName = schemaUrl.substring(schemaUrl.lastIndexOf('/') + 1, schemaUrl.length);
        }
        if (schemaFileName.search(/\.[^?]*$/i) < 0) {
            schemaFileName = schemaFileName.replace("?", ".");
            var suffix = ".xsd";
            if (schemaFileName.indexOf(".") > 0) {
                schemaFileName = schemaFileName.substring(0, schemaFileName.lastIndexOf(".")) + suffix;
            } else {
                schemaFileName = schemaFileName + suffix;
            }
        }
        var notAllowedChars = "!@#;%^*+={}|<>";
        for (i = 0; i < notAllowedChars.length; i ++) {
            var c = notAllowedChars.charAt(i);
            schemaFileName = schemaFileName.replace(c, "_");
        }
        $('input[name="overview_name"]').val(schemaFileName);
        var ajaxURL = caramel.context + '/apis/assets?type=schema&q="name":"' + schemaFileName + '"';
        $.ajax({
            url: ajaxURL,
            type: 'GET',
            success: function (data) {
                if (data.count > 0) {
                    messages.alertInfo("Schemas exist with the same name ");
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
                name = encodeURIComponent($('#schema_file_name').val());
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
            var ajaxURL = caramel.context + '/apis/assets?type=schema&q="name":"' + name +
                '","version":"' + version + '"';
            var resourceExist = false;
            $.ajax({
                async: false,
                url: ajaxURL,
                type: 'GET',
                success: function (data) {
                    if (data.count > 0) {
                        messages.alertError("Schema exist with same name and version");
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
                action = "addNewSchemaFileAssetButton";
            }
            if (action === 'addNewSchemaFileAssetButton') {//upload via file browser
                //call the custom endpoint for processing schema upload via file browser.
                var $schemaFileInput = $('input[name="schema_file"]');
                var schemaFileInputValue = $schemaFileInput.val();
                var schemaFilePath = schemaFileInputValue;

                if(!validator.isValidForm(uploadUI)) {
                    messages.alertInfo("All required fields must be provided");
                    return false;
                }
                var fileName = schemaFilePath.split('\\').reverse()[0];
                //set the zip file name, to the hidden attribute.
                $('#schema_file_name').val(fileName);
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
            messages.alertSuccess("Successfully created the schema");
        },
        error:function(){
            messages.alertError("Error occurred while adding the schema");
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
        $('#form-asset-create').attr('action', caramel.context + '/apis/assets?type=schema');

        $('#addMethodSelector').val('import');
        $('select').on('change', function() {
            var addSelector = $('#addMethodSelector');
            var selectedValue = addSelector.val();
            if (selectedValue == "upload") {
                uploadUI.show();
                importUI.hide();
                validator.initValidationEvents(uploadUI,function(){});
                validator.removeValidationEvents(importUI);
                $('#form-asset-create').attr('action', caramel.context + '/assets/schema/apis/schemas');

            } else if (selectedValue == "import") {
                importUI.show();
                uploadUI.hide();
                validator.initValidationEvents(importUI,function(){});
                validator.removeValidationEvents(uploadUI);
                $('#form-asset-create').attr('action', caramel.context + '/apis/assets?type=schema');
            }
        });
        var fileInput = $('input[name="schema_file"]');
        if(fileInput.val().length > 0){
            var fileName = fileInput.val().split('\\').reverse()[0];
            $('#schema_file_name').val(fileName);
        }
        $('input[name="schema_file"]').change(function(){
            var fileName = fileInput.val().split('\\').reverse()[0];
            $('#schema_file_name').val(fileName);
        })
    };
    initUI();
});
