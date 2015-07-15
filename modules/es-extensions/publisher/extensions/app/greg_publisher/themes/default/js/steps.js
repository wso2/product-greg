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

/*
 * Setting-up global variables.
 */
var btnNext = '[data-click-event=next]',
    btnPrevious = '[data-click-event=prev]';

/* button next function */
$('[data-click-event=steps] ' + btnNext).click(function(){
    $(this).closest('[data-step]').hide().next('[data-step]').show();
});

/* button previous function */
$('[data-click-event=steps] ' + btnPrevious).click(function(){
    $(this).closest('[data-step]').hide().prev('[data-step]').show();
});