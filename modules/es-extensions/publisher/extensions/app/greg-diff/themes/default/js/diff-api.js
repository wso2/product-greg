/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var diffData = {};
// Get full diff with two resources
function getUrlParameter(sParam) {
    var sPageURL = window.location.search.substring(1);
    var sURLVariables = sPageURL.split('&');
    for (var i = 0; i < sURLVariables.length; i++) {
        var sParameterName = sURLVariables[i].split('=');
        if (sParameterName[0] == sParam) {
            return sParameterName[1];
        }
    }
}

var paths = getUrlParameter('path').split(',');
var type = getUrlParameter('type');

$.ajax({
    url: caramel.context + '/apis/governance-artifacts/diff-text?targets=' + paths[1] + ',' + paths[0],
    type: 'GET',
    async: false,
    success: function (response) {
        diffData = JSON.parse(response);
    },
    error: function () {
        console.log("Error getting content.");
    }
});

// Getting initial load data
var sections = diffData.sections;
var init_section_name, init_change_name, initialLoadContent;

if (!jQuery.isEmptyObject(sections)) {

    for (var key in sections) {
        init_section_name = key;
        break;
    }

    if (!jQuery.isEmptyObject(sections[init_section_name].content)) {
        for (var key2 in sections[init_section_name].content) {
            init_change_name = key2;
            break;
        }
        if (!jQuery.isEmptyObject(diffData.sections[init_section_name].content[init_change_name])) {
            initialLoadContent = diffData.sections[init_section_name].content[init_change_name][0];
        }
    }
}

var value, orig1, orig2, dv, panes = 2, highlight = true, connect = null, collapse = false;

function initUI() {
    if (value == null) return;
    var target = document.getElementById("diffView");
    target.innerHTML = "";
    dv = CodeMirror.MergeView(target, {
        value: initialLoadContent.content.original,
        origLeft: panes == 3 ? orig1 : null,
        orig: orig2,
        lineNumbers: true,
        mode: "text/xml",
        highlightDifferences: highlight,
        connect: connect,
        collapseIdentical: collapse,
        theme: "base16-light"
    });
}

// Setting initial load data.
window.onload = function () {
    value = document.documentElement.innerHTML;
    orig2 = initialLoadContent.content.changed;
    initUI();
    setViewPanelsHeight();
    addTitle();
};

