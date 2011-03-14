package com.qcadoo.plugin.internal;

import java.io.Serializable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;

import org.hibernate.HibernateException;
import org.hibernate.usertype.UserType;

import com.qcadoo.plugin.api.Version;

public class VersionType implements UserType {

    @Override
    public int[] sqlTypes() {
        return new int[] { Types.VARCHAR };
    }

    @Override
    public Class<?> returnedClass() {
        return Version.class;
    }

    @Override
    public boolean equals(final Object x, final Object y) throws HibernateException {
        return x.equals(y);
    }

    @Override
    public int hashCode(final Object x) throws HibernateException {
        return x.hashCode();
    }

    @Override
    public Object nullSafeGet(final ResultSet rs, final String[] names, final Object owner) throws HibernateException,
            SQLException {
        String version = rs.getString(names[0]);
        return rs.wasNull() ? null : new Version(version);
    }

    @Override
    public void nullSafeSet(final PreparedStatement st, final Object value, final int index) throws HibernateException,
            SQLException {
        if (value == null) {
            st.setNull(index, Types.VARCHAR);
        } else {
            st.setString(index, value.toString());
        }
    }

    @Override
    public Object deepCopy(final Object value) throws HibernateException {
        return new Version(value.toString());
    }

    @Override
    public boolean isMutable() {
        return false;
    }

    @Override
    public Serializable disassemble(final Object value) throws HibernateException {
        return (Version) value;
    }

    @Override
    public Object assemble(final Serializable cached, final Object owner) throws HibernateException {
        return cached;
    }

    @Override
    public Object replace(final Object original, final Object target, final Object owner) throws HibernateException {
        return new Version(original.toString());
    }

}
