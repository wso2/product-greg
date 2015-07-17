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
 * [contains actions, for creating a swagger name when the swagger url is selected,
 * to show drop down menu for import & upload of swaggers,
 * to call custom api
 * ]
 */
$(function() {
    //function to set the swagger name from the swagger url.
    $('input[name="overview_url"]').change(function() {
        var swaggerUrl = $('input[name="overview_url"]').val();
        var swaggerFileName = "";
        if (swaggerUrl.indexOf("\\") != -1) {
            swaggerFileName = swaggerUrl.substring(swaggerUrl.lastIndexOf('\\') + 1, swaggerUrl.length);
        } else {
            swaggerFileName = swaggerUrl.substring(swaggerUrl.lastIndexOf('/') + 1, swaggerUrl.length);
        }
        if (swaggerFileName.search(/\.[^?]*$/i) < 0) {
            swaggerFileName = swaggerFileName.replace("?", ".");
            var suffix = ".json";
            if (swaggerFileName.indexOf(".") > 0) {
                swaggerFileName = swaggerFileName.substring(0, swaggerFileName.lastIndexOf(".")) + suffix;
            } else {
                swaggerFileName = swaggerFileName + suffix;
            }
        }
        var notAllowedChars = "!@#;%^*+={}|<>";
        for (i = 0; i < notAllowedChars.length; i ++) {
            var c = notAllowedChars.charAt(i);
            swaggerFileName = swaggerFileName.replace(c, "_");
        }
        $('input[name="overview_name"]').val(swaggerFileName);
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
			},
			error:function(){
				alert('Unable to add the asset');
                PublisherUtils.unblockButtons({
                    container:container
                });
			}	
		});
	};

    var styleFix = function(){
        var item = $('#ui-asset-operations-overlay');
        item.css('top','-15px');
    };

    //function to call the custom Swagger api or default api.
    $('form[name="form-asset-create"] input[type="submit"]').click(function(event) {

        var action = $(this).attr("name"); 
        var container;
        var $form = $('form[name="form-asset-create"]');
        if ( action === 'addNewSwaggerFileAssetButton') {//upload via file browser
            //call the custom endpoint for processing Swaggers upload via file browser.
            $form.attr('action', caramel.context + '/assets/swagger/apis/swaggers');
            var $swaggerFileInput = $('input[name="swagger_file"]');
            var swaggerFileInputValue = $swaggerFileInput.val();
            var swaggerFilePath = swaggerFileInputValue;
            var fileName = swaggerFilePath.split('\\').reverse()[0];
            //set the zip file name, to the hidden attribute.
            container = 'saveButtonsFile';
            $('input[name="swagger_file_name"]').val(fileName);
        } else if (action === 'addNewAssetButton') {//upload via url.
            //call the default endpoint.
            container = 'saveButtonsURL';
            $form.attr('action', caramel.context + '/apis/assets?type=swagger');
        }
        PublisherUtils.blockButtons({
            container:container,
            msg:'Creating the '+PublisherUtils.resolveCurrentPageAssetType()+ ' instance'
        });
        styleFix();
        doSubmit(action,container);
    });

});
