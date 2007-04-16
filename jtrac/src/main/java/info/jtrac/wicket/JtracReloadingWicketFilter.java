/*
 * Copyright 2002-2005 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.jtrac.wicket;

import org.apache.wicket.application.ReloadingClassLoader;
import org.apache.wicket.protocol.http.ReloadingWicketFilter;

/**
 * only used in development mode, hot deploy modified code
 * as far as possible without having to restart webapp
 */
public class JtracReloadingWicketFilter extends ReloadingWicketFilter {
    
    static {
        ReloadingClassLoader.includePattern("info.jtrac.wicket.*");
        ReloadingClassLoader.excludePattern("info.jtrac.wicket.JtracApplication");
        ReloadingClassLoader.excludePattern("info.jtrac.wicket.JtracSession");
        // ReloadingClassLoader.excludePattern("org.springframework.*");
    }
    
}