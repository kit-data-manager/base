/**
 * Copyright (C) 2014 Karlsruhe Institute of Technology
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package edu.kit.dama.mdm.base;

import edu.kit.dama.authorization.entities.GroupId;
import edu.kit.dama.authorization.entities.Role;
import edu.kit.dama.authorization.entities.UserId;
import edu.kit.dama.authorization.entities.impl.AuthorizationContext;
import edu.kit.dama.authorization.exceptions.EntityNotFoundException;
import edu.kit.dama.authorization.exceptions.UnauthorizedAccessAttemptException;
import edu.kit.dama.authorization.services.administration.GroupServiceLocal;
import edu.kit.dama.mdm.base.interfaces.IDefaultUserData;
import edu.kit.dama.mdm.core.tools.DateTester;
import edu.kit.dama.util.Constants;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.NamedAttributeNode;
import javax.persistence.NamedEntityGraph;
import javax.persistence.NamedEntityGraphs;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.eclipse.persistence.oxm.annotations.XmlNamedAttributeNode;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraph;
import org.eclipse.persistence.oxm.annotations.XmlNamedObjectGraphs;
import org.eclipse.persistence.queries.FetchGroupTracker;
import org.eclipse.persistence.sessions.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * All information about the user and it's certificate.
 *
 * @author hartmann-v
 */
@Entity
@XmlNamedObjectGraphs({
    @XmlNamedObjectGraph(
            name = "simple",
            attributeNodes = {
                @XmlNamedAttributeNode("userId"),
                @XmlNamedAttributeNode("distinguishedName")
            }),
    @XmlNamedObjectGraph(
            name = "default",
            attributeNodes = {
                @XmlNamedAttributeNode("userId"),
                @XmlNamedAttributeNode("firstName"),
                @XmlNamedAttributeNode("lastName"),
                @XmlNamedAttributeNode("email"),
                @XmlNamedAttributeNode("distinguishedName"),
                @XmlNamedAttributeNode("validUntil"),
                @XmlNamedAttributeNode("validFrom")
            })})
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement
@NamedEntityGraphs({
    @NamedEntityGraph(
            name = "UserData.simple",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("userId")}),
    @NamedEntityGraph(
            name = "UserData.default",
            includeAllAttributes = false,
            attributeNodes = {
                @NamedAttributeNode("userId"),
                @NamedAttributeNode("firstName"),
                @NamedAttributeNode("lastName"),
                @NamedAttributeNode("email"),
                @NamedAttributeNode("distinguishedName"),
                @NamedAttributeNode("validUntil"),
                @NamedAttributeNode("validFrom")
            })
})
public class UserData implements Serializable, IDefaultUserData, FetchGroupTracker {

    private final static Logger LOGGER = LoggerFactory.getLogger(UserData.class);

    /**
     * UID should be the date of the last change in the format yyyyMMdd.
     */
    private static final long serialVersionUID = 20111201L;
    public static final UserData WORLD_USER = factoryWorldUser();
    // <editor-fold defaultstate="collapsed" desc="declaration of variables">
    /**
     * Identification number of the user. primary key of the data set.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;
    /**
     * First name of the user.
     */
    private String firstName;
    /**
     * Last name of the user.
     */
    private String lastName;
    /**
     * E-mail address of the user.
     */
    private String email;
    /**
     * Distinguished name of the user. This distinguished name must be unique in
     * the user administration domain used by the underlaying system. E.g. an
     * LDAP server, a Web portal or a Shibboleth federation.
     */
    private String distinguishedName;
    /**
     * Date when user can login last time. In most cases it should be in the far
     * future. (e.g.: 12/31/2099)
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date validUntil;
    /**
     * Date when user may login first time. In most cases it's the generation
     * date.
     */
    @Temporal(TemporalType.TIMESTAMP)
    private Date validFrom;
    /**
     * The full user name (LastName, FirstName) used for presentation withing
     * UIs.
     */
    @Transient
    @XmlTransient
    private String fullname = null;

    /**
     * The id of the group the user is currently working in. By default, USERS
     * is the current group.
     */
    @Transient
    @XmlTransient
    private GroupId currentGroup = new GroupId(Constants.USERS_GROUP_ID);

