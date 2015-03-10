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
//javascript to render the wsdl content view size according to the size of it's
//parent container.
$(function() {
    $(document).ready(function() {
        //Get the parent of the viewer,which may not exist
        var parent = $('#schema-viewer') ? $('#schema-viewer').parent() : null;
        var widthOfSchemaViewer = null; //Let code mirror determine the width
        var heightOfSchemaViewer = null; //Let code mirror determine the height
        var schemaRenderingCanvas = '#schemacontent';
        //If the Schema Viewer exists and a parent container also exists
        //we will inherit the width
        if (parent) {
            widthOfSchemaViewer = parent.width();
        }
        //Attempt to get the rendering canvas
        var canvasArea = $(schemaRenderingCanvas) ? $(schemaRenderingCanvas)[0] : null;
        //Only try to render the editor if the canvas is found
        if (canvasArea) {
            var editor = CodeMirror.fromTextArea(canvasArea, {
                mode: "application/xml",
                lineNumbers: false,
                readOnly: true,
                lineWrapping: true
            });
            editor.setSize(widthOfSchemaViewer, heightOfSchemaViewer);
        }
    });
});