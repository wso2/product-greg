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
$(function() {
    $(document).ready(function() {
        //Get the parent of the viewer,which may not exist
        var parent = $('#javascript-viewer') ? $('#javascript-viewer').parent() : null;
        var widthOfJavaScriptViewer = null; //Let code mirror determine the width
        var heightOfJavaScriptViewer = null; //Let code mirror determine the height
        var javascriptRenderingCanvas = '#javascriptcontent';
        //If the javascript Viewer exists and a parent container also exists
        //we will inherit the width
        if (parent) {
            widthOfJavaScriptViewer = parent.width();
        }
        //Attempt to get the rendering canvas
        var canvasArea = $(javascriptRenderingCanvas) ? $(javascriptRenderingCanvas)[0] : null;
        //Only try to render the editor if the canvas is found
        if (canvasArea) {
            var editor = CodeMirror.fromTextArea(canvasArea, {
                mode: "application/javascript",
                lineNumbers: false,
                readOnly: true,
                lineWrapping: true
            });
            editor.setSize(widthOfJavaScriptViewer, heightOfJavaScriptViewer);
        }
        else{
        	alert('Error: could not render the javascript code mirror editor since a rendering canavas area was not defined.');
        }
    });
});