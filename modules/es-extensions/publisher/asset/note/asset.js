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
asset.manager = function (ctx) {
    return {
        create: function (options) {
            if(options.attributes.overview_hash == null){
                var uuid = require('uuid');
                options.attributes.overview_hash = uuid.generate();
            }
            var d = new Date();
            options.attributes.overview_timecreated = d.getHours() + ":" + d.getMinutes();
            options.attributes.overview_date = d.getDate() + "-" + d.getMonth() + "-" + d.getFullYear();
            options.attributes.overview_user = ctx.username;
            try {
                this._super.create.call(this, options);
            } catch (e) {
                throw e;
            }
        },
        update: function (options) {
            var d = new Date();
            options.attributes.overview_timecreated = d.getHours() + ":" + d.getMinutes();
            options.attributes.overview_date = d.getDate() + "-" + d.getMonth() + "-" + d.getFullYear();
            options.attributes.overview_user = ctx.username;
            try {
                this._super.update.call(this, options);
            } catch (e) {
                throw e;
            }
        },
        postCreate:function(){

        }
    };
};
asset.configure = function () {
    return {
        meta: {
            lifecycle: {
                defaultLifecycleEnabled: false
            },
            versionAttribute: '',
            ui: {
                icon: 'fw fw-wsdl'
            }
        }
    }
};
