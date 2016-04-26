/*
 * Copyright (c) 2014, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function isNotNullOrEmpty(checkText){

    if(checkText == "" || checkText === 'null' || checkText == null || checkText.length <= 0){
        return false;
    }
    return true;
}

function inputNotNullOrEmpty(jsonObj, paramNameArray){

    var input;
    for (var i = 0; i < paramNameArray.length; i++) {
        input = jsonObj[paramNameArray[i]];
        if(input == "" || input === 'null' || input == null || input.length <= 0){
            return false;
        }
    }
    return true;
}

function getImageByMediaType(mediaType) {

    //var config = application.get("mediaTypesToImages");
    //var img = config[mediaType];

    //if (isNotNullOrEmpty(img)) {
    //    return img;
    //}

    return "document.png";
}
