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
    var hasFlash = false;
    try {
        hasFlash = Boolean(new ActiveXObject('ShockwaveFlash.ShockwaveFlash'));
    } catch (exception) {
        hasFlash = ('undefined' != typeof navigator.mimeTypes['application/x-shockwave-flash']);
    }

// If Flash is disabled use clipboard.js. This will not support in Safari
    if (!hasFlash) {
        var clipboard = new Clipboard('.copy-button');
    } else {
        var client = new ZeroClipboard(document.getElementById("copy-button"));
        client.on("ready", function (readyEvent) {
            this.on("aftercopy", function (event) {
                //Provide feedback to the user indicating that the
                $('#copy-button').html('<i class="fw fw-check" style="color:green"></i> Copied to clipboard');
                $('#copy-button').removeClass('btn-default');
                $('#copy-button').addClass('btn-warning');
                setTimeout(function () {
                    $('#copy-button').html('<i class="fw fw-copy"></i> Copy URL');
                    $('#copy-button').removeClass('btn-warning');
                    $('#copy-button').addClass('btn-default');
                }, 5000);
            });
        });
    }
});

$("#copy-button").click(function () {
    var url = $(this).data('clipboardText');
    var hasFlash = false;
    try {
        hasFlash = Boolean(new ActiveXObject('ShockwaveFlash.ShockwaveFlash'));
    } catch (exception) {
        hasFlash = ('undefined' != typeof navigator.mimeTypes['application/x-shockwave-flash']);
    }

    // At least Safari 3+: "[object HTMLElementConstructor]"
    var isSafari = Object.prototype.toString.call(window.HTMLElement).indexOf('Constructor') > 0;
    if (isSafari == true && hasFlash == false) {
        $('#copy-button').html('<i class="fw fw-check" style="color:green"></i> Please Ctr+v!');
    } else {
        $('#copy-button').html('<i class="fw fw-check" style="color:green"></i> Copied to clipboard');
    }
    var popoverInput = "<input type='text' class='form-control' onClick='this.setSelectionRange(0, this.value.length)' value='" +
        url + "' aria-describedby='basic-addon2' readonly>"

    // Provide feedback to the user indicating that the
    $('#copy-button').removeClass('btn-default');
    $('#copy-button').addClass('btn-warning');
    setTimeout(function () {
        $('#copy-button').html('<i class="fw fw-copy"></i> Copy URL');
        $('#copy-button').removeClass('btn-warning');
        $('#copy-button').addClass('btn-default');
    }, 5000);
    if (isSafari == true && hasFlash == false) {
        $('#copy-button').popover({animation: true, content: popoverInput, html: true, placement: 'bottom'});
    }
});