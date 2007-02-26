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
import info.jtrac.domain.User;
import info.jtrac.domain.UserSpaceRole;
import java.util.ArrayList;
import java.util.List;
import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import wicket.behavior.SimpleAttributeModifier;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.IChoiceRenderer;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.BoundCompoundPropertyModel;
import wicket.model.PropertyModel;

/**
 * user allocate page
 */
public class UserAllocatePage extends BasePage {
      
    private WebPage previous;
    private User user;        
    
    public UserAllocatePage(User u, WebPage previous) {
        this.user = getJtrac().loadUser(u.getId());
        this.previous = previous;        
        add(new UserAllocateForm("form"));
    }
    
    private class UserAllocateForm extends Form {                
        
        public UserAllocateForm(String id) {
            
            super(id);                            
            
            UserSpaceRole usr = new UserSpaceRole();            
            final BoundCompoundPropertyModel model = new BoundCompoundPropertyModel(usr);
            setModel(model);
            
            add(new FeedbackPanel("feedback"));
            
            add(new Label("label", user.getName() + " (" + user.getLoginName() + ")"));
            
            List<UserSpaceRole> usrs = new ArrayList(user.getUserSpaceRoles());
            
            final SimpleAttributeModifier sam = new SimpleAttributeModifier("class", "alt");
            
            add(new ListView("usrs", usrs) {
                protected void populateItem(ListItem listItem) {
                    if(listItem.getIndex() % 2 == 1) {
                        listItem.add(sam);
                    }                    
                    final UserSpaceRole usr = (UserSpaceRole) listItem.getModelObject();
                    listItem.add(new Label("space.name", new PropertyModel(usr, "space.name")));                    
                    listItem.add(new Label("roleKey", new PropertyModel(usr, "roleKey")));
                    Button deallocate = new Button("deallocate") {
                        @Override
                        public void onSubmit() {
                            getJtrac().removeUserSpaceRole(usr);
                            refreshPrincipal(usr.getUser());
                            setResponsePage(new UserAllocatePage(user, previous));
                        }                   
                    };
                    if(usr.getUser().getId() == 1 && "ROLE_ADMIN".equals(usr.getRoleKey())) {
                        deallocate.setEnabled(false);
                    }
                    listItem.add(deallocate);
                }
            });                       
            
            List<Space> spaces = getJtrac().findUnallocatedSpacesForUser(user.getId());
            
            DropDownChoice spaceChoice = new DropDownChoice("space", spaces, new IChoiceRenderer() {
                public Object getDisplayValue(Object o) {
                    return ((Space) o).getName();
                }
                public String getIdValue(Object o, int i) {
                    return ((Space) o).getId() + "";
                }
            });            
            spaceChoice.setNullValid(true);
            
            final DropDownChoice roleKeyChoice = new DropDownChoice("roleKey");            
            roleKeyChoice.setOutputMarkupId(true);
            roleKeyChoice.setEnabled(false);
            roleKeyChoice.setRequired(true);
            roleKeyChoice.setNullValid(true);
            roleKeyChoice.add(new ErrorHighlighter());
            add(roleKeyChoice);
            
            final Button allocateButton = new Button("allocate") {
                @Override
                public void onSubmit() {     
                    UserSpaceRole usr = (UserSpaceRole) UserAllocateForm.this.getModelObject();
                    if(usr.getSpace() == null || usr.getRoleKey() == null) {
                        return;
                    }
                    getJtrac().storeUserSpaceRole(user, usr.getSpace(), usr.getRoleKey());
                    refreshPrincipal(user);
                    setResponsePage(new UserAllocatePage(user, previous));
                }                   
            };
            allocateButton.setOutputMarkupId(true);
            allocateButton.setEnabled(false);
            add(allocateButton);           
            
            spaceChoice.add(new AjaxFormComponentUpdatingBehavior("onChange") {
                protected void onUpdate(AjaxRequestTarget target) {
                    Space space = (Space) getFormComponent().getConvertedInput();
                    if (space == null) {
                        roleKeyChoice.setEnabled(false);
                        allocateButton.setEnabled(false);
                    } else {
                        Space temp = getJtrac().loadSpace(space.getId());
                        List<String> roleKeys = new ArrayList(temp.getMetadata().getRoles().keySet());
                        roleKeyChoice.setChoices(roleKeys);                    
                        roleKeyChoice.setEnabled(true);
                        allocateButton.setEnabled(true);
                    }
                    target.addComponent(roleKeyChoice);
                    target.addComponent(allocateButton);
                }
            });            
            
            add(spaceChoice);                                
            
            WebMarkupContainer makeAdmin = new WebMarkupContainer("makeAdmin");
            if(user.isAdminForAllSpaces()) {
                makeAdmin.setVisible(false);
            } else {
                makeAdmin.add(new Button("makeAdmin") {
                    @Override
                    public void onSubmit() {     
                        getJtrac().storeUserSpaceRole(user, null, "ROLE_ADMIN");
                        refreshPrincipal(user);
                        setResponsePage(new UserAllocatePage(user, previous));
                    }                   
                });
            }
            add(makeAdmin);
            
            // cancel ==========================================================
            add(new Link("cancel") {
                public void onClick() {
                    setResponsePage(previous);
                }                
            });            
        }              
                        
    }        
        

    
}