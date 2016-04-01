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

// Getting initial load data
window.onload = function () {
    loadInitialTextDiff();
    if (type == "wsdl") {
        loadSectionList();
    }
    else {
        renderPartial("list-default", {}, function(template){
            $('#sectionList').append(template);
        });
    }
};

//var changeMap = {
//    "CONTENT_ADDITION": "Content Additions",
//    "CONTENT_REMOVAL": "Content Removals",
//    "CONTENT_CHANGE": "Content Changes",
//    "CONTENT_TEXT": "Content Text"
//};

var loadContent;
var value, target, orig1, orig2, dv, panes = 2, highlight = true, connect = null, collapse = false;

function loadInitialTextDiff() {
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
    var sectionMap = {
        "wsdl_declaration": "WSDL Declaration Diff",
        "wsdl_imports": "WSDL Imports Diff",
        "wsdl_bindings": "WSDL Bindings Diff",
        "wsdl_messages": "WSDL Messages Diff",
        "wsdl_porttype": "WSDL PortTypes Diff",
        "wsdl_operations": "WSDL Operations Diff",
        "wsdl_service": "WSDL Service Diff",
        "wsdl_ports": "WSDL Ports Diff",
        "default":"Default Complete Text Diff"
    };
    var keys = Object.keys(detailDiffData.sections);
    var sections = [];
    var entry;
    keys.forEach(function(key){
        entry = {};
        entry.name = key;
        entry.title = sectionMap[key];
        entry.data = detailDiffData.sections[key];
        sections.push(entry);
    });

    if (!jQuery.isEmptyObject(sections)) {

        renderPartial("list-section-changes", {sections : sections}, function(template){
            $('#sectionList').append(template);
        });

        //$('#sectionList').append('<h4 class="panel-title">' +
        //    '<a href="#" class="list-group-item active">Sections in ' + type + '</a>' +
        //    '</h4>');

        //for (var sectionName in sections) {
            //$('#sectionList').append('<div class="panel panel-default">' +
            //    '<div class="panel-heading" role="tab" id="heading' + sectionName + '">' +
            //    '<h4 class="panel-title">' +
            //    '<a class="collapsed" role="button" data-toggle="collapse" data-parent="#sectionList" href="#collapse' +
            //    sectionName + '" aria-expanded="false" aria-controls="collapse' + sectionName + '">' +
            //    sectionMap[sectionName] + '</a>' +
            //    '</h4>' +
            //    '</div>' +
            //    '<div class="panel-collapse collapse" role="tabpanel" id="collapse' +
            //    sectionName + '" aria-labelledby="heading' + sectionName + '">' +
            //    '<div class="list-group" id="sectionChanges' + sectionName + '">' +
            //    '</div>' +
            //    '</div>' +
            //    '</div>');
            //if (!jQuery.isEmptyObject(sections[sectionName].content)) {
            //    for (var changeName in sections[sectionName].content) {
                    //$('#sectionChanges' + sectionName).append('<a href="#" class="list-group-item" ' +
                    //    'onclick="loadSectionChangesDiff(\'' + sectionName + '\', \'' + changeName + '\')">' +
                    //    changeMap[changeName] + '</a>');
                //}
            //}
        //}
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

var partial = function(name) {
    return '/extensions/app/greg-diff/themes/' + caramel.themer + '/partials/' + name + '.hbs';
};

var renderPartial = function(partialKey,data, fn) {
    fn = fn || function() {};
    var partialName = partialKey;
    if (!partialName) {
        throw 'A template name has not been specified for template key ' + partialKey;
    }
    var obj = {};
    obj[partialName] = partial(partialName);
    caramel.partials(obj, function() {
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

//// Getting initial load data
//var sections = diffData.sections;
//var init_section_name, init_change_name, initialLoadContent;
//
//if (!jQuery.isEmptyObject(sections)) {
//
//    for (var key in sections) {
//        init_section_name = key;
//        break;
//    }
//
//    if (!jQuery.isEmptyObject(sections[init_section_name].content)) {
//        for (var key2 in sections[init_section_name].content) {
//            init_change_name = key2;
//            break;
//        }
//        if (!jQuery.isEmptyObject(diffData.sections[init_section_name].content[init_change_name])) {
//            initialLoadContent = diffData.sections[init_section_name].content[init_change_name][0];
//        }
//    }
//}
//
//var value, orig1, orig2, dv, panes = 2, highlight = true, connect = null, collapse = false;
//
//function initUI() {
//    if (value == null) return;
//    var target = document.getElementById("diffView");
//    target.innerHTML = "";
//    dv = CodeMirror.MergeView(target, {
//        value: initialLoadContent.content.original,
//        origLeft: panes == 3 ? orig1 : null,
//        orig: orig2,
//        lineNumbers: true,
//        mode: "text/xml",
//        highlightDifferences: highlight,
//        connect: connect,
//        collapseIdentical: collapse,
//        theme: "base16-light"
//    });
//}
//
//// Setting initial load data.
//window.onload = function () {
//    value = document.documentElement.innerHTML;
//    orig2 = initialLoadContent.content.changed;
//    initUI();
//    setViewPanelsHeight();
//    addTitle();
//};
//
