/*
 *  Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

var name;
var hps = require('/themes/store/helpers/header.js');
var that = this;
/*
 In order to inherit all variables in the default helper
 */
for (name in hps) {
    if (hps.hasOwnProperty(name)) {
        that[name] = hps[name];
    }
}
var fn = that.resources||function() { return {} };
var resources = function(page, meta) {
    var o = fn(page, meta);
    if (!o.css) {
        o.css = [];
    }
    if(!o.js){
        o.js = [];
    }
    if(!o.code){
        o.code = [];
    }
    o.css.push('common.css');
    o.css.push('sidepanel.css');
    o.css.push('left-navigation.css');
    o.css.push('font-wso2.css');
    o.css.push('bootstrap-dialog.min.css');
    o.css.push('typeahead.css');
    o.js.push('sidepanel.js');
    o.js.push('greg-subscriptions-api.js');
    o.js.push('bootstrap-dialog.min.js');
    o.js.push('typeahead.bundle.min.js');
    // remove this line, because this file is loaded twice in store side. That occurs search-box css issue
    //o.js.push('typeahead.js');
    o.code.push('taxonomy-meta-data.hbs');
    o.code.push('tenant-meta-data.hbs');
    return o;
};