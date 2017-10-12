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
 * [contains actions, for creating a xslt name when the xslt url is selected,
 * to show drop down menu for import & upload of xslts,
 * to call custom api
 * ]
 */
$(function() {
    //function to set the xslt name from the xslt url.
    $('input[name="overview_url"]').change(function() {
        var xsltUrl = $('input[name="overview_url"]').val();
        var xsltFileName = "";
        if (xsltUrl.indexOf("\\") != -1) {
            xsltFileName = xsltUrl.substring(xsltUrl.lastIndexOf('\\') + 1, xsltUrl.length);
        } else {
            xsltFileName = xsltUrl.substring(xsltUrl.lastIndexOf('/') + 1, xsltUrl.length);
        }
        if (xsltFileName.search(/\.[^?]*$/i) < 0) {
            xsltFileName = xsltFileName.replace("?", ".");
            var suffix = ".xslt";
            if (xsltFileName.indexOf(".") > 0) {
                xsltFileName = xsltFileName.substring(0, xsltFileName.lastIndexOf(".")) + suffix;
            } else {
                xsltFileName = xsltFileName + suffix;
            }
        }
        var notAllowedChars = "!@#;%^*+={}|<>";
        for (i = 0; i < notAllowedChars.length; i ++) {
            var c = notAllowedChars.charAt(i);
            xsltFileName = xsltFileName.replace(c, "_");
        }
        $('input[name="overview_name"]').val(xsltFileName);
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
	var doSubmit = function(){
		$('#form-asset-create').ajaxForm({
			success:function(){
				var options=obtainFormMeta('#form-asset-create');
				//alert('Aww snap! '+JSON.stringify(options));
				window.location=options.redirectUrl;
			},
			error:function(){
				alert('Unable to add the asset');
			}	
		});
	};
    //function to call the custom xslt api or default api.
    $('form[name="form-asset-create"] input[type="submit"]').click(function(event) {
        var $form = $('form[name="form-asset-create"]');
        if ($(this).attr("name") == 'addNewXsltFileAssetButton') {//upload via file browser
            //call the custom endpoint for processing xslts upload via file browser.
            $form.attr('action', caramel.context + '/asts/xslt/apis/xslts');
            var $xsltFileInput = $('input[name="xslt_file"]');
            var xsltFileInputValue = $xsltFileInput.val();
            var xsltFilePath = xsltFileInputValue;
            var fileName = xsltFilePath.split('\\').reverse()[0];
            //set the zip file name, to the hidden attribute.
            $('input[name="xslt_file_name"]').val(fileName);
			doSubmit();
        } else if ($(this).attr("name") == 'addNewAssetButton') {//upload via url.
            //call the default endpoint.
            $form.attr('action', caramel.context + '/apis/assets?type=xslt');
            doSubmit();
        }
    });

});