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
 * [contains actions, for creating a policy name when the policy url is selected,
 * to show drop down menu for import & upload of policies,
 * to call custom api
 * ]
 */
$(function() {
    //function to set the policy name from the policy url.
    $('input[name="overview_url"]').change(function() {
        var policyUrl = $('input[name="overview_url"]').val();
        var policyFileName = "";
        //This check seems unwanted, as registry does not support file
        //upload here.
        if (policyUrl.indexOf("\\") != -1) {
            policyFileName = policyUrl.substring(policyUrl.lastIndexOf('\\') + 1, policyUrl.length);
        } else {
            policyFileName = policyUrl.substring(policyUrl.lastIndexOf('/') + 1, policyUrl.length);
        }
        if (policyFileName.search(/\.[^?]*$/i) < 0) {
            policyFileName = policyFileName.replace("?", ".");
            var suffix = ".xml";
            if (policyFileName.indexOf(".") > 0) {
                policyFileName = policyFileName.substring(0, policyFileName.lastIndexOf(".")) + suffix;
            } else {
                policyFileName = policyFileName + suffix;
            }
        }
        var notAllowedChars = "!@#;%^*+={}|<>";
        for (i = 0; i < notAllowedChars.length; i ++) {
            var c = notAllowedChars.charAt(i);
            policyFileName = policyFileName.replace(c, "_");
        }
        $('input[name="overview_name"]').val(policyFileName);
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
    //function to call the custom policy api or default api.
    $('form[name="form-asset-create"] input[type="submit"]').click(function(event) {
        var $form = $('form[name="form-asset-create"]');
        if ($(this).attr("name") == 'addNewPolicyFileAssetButton') {//upload via file browser
            //call the custom endpoint for processing policy upload via file browser.
            $form.attr('action', caramel.context + '/asts/policy/apis/policies');
            var $policyFileInput = $('input[name="policy_file"]');
            var policyFileInputValue = $policyFileInput.val();
            var policyFilePath = policyFileInputValue;
            var fileName = policyFilePath.split('\\').reverse()[0];
            //set the zip file name, to the hidden attribute.
            $('input[name="policy_file_name"]').val(fileName);
            $form.submit();
        } else if ($(this).attr("name") == 'addNewAssetButton') {//upload via url.
            //call the default endpoint.
            $form.attr('action', caramel.context + '/apis/assets?type=policy');
            $form.submit();
        }
    });
});