    /**
     * The role of the user in the current group.
     */
    @Transient
    @XmlTransient
    private Role currentRole = null;

    // </editor-fold>
    // <editor-fold defaultstate="collapsed" desc="setters and getters">
    @Override
    public Long getUserId() {
        return userId;
    }

    /**
     * Set unique user id. (This should be done automatically by the database.)
     *
     * @param userId the userId to set
     */
    public void setUserId(final Long userId) {
        this.userId = userId;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    /**
     * Set first name.
     *
     * @param firstName the firstName to set
     */
    public void setFirstName(final String firstName) {
        this.firstName = firstName;
        fullname = null;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    /**
     * Set last name.
     *
     * @param lastName the lastName to set
     */
    public void setLastName(final String lastName) {
        this.lastName = lastName;
        fullname = null;
    }

    @Override
    public String getDistinguishedName() {
        return distinguishedName;
    }

    /**
     * Set distinguished name.
     *
     * @param distinguishedName the distributedName to set
     */
    public void setDistinguishedName(final String distinguishedName) {
        this.distinguishedName = distinguishedName;
    }

    @Override
    public String getEmail() {
        return email;
    }

    /**
     * Set email.
     *
     * @param email the email to set.
     */
    public void setEmail(final String email) {
        // There could be an easy test for a valid email-address.
        this.email = email;
    }

    @Override
    public Date getValidUntil() {
        return validUntil;
    }

    /**
     * Set property 'validUntil'. If validUntil is less or equal than validFrom
     * an IllegalArgumentException is thrown.
     *
     * @see #validUntil
     * @param validUntil the validUntil to set
     */
    public void setValidUntil(final Date validUntil) {
        DateTester.testForValidDates(validFrom, validUntil);
        this.validUntil = validUntil;
    }

    @Override
    public Date getValidFrom() {
        return validFrom;
    }

    /**
     * Set property 'validFrom'. If validFrom is greater or equal than
     * validUntil an IllegalArgumentException is thrown.
     *
     * @see #validFrom
     * @param validFrom the validFrom to set
     */
    public void setValidFrom(final Date validFrom) {
        DateTester.testForValidDates(validFrom, validUntil);
        this.validFrom = validFrom;
    }

    /**
     * Get the full name (last name, first name)
     *
     * @return The full name.
     */
    public String getFullname() {
        if (fullname == null) {
            fullname = getLastName() + ", " + getFirstName();
        }
        return fullname;
    }

    /**
     * Get the if of the group the user is currently working in.
     *
     * @return The group id.
     */
    public final GroupId getCurrentGroup() {
        return currentGroup;
    }

    /**
     * Set the if of the group the user is currently working in.
     *
     * @param currentGroup The group id.
     */
    public final void setCurrentGroup(GroupId currentGroup) {
        this.currentGroup = currentGroup;
        this.currentRole = null;
    }

    /**
     * Returns the role which was determined for this user. If no current role
     * is defined, the default role will be returned. The current role is reset
     * if the current group changes.
     *
     * @return The current role.
     */
    public final Role getCurrentRole() {
        if (WORLD_USER.equals(this) || "NoUser".equals(getDistinguishedName())) {
            return Role.NO_ACCESS;
        }
        if (currentRole == null) {
            //obtain role
            Role result = Role.NO_ACCESS;
            try {
                result = (Role) GroupServiceLocal.getSingleton().getMaximumRole(getCurrentGroup(), new UserId(getDistinguishedName()), AuthorizationContext.factorySystemContext());
                if (result == null) {
                    LOGGER.error("No max role returned for user " + getUserId() + " in group " + getCurrentGroup());
                    result = Role.NO_ACCESS;
                }
            } catch (EntityNotFoundException ex) {
                LOGGER.warn("Failed to obtain max. role for user " + getUserId() + " in group " + getCurrentGroup().getStringRepresentation(), ex);
            } catch (UnauthorizedAccessAttemptException ex) {
                LOGGER.error("Unauthorized to obtain max. role for user " + getUserId() + " in group " + getCurrentGroup().getStringRepresentation(), ex);
            }
            return result;
        }
        //returned cached value
        return currentRole;
    }

    /**
     * Set the current role. By default, this method should not be used as the
     * current role is determined via getCurrentRole(). In special cases this
     * method can be used e.g. to overwrite the role obtained from the database.
     *
     * @param pRole The current role.
     */
    public final void setCurrentRole(Role pRole) {
        currentRole = pRole;
    }

// </editor-fold>
    /**
     * Factory a dummy user which can be used if no user was found anywhere
     * instead of using 'null'. The userId of the user will be 'NoUser', the id
     * is 0 and first name, last name and email are set to 'No', 'User' and
     * 'NoUser'.
     *
     * @return The dummy user.
     */
    private static UserData factoryWorldUser() {
        UserData noUser = new UserData();
        noUser.setUserId(0l);
        noUser.setDistinguishedName(new UserId(Constants.WORLD_USER_ID).getStringRepresentation());
        noUser.setFirstName("Public");
        noUser.setLastName("Access");
        return noUser;
    }

    @Override
    public String toString() {
        StringBuilder buffer = new StringBuilder("User\n----\n");
        buffer.append(getUserId()).append(": ").append(firstName).append(" ");
        buffer.append(lastName).append("\n E-Mail: ").append(getEmail());
        buffer.append("\nDN: ").append(getDistinguishedName());
        buffer.append("\nvalid from: ").append(getValidFrom());
        buffer.append("\nvalid until: ").append(getValidUntil()).append("\n");
        return buffer.toString();
    }

    @Override
    public boolean equals(Object other) {
        boolean equals = true;
        if (this == other) {
            return equals;
        }
        if (other != null && (getClass() == other.getClass())) {
            UserData otherUserData = (UserData) other;
            if (userId != null) {
                equals = equals && (userId.equals(otherUserData.userId));
            } else {
                equals = equals && (otherUserData.userId == null);
            }
            if (equals && (firstName != null)) {
                equals = equals && (firstName.equals(otherUserData.firstName));
            } else {
                equals = equals && (otherUserData.firstName == null);
            }
            if (equals && (lastName != null)) {
                equals = equals && (lastName.equals(otherUserData.lastName));
            } else {
                equals = equals && (otherUserData.lastName == null);
            }
            if (equals && (email != null)) {
                equals = equals && (email.equals(otherUserData.email));
            } else {
                equals = equals && (otherUserData.email == null);
            }
            if (equals && (distinguishedName != null)) {
                equals = equals && (distinguishedName.equals(otherUserData.distinguishedName));
            } else {
                equals = equals && (otherUserData.distinguishedName == null);
            }
            if (equals && (validFrom != null)) {
                equals = equals && (validFrom.equals(otherUserData.validFrom));
            } else {
                equals = equals && (otherUserData.validFrom == null);
            }
            if (equals && (validUntil != null)) {
                equals = equals && (validUntil.equals(otherUserData.validUntil));
            } else {
                equals = equals && (otherUserData.validUntil == null);
            }
        } else {
            equals = false;
        }

        return equals;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 37 * hash + (this.firstName != null ? this.firstName.hashCode() : 0);
        hash = 37 * hash + (this.lastName != null ? this.lastName.hashCode() : 0);
        hash = 37 * hash + (this.email != null ? this.email.hashCode() : 0);
        return hash;
    }
    private transient org.eclipse.persistence.queries.FetchGroup fg;
    private transient Session sn;

    @Override
    public org.eclipse.persistence.queries.FetchGroup _persistence_getFetchGroup() {
        return this.fg;
    }

    @Override
    public void _persistence_setFetchGroup(org.eclipse.persistence.queries.FetchGroup fg) {
        this.fg = fg;
    }

    @Override
    public boolean _persistence_isAttributeFetched(String string) {
        return true;
    }

    @Override
    public void _persistence_resetFetchGroup() {
    }

    @Override
    public boolean _persistence_shouldRefreshFetchGroup() {
        return false;
    }

    @Override
    public void _persistence_setShouldRefreshFetchGroup(boolean bln) {

    }

    @Override
    public Session _persistence_getSession() {

        return sn;
    }

    @Override
    public void _persistence_setSession(Session sn) {
        this.sn = sn;

    }
}
