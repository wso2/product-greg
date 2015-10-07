/*
 *  Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * [contains actions, for creating a wsdl name when the wsdl url is selected,
 * to show drop down menu for import & upload of wsdls,
 * to call custom api
 * ]
 */
$(function() {
    //function to set the wsdl name from the wsdl url.
    $('input[name="overview_url"]').change(function() {
        var wsdlUrl = $('input[name="overview_url"]').val();
        var wsdlFileName = "";
        if (wsdlUrl.indexOf("\\") != -1) {
            wsdlFileName = wsdlUrl.substring(wsdlUrl.lastIndexOf('\\') + 1, wsdlUrl.length);
        } else {
            wsdlFileName = wsdlUrl.substring(wsdlUrl.lastIndexOf('/') + 1, wsdlUrl.length);
        }
        if (wsdlFileName.search(/\.[^?]*$/i) < 0) {
            wsdlFileName = wsdlFileName.replace("?", ".");
            var suffix = ".wsdl";
            if (wsdlFileName.indexOf(".") > 0) {
                wsdlFileName = wsdlFileName.substring(0, wsdlFileName.lastIndexOf(".")) + suffix;
            } else {
                wsdlFileName = wsdlFileName + suffix;
            }
        }
        var notAllowedChars = "!@#;%^*+={}|<>";
        for (i = 0; i < notAllowedChars.length; i ++) {
            var c = notAllowedChars.charAt(i);
            wsdlFileName = wsdlFileName.replace(c, "_");
        }
        $('input[name="overview_name"]').val(wsdlFileName);
    });
    //function to display upload or import uis.
    var uploadUI = $('#uploadUI');
    var importUI = $('#importUI');
    importUI.show();

    $('select').on('change', function() {
        var addSelector = $('#addMethodSelector');
        var selectedValue = addSelector.val();
        if (selectedValue == "upload") {
            uploadUI.show();
            importUI.hide();
        } else if (selectedValue == "import") {
            importUI.show();
            uploadUI.hide();
        }
    });

	var obtainFormMeta=function(formId){
	   return $(formId).data();
	};

	var doSubmit = function(action,container){
		$('#form-asset-create').ajaxForm({
			success:function(){
				var options=obtainFormMeta('#form-asset-create');
				window.location=options.redirectUrl;
                messages.alertSuccess("Successfully created the wsdl");
                $('form[name="form-asset-create"]').data('submitted', false);
			},
			error:function(){
                messages.alertError("Error occurred while adding the wsdl");

                var createButton = "";
                if(action === 'addNewWsdlFileAssetButton') {
                    createButton = $('#btn-create-asset-file');
                } else if(action === 'addNewAssetButton') {
                    createButton = $('#btn-create-asset');
                }

                createButton.show();
                createButton.next().show();
                $('.fa-spinner').parent().remove();
                $('form[name="form-asset-create"]').data('submitted', false);
			}	
		});
	};

    var styleFix = function(){
        var item = $('#ui-asset-operations-overlay');
        item.css('top','-15px');
    };

    //function to call the custom wsdl api or default api.
    $('form[name="form-asset-create"] input[type="submit"]').click(function(event) {
        var action = "";
        if ($('#importUI').is(":visible")) {
            action = "addNewAssetButton";
        } else if ($('#uploadUI').is(":visible")) {
            action = "addNewWsdlFileAssetButton";
        }

        var container;
        var $form = $('form[name="form-asset-create"]');
        if (action === 'addNewWsdlFileAssetButton') {//upload via file browser
            //call the custom endpoint for processing wsdls upload via file browser.
            $form.attr('action', caramel.context + '/assets/wsdl/apis/wsdls');
            var $wsdlFileInput = $('input[name="wsdl_file"]');
            var wsdlFileInputValue = $wsdlFileInput.val();
            var wsdlFilePath = wsdlFileInputValue;

            var wsdlFileVersion = $('input[name="file_version"]').val();
            if(wsdlFileVersion == "" || wsdlFilePath == "") {
                messages.alertInfo("All required fields must be provided");
                return false;
            }

            if($form.data('submitted') === true) {
                return false;
            } else {
                $form.data('submitted', true);
            }

            var fileName = wsdlFilePath.split('\\').reverse()[0];
            //set the zip file name, to the hidden attribute.
            container = 'saveButtonsFile';
            $('input[name="wsdl_file_name"]').val(fileName);
        } else if (action === 'addNewAssetButton') {//upload via url.
            var wsdlUrl = $('input[name="overview_url"]').val();
            var wsdlFileName = $('input[name="overview_name"]').val();
            var wsdlVersion = $('input[name="overview_version"]').val();

            if(wsdlUrl == "" || wsdlFileName == "" || wsdlVersion == "") {
                messages.alertInfo("All required fields must be provided");
                return false;
            }

            if($form.data('submitted') === true) {
                return false;
            } else {
                $form.data('submitted', true);
            }

            //call the default endpoint.
            container = 'saveButtonsURL';
            $form.attr('action', caramel.context + '/apis/assets?type=wsdl');
        }

        doSubmit(action,container);

        var createButton = "";
        if(action === 'addNewWsdlFileAssetButton') {
            createButton = $('#btn-create-asset-file');
        } else if(action === 'addNewAssetButton') {
            createButton = $('#btn-create-asset');
        }

        createButton.hide();
        createButton.next().hide();
        createButton.parent().append($('<div style="font-size: 16px;margin-top: 10px;"><i class="fa fa-spinner fa-pulse"></i> Creating the wsdl instance...</div>'));
    });

});
