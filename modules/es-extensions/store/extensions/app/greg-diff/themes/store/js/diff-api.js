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
$(document).ready(function () {
    var paths = getUrlParameter('path').split(',');
    var type = getUrlParameter('type');
    var domain = resolveDomain();
    var detailDiffData = {};
    var CODEMIRROR_OPTIONS = {};
    CODEMIRROR_OPTIONS.loadContent = null;
    CODEMIRROR_OPTIONS.orig1 = null;
    CODEMIRROR_OPTIONS.orig2 = null;
    CODEMIRROR_OPTIONS.dv = null;
    CODEMIRROR_OPTIONS.panes = 2;
    CODEMIRROR_OPTIONS.highlight = true;
    CODEMIRROR_OPTIONS.connect = null;
    CODEMIRROR_OPTIONS.collapse = false;
    CODEMIRROR_OPTIONS.target = null;
    var assetName, baseVersion, revisionVersion;
    resolveAssetName();
    resolveVersion();
    var textDiffURL = resolveTextDiffURL(paths, domain, type);
    initTextDiff(textDiffURL);
    if ("wsdl" === type) {
        var detailDiffURL = resolveDetailDiffURL(paths, domain, type);
        initDetailDiff(detailDiffURL);
    }
    else {
        renderPartial("list-default", {assetName: assetName}, function (template) {
            $('#sectionList').append(template);
        });
    }

    /**
     * Load the diff view data asynchronously and render the UI
     */
    function initTextDiff(url) {
        $.ajax({
            url: url,
            type: 'GET',
            async: false,
            success: function (response) {
                var textDiffData = JSON.parse(response);
                //Note: We do not need to call the render method here (since this is async), however in the future
                //if we need to load the diff view asynchronously the async property can be set to TRUE.
                renderTextDiff(textDiffData);
            },
            error: function () {
                alert('Failed to load comparison text data');
            }
        });
    }

    function initDetailDiff(url) {
        $.ajax({
            url: url,
            type: 'GET',
            async: false,
            success: function (response) {
                detailDiffData = JSON.parse(response);
                //Note: We do not need to call the render method here (since this is async), however in the future
                //if we need to load the diff view asynchronously the async property can be set to TRUE.
                renderDetailDiff(detailDiffData);
            },
            error: function () {
                alert('Failed to load comparison detail data');
            }
        });
    }

    /**
     * Resolves the tenant domain against which the API call must be made
     */
    function resolveDomain() {
        var tenantDomain;
        var domain = '';
        if ((store) && (store.store)) {
            tenantDomain = store.store.tenantDomain;
        }
        //Construct the tenant query parameter if a tenant domain was resolved
        if (tenantDomain) {
            domain = '&tenant=' + tenantDomain;
        }
        return domain;
    }

    function resolveTextDiffURL(paths, domain, type) {
        return caramel.context + '/apis/governance-artifacts/diff-text?targets=' + paths[1] + ',' + paths[0] + domain
            + '&type=' + type;
    }

    function resolveDetailDiffURL(paths, domain, type) {
        return caramel.context + '/apis/governance-artifacts/diff-detail?targets=' + paths[1] + ',' + paths[0] + domain
            + '&type=' + type;
    }

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

    /**
     * Resolve the asset name that shown in the diff view
     */
    function resolveAssetName() {
        var basePath = paths[1].split("/");
        assetName = basePath[basePath.length - 1];
    }

    /**
     * Resolve the versions of files that shown in the diff view
     */
    function resolveVersion() {
        var basePath = paths[1].split("/");
        var revisionPath = paths[0].split("/");
        baseVersion = basePath[basePath.length - 2];
        revisionVersion = revisionPath[revisionPath.length - 2];
    }

    /**
     * Renders the diff view by processing the response object provided by the API
     * and then initializing the CodeMirror UI
     */
    function renderTextDiff(textDiffData) {
        var textDiffSections = textDiffData.sections;
        if (!jQuery.isEmptyObject(textDiffSections)) {
            var sectionName = Object.keys(textDiffSections)[0];
            if (!jQuery.isEmptyObject(textDiffSections[sectionName].content)) {
                var changeName = Object.keys(textDiffSections[sectionName].content)[0];
                if (!jQuery.isEmptyObject(textDiffData.sections[sectionName].content[changeName])) {
                    var loadContent = textDiffData.sections[sectionName].content[changeName][0];
                }
            }
        }
        CODEMIRROR_OPTIONS.loadContent = loadContent;
        CODEMIRROR_OPTIONS.orig2 = loadContent.content.changed;
        var value = document.documentElement.innerHTML;
        if (value == null) return;
        var target = document.getElementById("diffView");
        target.innerHTML = "";
        CODEMIRROR_OPTIONS.target = target;
        initUI(CODEMIRROR_OPTIONS);
        setViewPanelsHeight();
        addTitle(baseVersion, revisionVersion);
    }

    function renderDetailDiff(detailDiffData) {
        var keys = Object.keys(detailDiffData.sections);
        var sections = [];
        var entry;
        keys.forEach(function (key) {
            entry = {};
            entry.name = key;
            entry.title = getDiffLabel(key);
            entry.data = detailDiffData.sections[key];
            sections.push(entry);
        });
        if (!jQuery.isEmptyObject(sections)) {
            renderPartial("list-section-changes", {assetName: assetName, sections: sections}, function (template) {
                $('#sectionList').append(template);
            });
        }
        else {
            renderPartial("list-default", {assetName: assetName}, function (template) {
                $('#sectionList').append(template);
            });
        }
    }

    /**
     * Adding onclick event listeners to dynamically added elements
     */
    $(document).on("click", ".details-sub-menu", function () {
        var section = $(this).data('section');
        var change = $(this).data('change');
        var value = document.documentElement.innerHTML;
        if (value == null) return;
        var target = document.getElementById("diffView");
        target.innerHTML = "";
        CODEMIRROR_OPTIONS.target = target;
        if (!jQuery.isEmptyObject(detailDiffData.sections[section].content[change])) {
            var loadContent = detailDiffData.sections[section].content[change][0];
            CODEMIRROR_OPTIONS.loadContent = loadContent;
            if ("CONTENT_ADDITION" === change) {
                //CODEMIRROR_OPTIONS.orig2 = loadContent.content;
                CODEMIRROR_OPTIONS.orig2 = null;
                initUIAddition(CODEMIRROR_OPTIONS);
            } else if ("CONTENT_REMOVAL" === change) {
                //CODEMIRROR_OPTIONS.orig2 = "";
                CODEMIRROR_OPTIONS.orig2 = null;
                initUIRemoval(CODEMIRROR_OPTIONS);
            } else {
                CODEMIRROR_OPTIONS.orig2 = loadContent.content.changed;
                initUI(CODEMIRROR_OPTIONS);
            }
            setViewPanelsHeight();
            addTitle(baseVersion, revisionVersion, change);
        }
    });

    $(document).on("click", ".panel-heading", function () {
        var detail_menu_element = $(this).children().children().children().children();
        if (detail_menu_element.hasClass("glyphicon-chevron-down")) {
            detail_menu_element.removeClass("glyphicon-chevron-down");
            detail_menu_element.addClass("glyphicon-chevron-up");
        } else {
            detail_menu_element.removeClass("glyphicon-chevron-up");
            detail_menu_element.addClass("glyphicon-chevron-down");
        }
    });


    $(document).on("click", ".text-diff", function () {
        var textDiffURL = resolveTextDiffURL(paths, domain, type);
        initTextDiff(textDiffURL);
    });

    /**
     * Resolving the specific partial file from the name
     */
    function partial(name) {
        return '/extensions/app/greg-diff/themes/' + caramel.themer + '/partials/' + name + '.hbs';
    }

    /**
     * Rendering templates into the page
     */
    function renderPartial(partialKey, data, fn) {
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
    }

    /**
     * Mapping back end variables into proper labels
     */
    function getDiffLabel(key) {
        var SECTION_LABELS_MAP = {
            "wsdl_declaration": "WSDL Declaration Diff",
            "wsdl_imports": "WSDL Imports Diff",
            "wsdl_bindings": "WSDL Bindings Diff",
            "wsdl_messages": "WSDL Messages Diff",
            "wsdl_porttype": "WSDL PortTypes Diff",
            "wsdl_operations": "WSDL Operations Diff",
            "wsdl_service": "WSDL Service Diff",
            "wsdl_ports": "WSDL Ports Diff",
            "default": "Complete Text Diff"
        };
        return SECTION_LABELS_MAP[key];
    }

    /**
     * Initializing logic for the CodeMirror library
     */
    function initUI(options) {
        options.dv = CodeMirror.MergeView(options.target, {
            value: options.loadContent.content.original,
            origLeft: options.panes == 3 ? options.orig1 : null,
            orig: options.orig2,
            lineNumbers: true,
            mode: "text/xml",
            highlightDifferences: options.highlight,
            connect: options.connect,
            collapseIdentical: options.collapse,
            theme: "ambiance"
        });
    }

    function initUIAddition(options) {
        options.dv = CodeMirror.MergeView(options.target, {
            //value: "",
            value: options.loadContent.content,
            origLeft: options.panes == 3 ? options.orig1 : null,
            orig: options.orig2,
            lineNumbers: true,
            mode: "text/xml",
            highlightDifferences: options.highlight,
            connect: options.connect,
            collapseIdentical: options.collapse,
            theme: "ambiance"
        });
    }

    function initUIRemoval(options) {
        options.dv = CodeMirror.MergeView(options.target, {
            value: options.loadContent.content,
            origLeft: options.panes == 3 ? options.orig1 : null,
            orig: options.orig2,
            lineNumbers: true,
            mode: "text/xml",
            highlightDifferences: options.highlight,
            connect: options.connect,
            collapseIdentical: options.collapse,
            theme: "ambiance"
        });
    }
});
