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

package org.wso2.carbon.registry.sample.test.utils;

import java.io.File;
import java.io.FileFilter;

public class RegExFilter implements FileFilter {

    private final TypeFilter type;
    private final String pattern;

    public RegExFilter( final TypeFilter type, final String pattern ) {
        super();
        this.type = type;
        this.pattern = pattern;
    }


    @Override
    public boolean accept( final File file ) {
        return this.type.accept( file ) && file.getName().matches( this.pattern );
    }
}
