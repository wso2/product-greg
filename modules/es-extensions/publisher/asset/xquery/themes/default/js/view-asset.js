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
        var parent = $('#xquery-viewer') ? $('#xquery-viewer').parent() : null;
        var widthOfXQueryViewer = null; //Let code mirror determine the width
        var heightOfXQueryViewer = null; //Let code mirror determine the height
        var xqueryRenderingCanvas = '#xquerycontent';
        //If the xquery Viewer exists and a parent container also exists
        //we will inherit the width
        if (parent) {
            widthOfXQueryViewer = parent.width();
        }
        //Attempt to get the rendering canvas
        var canvasArea = $(xqueryRenderingCanvas) ? $(xqueryRenderingCanvas)[0] : null;
        //Only try to render the editor if the canvas is found
        if (canvasArea) {
            var editor = CodeMirror.fromTextArea(canvasArea, {
                mode: "application/xquery+xml",
                lineNumbers: false,
                readOnly: true,
                lineWrapping: true
            });
            editor.setSize(widthOfXQueryViewer, heightOfXQueryViewer);
        }
        else{
        	alert('Error: could not render the xquery code mirror editor since a rendering canavas area was not defined.');
        }
    });
});