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
    var url = resolveURL(paths, domain);
    init(url);

    /**
     * Load the diff view data asynchoronously and render the UI
     */
    function init(url) {
        $.ajax({
            url: url,
            type: 'GET',
            async: false,
            success: function(response) {
                var diffData = JSON.parse(response);
                //Note: We do not need to call the render method here (since this is async), however in the future 
                //if we need to load the diff view asynchronously the async property can be set to TRUE.
                render(diffData); 
            },
            error: function() {
                alert('Failed to load comparison data');
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

    function resolveURL(paths, domain) {
        return caramel.context + '/apis/governance-artifacts/diff-text?targets=' + paths[1] + ',' + paths[0] + domain;
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
    function render(diffData) {
        var opts = {};
        var init_section_name;
        var init_change_name;
        var initialLoadContent;
        opts.value = null;
        opts.orig1 = null;
        opts.orig2 = null;
        opts.dv = null;
        opts.panes = 2;
        opts.highlight = true;
        opts.connect = null;
        opts.collapse = false;
        var sections = diffData.sections;
        //Process the diff object sent by the API
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
        opts.initialLoadContent = initialLoadContent;
        opts.value = document.documentElement.innerHTML;
        opts.orig2 = initialLoadContent.content.changed;
        initUI(opts);
        setViewPanelsHeight();
        addTitle();
    }
    /**
     * Initializing logic for the CodeMirror librray
     */
    function initUI(options) {
        if (options.value == null) return;
        var target = document.getElementById("diffView");
        target.innerHTML = "";
        dv = CodeMirror.MergeView(target, {
            value: options.initialLoadContent.content.original,
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