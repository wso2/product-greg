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
$(document).ready(function() {
    var paths = getUrlParameter('path').split(',');
    var type = getUrlParameter('type');
    var domain = resolveDomain();
    var detailDiffData = {};
    var opts = {};
    opts.loadContent = null;
    opts.orig1 = null;
    opts.orig2 = null;
    opts.dv = null;
    opts.panes = 2;
    opts.highlight = true;
    opts.connect = null;
    opts.collapse = false;
    opts.target = null;

    var textDiffURL = resolveTextDiffURL(paths, domain, type);
    initTextDiff(textDiffURL);
    if ("wsdl" == type) {
        var detailDiffURL = resolveDetailDiffURL(paths, domain, type);
        initDetailDiff(detailDiffURL);
    }
    else {
        renderPartial("list-default", {}, function(template){
            $('#sectionList').append(template);
        });
    }

    /**
     * Load the diff view data asynchoronously and render the UI
     */
    function initTextDiff(url) {
        $.ajax({
            url: url,
            type: 'GET',
            async: false,
            success: function(response) {
                var textDiffData = JSON.parse(response);
                //Note: We do not need to call the render method here (since this is async), however in the future
                //if we need to load the diff view asynchronously the async property can be set to TRUE.
                renderTextDiff(textDiffData);
            },
            error: function() {
                alert('Failed to load comparison text data');
            }
        });
    }

    function initDetailDiff(url) {
        $.ajax({
            url: url,
            type: 'GET',
            async: false,
            success: function(response) {
                detailDiffData = JSON.parse(response);
                //Note: We do not need to call the render method here (since this is async), however in the future
                //if we need to load the diff view asynchronously the async property can be set to TRUE.
                renderDetailDiff(detailDiffData);
            },
            error: function() {
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
        if ((store) && (store.publisher)) {
            tenantDomain = store.publisher.tenantDomain;
        }
        //Construct the tenant query parameter if a tenant domain was resolved
        if (tenantDomain) {
            domain = '&tenant=' + tenantDomain;
        }
        return domain;
    }

    function resolveTextDiffURL(paths, domain, type) {
        return caramel.context + '/apis/governance-artifacts/diff-text?targets=' + paths[1] + ',' + paths[0] + domain + '&type=' + type;
    }

    function resolveDetailDiffURL(paths, domain, type) {
        return caramel.context + '/apis/governance-artifacts/diff-detail?targets=' + paths[1] + ',' + paths[0] + domain + '&type=' + type;
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
     * Renders the diff view by processing the response provided by the API
     * and then initializing the CodeMirror UI
     */
    function renderTextDiff(textDiffData) {
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
                    var sectionChange = textDiffData.sections[sectionName].sectionSummary[changeName][0];
                    var loadContent = textDiffData.sections[sectionName].content[changeName][0];
                }
            }
        }
        opts.loadContent = loadContent;
        opts.orig2 = loadContent.content.changed;
        var value = document.documentElement.innerHTML;
        if (value == null) return;
        var target = document.getElementById("diffView");
        target.innerHTML = "";
        opts.target = target;
        initUI(opts);
        setViewPanelsHeight();
        addTitle(sectionChange);
    }

    function renderDetailDiff(detailDiffData) {
        var sectionMap = {
            "wsdl_declaration": "WSDL Declaration",
            "wsdl_imports": "WSDL Imports",
            "wsdl_bindings": "WSDL Bindings",
            "wsdl_messages": "WSDL Messages",
            "wsdl_porttype": "WSDL PortTypes",
            "wsdl_operations": "WSDL Operations",
            "wsdl_service": "WSDL Service",
            "wsdl_ports": "WSDL Ports",
            "default":"Complete Text Diff"
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
        }
        else {
            renderPartial("list-default", {}, function(template){
                $('#sectionList').append(template);
            });
        }
    }

    $(document).on("click", ".list-group-item", function(){
        var section = $(this).data('section');
        var change = $(this).data('change');
        var value = document.documentElement.innerHTML;
        if (value == null) return;
        var target = document.getElementById("diffView");
        target.innerHTML = "";
        opts.target = target;
        if (!jQuery.isEmptyObject(detailDiffData.sections[section].content[change])) {
            var sectionChange = detailDiffData.sections[section].sectionSummary[change][0];
            var loadContent = detailDiffData.sections[section].content[change][0];
            opts.loadContent = loadContent;
            if ("CONTENT_ADDITION" == change) {
                //opts.orig2 = loadContent.content;
                opts.orig2 = null;
                initUIAddition(opts);
            } else if ("CONTENT_REMOVAL" == change) {
                //opts.orig2 = "";
                opts.orig2 = null;
                initUIRemoval(opts);
            } else {
                opts.orig2 = loadContent.content.changed;
                initUI(opts);
            }
            setViewPanelsHeight();
            addTitle(sectionChange);
        }
    });

    $(document).on("click", ".text-diff", function(){
        var textDiffURL = resolveTextDiffURL(paths, domain, type);
        initTextDiff(textDiffURL);
    });

    function partial(name) {
        return '/extensions/app/greg-diff/themes/' + caramel.themer + '/partials/' + name + '.hbs';
    }

    function renderPartial(partialKey,data, fn) {
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
    }

    /**
     * Initializing logic for the CodeMirror librray
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
            theme: "base16-light"
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
            theme: "base16-light"
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
            theme: "base16-light"
        });
    }
});
