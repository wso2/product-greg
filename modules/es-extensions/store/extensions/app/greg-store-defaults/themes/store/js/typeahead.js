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
$(document).ready(function () {
	var substringMatcher = function (strs) {
		return function findMatches(q, cb) {
			var matches, substringRegex;

			// an array that will be populated with substring matches
			matches = [];

			// regex used to determine if a string contains the substring `q`
			substrRegex = new RegExp(q, 'i');

			// iterate through the pool of strings and for any string that
			// contains the substring `q`, add it to the `matches` array
			$.each(strs, function (i, str) {
				if (substrRegex.test(str)) {
					matches.push(str);
				}
			});

			cb(matches);
		};
	};
	var history = [];

	if ((store) && (store.store)) {
		var stringHistory = store.store.searchHistory || "";
		history = stringHistory.split(',');
		$('#the-basics .typeahead').typeahead({
				hint: true,
				highlight: true,
				minLength: 1
			},
			{
				name: 'history',
				display: function (o) {
					return $((document.createElement('div'))).html(o).text();
				},
				source: substringMatcher(history),
				templates: {
					header: function (data) {
						return '<div class="text-mute" style="padding-left:20px;border-bottom:1px solid #eee;margin-bottom:5px;padding-top:5px;">Search History</div>';
					},
					suggestion: function (data) {
						return '<p>' + data + '</p>';
					}
				}
			});
	}
});
