/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.carbon.registry.search.metadata.test.utils;

import org.testng.annotations.DataProvider;

public class Parameters {
    @DataProvider(name = "invalidCharacter")
    public static Object[][] invalidCharacter() {
        return new Object[][]{
                {"<"},
                {">"},
                {"#"},
                {"@"},
                {"|"},
                {"^"},
                {"\\"},
                {","},
                {"\""},
                {"~"},
                {"!"},
                {"*"},
                {"{"},
                {"}"},
                {";"},
                {"+"},
                {"\'"},
                {"="}
        };
    }
    @DataProvider(name = "invalidCharacterForContent")
    public static Object[][] invalidCharacterForContent() {
        return new Object[][]{
                {"<"},
                {">"},
                {"#"},
                {"@"},
                {"|"},
                {"^"},
                {"\\"},
                {","},
                {"\""},
                {"~"},
                {"!"},
                {"*"},
                {"{"},
                {"}"},
                {"%"},
                {";"},
                {"+"},
                {"\'"},
                {"["},
                {"]"},
                {"("},
                {")"}
        };
    }

    @DataProvider(name = "invalidCharacterForTags")
    public static Object[][] invalidCharacterForTags() {
        return new Object[][]{
                {"~"},
                {"!"},
                {"@"},
                {"#"},
                {";"},
                {"%"},
                {"^"},
                {"*"},
                {"+"},
                {"="},
                {"{"},
                {"}"},
                {"\\|"},
                {"\\\\"},
                {"<"},
                {">"},
                {"\""},
                {"'"},
                // the separator on its own is an invalid character.
                {","},
        };
    }

    @DataProvider(name = "invalidCharacterForTags2")
    public static Object[][] invalidCharacterForTags2() {
        return new Object[][]{
                {" "},
                {""},
        };
    }

    @DataProvider(name = "invalidCharacterForMediaType")
    public static Object[][] invalidCharacterForMediaType() {
        return new Object[][]{
                {"<"},
                {">"},
                {"#"},
                {"@"},
                {"|"},
                {"^"},
                {"\\"},
                {","},
                {"\""},
                {"~"},
                {"!"},
                {"*"},
                {"{"},
                {"}"},
                {";"},
                {"\'"},
                {"="}
        };
    }
}
