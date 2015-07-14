/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var editor;

$(function() {
    $(document).ready(function() {
        //Get the parent of the viewer,which may not exist
        var parent = $('#swagger-viewer') ? $('#swagger-viewer').parent() : null;
        var widthOfSwaggerViewer = null; //Let code mirror determine the width
        var heightOfSwaggerViewer = null; //Let code mirror determine the height
        var swaggerRenderingCanvas = '#swaggercontent';
        //If the swagger Viewer exists and a parent container also exists
        //we will inherit the width
        if (parent) {
            widthOfSwaggerViewer = parent.width();
        }
        //Attempt to get the rendering canvas
        var canvasArea = $(swaggerRenderingCanvas) ? $(swaggerRenderingCanvas)[0] : null;
        //Only try to render the editor if the canvas is found
        if (canvasArea) {
            editor = CodeMirror.fromTextArea(canvasArea, {
                mode: "application/json",
                lineNumbers: false,
                readOnly: true,
                lineWrapping: true
            });
            editor.setSize(widthOfSwaggerViewer, heightOfSwaggerViewer);
            var totalLines = editor.lineCount();
            var totalChars = editor.getTextArea().value.length;
            editor.autoFormatRange({line:0, ch:0}, {line:totalLines, ch:totalChars});
        }
        else{
        	alert('Error: could not render the swagger code mirror editor since a rendering canavas area was not defined.');
        }
    });
});
$("#diff-view-version").on('change',function(){
    var diff_view_url = "/publisher/pages/diff?type=wsdl&path=" + $("#diff-view-version").val() + ',' +
        $("#diff-view-version").find(':selected').data('base_path');
    $("#diff-view-button").attr("href", diff_view_url);
});

$('select.select2').select2({
    dropdownCssClass: 'version-select-drop',
    containerCssClass: 'version-select'
});

