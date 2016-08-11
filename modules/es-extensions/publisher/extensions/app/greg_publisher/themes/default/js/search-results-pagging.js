/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
$(function () {
    var SEARCH_API = '/apis/assets?q=';
    var rows_added = 0;
    var last_to = 0;
    var items_per_row = 0;
    var doPagination = true;
    store.infiniteScroll = {};
    store.infiniteScroll.recalculateRowsAdded = function () {
        return (last_to - last_to % items_per_row) / items_per_row;
    };
    store.infiniteScroll.addItemsToPage = function (query) {
        var screen_width = $(window).width();
        var screen_height = $(window).height();


        var header_height = 400;
        var thumb_width = 170;
        var thumb_height = 280;
        var gutter_width = 20;

        screen_width = screen_width - gutter_width; // reduce the padding from the screen size
        screen_height = screen_height - header_height;

        items_per_row = (screen_width - screen_width % thumb_width) / thumb_width;
        // var rows_per_page = (screen_height-screen_height%thumb_height)/thumb_height;
        var scroll_pos = $(document).scrollTop();
        var row_current = (screen_height + scroll_pos - (screen_height + scroll_pos) % thumb_height) / thumb_height;
        // We increase the row current by 2 since we need to provide
        // one additional row to scroll down without loading it from backend
        row_current += 3;


        var from = 0;
        var to = 0;
        if (row_current > rows_added && doPagination) {
            from = rows_added * items_per_row;
            to = row_current * items_per_row;
            last_to = to; // We store this os we can recalculate rows_added when resolution change
            rows_added = row_current;
            store.infiniteScroll.getItems(from, to, query);
            //console.info('getting items from ' + from + " to " + to + " screen_width " + screen_width + " items_per_row " + items_per_row);
        }

    };
    store.infiniteScroll.getItems = function (from, to, query) {
        var count = to - from;
        var dynamicData = {};
        dynamicData["from"] = from;
        dynamicData["to"] = to;
        var path = window.location.href; // Current page path
        // Returns the jQuery ajax method
        var url = caramel.tenantedUrl(SEARCH_API + query + "&paginationLimit=" + to + "&start=" + from + "&count=" + count);

        caramel.render('loading', 'Loading assets from ' + from + ' to ' + to + '.', function (info, content) {
            $('.loading-animation-big').remove();
            $('body').append($(content));
        });

        $.ajax({
            url: url,
            method: 'GET',
            success: function (data) {
                var results = [];
                if (data) {
                    results = data.list || [];
                }
                for (var i = 0; i < results.length; i++) {
                    results[i].showType = true;
                }
                if (results.length == 0) {
                    if (from == 0) {
                        $('#search-results').html('<div class="emptyAssets-MsgDiv"><p class="emptyAssets">We are sorry! we couldn\'t find anything for you ...</p></div>');
                    }
                    $('.loading-animation-big').remove();
                    doPagination = false;
                } else {
                    loadPartials('list-assets', function (partials) {
                        caramel.partials(partials, function () {
                            caramel.render('list_assets_table_body', results, function (info, content) {
                                $('#search-results').append($(content));
                                $('.loading-animation-big').remove();
                            });
                        });
                    });
                }
            }, error: function () {
                doPagination = false;
                $('.loading-animation-big').remove();
            }
        });
    };
    store.infiniteScroll.showAll = function (query) {
        store.infiniteScroll.addItemsToPage(query);
        $(window).scroll(function () {
            store.infiniteScroll.addItemsToPage(query);
        });
        $(window).resize(function () {
            // Recalculate "rows_added"
            rows_added = store.infiniteScroll.recalculateRowsAdded();
            store.infiniteScroll.addItemsToPage(query);
        });
    };

    var isEmptyQuery = function (query) {
        query = query.trim();
        return (query.length <= 0);
    };
    var loadPartials = function (partial, done) {
        $.ajax({
            url: caramel.url('/apis/partials') + '?partial=' + partial,
            success: function (data) {
                done(data);
            },
            error: function () {
                done(err);
            }
        });
    };

    /**
     * Replace all the occurrences of $regex by $replace in $originalString
     * @param  {originalString} input - Raw string.
     * @param  {regex} input - Target key word or regex that need to be replaced.
     * @param  {replace} input - Replacement key word
     * @return {String}       Output string
     */
    var replaceAll = function(originalString, regex, replace) {
        return originalString.replace(new RegExp(regex, 'g'), replace);
    };


    /**
     * Split the query params by space and quotation mark
     * @param  {q} input - search value input from user.
     * @return {String[]}       Output string array
     */
    var splitQuery = function (q) {
        var comps;
        if (q.indexOf('"') > -1) {
            var queryWithoutQuots = q;
            // Searching is only allowed with quots for tags and content.
            var queryWithQuots = q.match(/(tags|content|name):"(.*?)"/g);

            for (var i = 0; queryWithQuots != null && i < queryWithQuots.length; i++) {
                queryWithoutQuots = queryWithoutQuots.replace(queryWithQuots[i], '').trim();
                queryWithQuots[i] = replaceAll(queryWithQuots[i], '"', '\\"');
            }

            var queryNameWithQuots = queryWithoutQuots.match(/"(.*?)"/g);

            for (var i = 0; queryNameWithQuots != null && i < queryNameWithQuots.length; i++) {
                queryWithoutQuots = queryWithoutQuots.replace(queryNameWithQuots[i], '').trim();
                queryNameWithQuots[i] = replaceAll(queryNameWithQuots[i], '"', '\\"');
            }

            // merging queryWithQuots and queryNameWithQuots for ease of use.
            if (queryWithQuots != null && queryNameWithQuots != null) {
                queryWithQuots.concat(queryNameWithQuots);
            } else if (queryNameWithQuots != null) {
                queryWithQuots = queryNameWithQuots;
            }

            // 3 or more pairs. Ex: (tags:"customer service" name:buy content:she)
            if (queryWithoutQuots.indexOf(' ') > -1) {
                var queryWithoutQuotsArr = queryWithoutQuots.split(' ');
                var comps = queryWithQuots.concat(queryWithoutQuotsArr);
            }
            // 2 or less pairs. Ex: (tags:"customer service" content:she)
            else {
                // if there is no queryWithoutQuots
                if (queryWithoutQuots == "") {
                    comps = queryWithQuots;
                } else {
                    comps = queryWithQuots.concat(queryWithoutQuots);
                }

            }
        } else if (q.indexOf(' AND ') > -1 || q.indexOf(' OR ') > -1) {
            // Note: if user wants to do a boolean search within multiple attributes this is the place he has to change.
            comps = (q + ',').split(','); // Since JSON.parse("[" + q + "]") is not working
            comps.splice(comps.length - 1, 1); // Removing empty array element
        } else {
            comps = q.split(' ');
        }

        return comps;
    };


    var modifiedQuery = function (q) {
        var comps = splitQuery(q);

        return comps.map(function (key) {
            var keyPair = key.split(':');
            if (keyPair.length === 1) {
                return '"name":"' + encodeURIComponent(keyPair[0]) + '"';
            } else {
                return '"' + keyPair[0] + '":"' + encodeURIComponent(keyPair[1]) + '"';
            }
        }).join(',');
    };

    $(document).ready(function (e) {
        doPagination = true;
        rows_added = 0;
        $('#search-results').html('');
        // Note: fix for page scrolling issue.
        // var query = store.publisher.query;
        var query = $('#inp_searchAsset').val();
        query = modifiedQuery(query);
        if (isEmptyQuery(query)) {
            //console.log('User has not entered anything');
            return;
        }
        store.infiniteScroll.showAll(query);
    });
});