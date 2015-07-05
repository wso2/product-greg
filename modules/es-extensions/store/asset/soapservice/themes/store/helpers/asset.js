/*
 * Copyright (c) 2005-2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */
var name;
var custom = require('/extensions/app/greg-store-defaults/themes/store/helpers/asset.js');
var that = this;
/*
In order to inherit all variables in the default helper
*/
for (name in custom) {
    if (custom.hasOwnProperty(name)) {
        that[name] = custom[name];
    }
}
var fn = that.resources;
var resources = function(page, meta) {
    var o = fn(page, meta);
    if (!o.css) {
        o.css = [];
    }
    var log = new Log();
    log.debug('Overidden by soap service!!!');
    //code-mirror third party library to support syntax highlighting & formatting for WSDL content.
    o.css.push('codemirror.css');
    o.js.push('codemirror.js');
    o.js.push('xml.js'); //codemirror file to provide 'xml' type formatting.
    o.js.push('asset-view.js');//renders the wsdl content with codemirror supported formatting.
    return o;
};