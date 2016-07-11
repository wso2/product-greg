/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
var sevices;
$(function () {
    services = JSON.parse(store.viewer.content);
    var partial = function (name) {
        return '/extensions/app/soap-viewer/themes/' + caramel.themer + '/partials/' + name + '.hbs';
    };

    var renderPartial = function (partialKey, containerKey, data, fn) {
        fn = fn || function () {
            };
        var partialName = partialKey;
        var containerName = containerKey;
        if (!partialName) {
            throw 'A template name has not been specified for template key ' + partialKey;
        }
        if (!containerName) {
            throw 'A container name has not been specified for container key ' + containerKey;
        }
        var obj = {};
        obj[partialName] = partial(partialName);
        caramel.partials(obj, function () {
            var template = Handlebars.partials[partialName](data);
            $(containerName).html(template);
            fn(containerName);
        });
    };

    renderPartial('types', '#output', '', function () {
        renderPartial('operations', '#result', services
        );

    });

    Handlebars.registerHelper('escapeC', function (str) {
        return str.replace(/[^a-zA-Z0-9-]/g, '_escaped_');
    });

    $('body').on('change', 'select[data-change=transport-type]', function () {
        $('option:selected', this).each(function () {
            var selectedOption = $('#' + $(this).val());

            selectedOption
                .addClass('in')
                .attr('aria-expanded', 'true');

            selectedOption.siblings()
                .removeClass('in')
                .attr('aria-expanded', 'false');
        });
    });

    $('body').on('click', '[data-toggle=collapse-all]', function (e) {
        e.preventDefault();
        $(this).addClass('active');
        $(this).siblings().removeClass('active');
        $(this)
            .closest('.collapsible-controls')
            .siblings('.services-ui')
            .find('.operation[aria-expanded="true"]')
            .collapse('hide');
    });

    $('body').on('click', '[data-toggle=expand-all]', function (e) {
        e.preventDefault();
        $(this).addClass('active');
        $(this).siblings().removeClass('active');
        $(this)
            .closest('.collapsible-controls')
            .siblings('.services-ui')
            .find('.operation[aria-expanded="false"]')
            .collapse('show');
    });
});

