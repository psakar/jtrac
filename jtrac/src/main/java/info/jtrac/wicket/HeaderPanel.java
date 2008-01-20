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

import info.jtrac.domain.Space;
import info.jtrac.domain.State;
import info.jtrac.domain.User;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.Cookie;
import org.acegisecurity.context.SecurityContextHolder;
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.protocol.http.WebResponse;

/**
 * header navigation
 */
public class HeaderPanel extends BasePanel {    
    
    public HeaderPanel() {
        super("header");
        
        final User user = getPrincipal();
        final Space space = getCurrentSpace();
        final List<Space> spaces = new ArrayList(user.getSpaces());
        
        add(new Link("dashboard") {
            public void onClick() {
                // if only one space, that would remain "selected" across all navigation.
                if(spaces.size() == 1) {
                    ((JtracSession) getSession()).setCurrentSpace(spaces.get(0));
                } else {
                    ((JtracSession) getSession()).setCurrentSpace(null);
                }                
                setResponsePage(DashboardPage.class);
            }            
        });
        
        if (space == null) {
            add(new Label("space", "").setVisible(false));
            add(new Label("new", "").setVisible(false));
            add(new Link("search") {
                public void onClick() {                    
                    // if only one space don't use generic search screen
                    if(spaces.size() == 1) {
                        Space current = spaces.get(0);
                        setCurrentSpace(current);                        
                    } else {
                        setCurrentSpace(null);  // may have come here with back button
                    }
                    setResponsePage(ItemSearchFormPage.class);
                }            
            });            
        } else {
            add(new WebMarkupContainer("space").add(new Label("space", space.getName())));            
            if (user.getPermittedTransitions(space, State.NEW).size() > 0) {            
                add(new Link("new") {
                    public void onClick() {
                        setResponsePage(ItemFormPage.class);
                    }            
                });
            } else {
                add(new WebMarkupContainer("new").setVisible(false));       
            }
            
            add(new Link("search") {
                public void onClick() {
                    setResponsePage(ItemSearchFormPage.class);
                }            
            });            
        }
        
        if(user.getId() == 0) {
            add(new WebMarkupContainer("options").setVisible(false));
            add(new WebMarkupContainer("logout").setVisible(false));
            add(new Link("login") {
                public void onClick() {
                    setResponsePage(LoginPage.class);
                }            
            });
            add(new WebMarkupContainer("user").setVisible(false));
        } else {
            add(new Link("options") {
                public void onClick() {
                    ((JtracSession) getSession()).setCurrentSpace(null); 
                    setResponsePage(OptionsPage.class);
                }            
            }); 
            add(new Link("logout") {
                public void onClick() {                                        
                    Cookie cookie = new Cookie("jtrac", user.getLoginName() 
                        + ":" + getJtrac().encodeClearText(user.getPassword()));
                    ((WebResponse) getRequestCycle().getResponse()).clearCookie(cookie);                    
                    getSession().invalidate();
                    logger.debug("invalidated session and cleared cookie"); 
                    // if CAS is involved, clear security context as well
                    SecurityContextHolder.clearContext();
                    setResponsePage(LogoutPage.class, new PageParameters("locale=" + user.getLocale()));
                }            
            });
            add(new WebMarkupContainer("login").setVisible(false));
            add(new WebMarkupContainer("user").add(new Label("user", user.getName())));
        }             
        
    }
    
}
