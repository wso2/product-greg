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
var detailDiffData = {};
var textDiffData = {};
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

// Getting initial load data
window.onload = function () {
    loadInitialTextDiff();
    if ("wsdl" == type) {
        loadSectionList();
    }
    else {
        renderPartial("list-default", {}, function (template) {
            $('#sectionList').append(template);
        });
    }
};

var loadContent;
var value, target, orig1, orig2, dv, panes = 2, highlight = true, connect = null, collapse = false;

function loadInitialTextDiff() {
    $.ajax({
        url: caramel.context + '/apis/governance-artifacts/diff-text?targets=' + paths[1] + ',' + paths[0] + '&type='
        + type,
        type: 'GET',
        async: false,
        success: function (response) {
            textDiffData = JSON.parse(response);
        },
        error: function () {
            //console.log("Error getting content.");
        }
    });

    var textDiffSections = textDiffData.sections;
    var sectionName, changeName;
    if (!jQuery.isEmptyObject(textDiffSections)) {
        for (var key in textDiffSections) {
            sectionName = key;
            break;
        }
        if (!jQuery.isEmptyObject(textDiffSections[sectionName].content)) {
            for (var key2 in textDiffSections[sectionName].content) {
                changeName = key2;
                break;
            }
            if (!jQuery.isEmptyObject(textDiffData.sections[sectionName].content[changeName])) {
                loadContent = textDiffData.sections[sectionName].content[changeName][0];
                var sectionChange = textDiffData.sections[sectionName].sectionSummary[changeName][0];
            }
        }
    }
    value = document.documentElement.innerHTML;
    if (value == null) return;
    target = document.getElementById("diffView");
    target.innerHTML = "";
    orig2 = loadContent.content.changed;
    initUI();
    setViewPanelsHeight();
    addTitle(sectionChange);
}

function loadSectionList() {
    $.ajax({
        url: caramel.context + '/apis/governance-artifacts/diff-detail?targets=' + paths[1] + ',' + paths[0] + '&type='
        + type,
        type: 'GET',
        async: false,
        success: function (response) {
            detailDiffData = JSON.parse(response);
        },
        error: function () {
            //console.log("Error getting content.");
        }
    });

    var sectionMap = {
        "wsdl_declaration": "WSDL Declaration Diff",
        "wsdl_imports": "WSDL Imports Diff",
        "wsdl_bindings": "WSDL Bindings Diff",
        "wsdl_messages": "WSDL Messages Diff",
        "wsdl_porttype": "WSDL PortTypes Diff",
        "wsdl_operations": "WSDL Operations Diff",
        "wsdl_service": "WSDL Service Diff",
        "wsdl_ports": "WSDL Ports Diff",
        "default": "Default Complete Text Diff"
    };
    var keys = Object.keys(detailDiffData.sections);
    var sections = [];
    var entry;
    keys.forEach(function (key) {
        entry = {};
        entry.name = key;
        entry.title = sectionMap[key];
        entry.data = detailDiffData.sections[key];
        sections.push(entry);
    });

    if (!jQuery.isEmptyObject(sections)) {
        renderPartial("list-section-changes", {sections: sections}, function (template) {
            $('#sectionList').append(template);
        });
    }
    else {
        renderPartial("list-default", {}, function (template) {
            $('#sectionList').append(template);
        });
    }
}

function loadSectionChangesDiff(sectionName, changeName) {
    value = document.documentElement.innerHTML;
    if (value == null) return;
    target = document.getElementById("diffView");
    target.innerHTML = "";
    if (!jQuery.isEmptyObject(detailDiffData.sections[sectionName].content[changeName])) {
        var sectionChange = detailDiffData.sections[sectionName].sectionSummary[changeName][0];
        loadContent = detailDiffData.sections[sectionName].content[changeName][0];
        if ("CONTENT_ADDITION" == changeName) {
            orig2 = loadContent.content;
            initUIAddition();
        } else if ("CONTENT_REMOVAL" == changeName) {
            orig2 = "";
            initUIRemoval();
        } else {
            orig2 = loadContent.content.changed;
            initUI();
        }
        setViewPanelsHeight();
        addTitle(sectionChange);
    }
}

var partial = function (name) {
    return '/extensions/app/greg-diff/themes/' + caramel.themer + '/partials/' + name + '.hbs';
};

var renderPartial = function (partialKey, data, fn) {
    fn = fn || function () {
        };
    var partialName = partialKey;
    if (!partialName) {
        throw 'A template name has not been specified for template key ' + partialKey;
    }
    var obj = {};
    obj[partialName] = partial(partialName);
    caramel.partials(obj, function () {
        var template = Handlebars.partials[partialName](data);
        fn(template);
    });
};

function initUI() {
    dv = CodeMirror.MergeView(target, {
        value: loadContent.content.original,
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

function initUIAddition() {
    dv = CodeMirror.MergeView(target, {
        value: "",
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

function initUIRemoval() {
    dv = CodeMirror.MergeView(target, {
        value: loadContent.content,
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
